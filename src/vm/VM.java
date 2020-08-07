package vm;

import java.util.ArrayDeque;
import java.util.Deque;

import consts.OpCodes;

//構文解析に特化した仮想マシン
public class VM {
    int ip = 0; // 入力の解析位置(input parsing position)
    int pc = 0; // プログラムカウンタ(program counter)
    Deque<Entry> stack = new ArrayDeque<Entry>(); // Stack

    private byte[] program;
    private char[] inputString;

    public VM(byte[] program, char[] inputString) {
        this.program = program;
        this.inputString = inputString;
    }

    public boolean exec() throws ParseException {
        while (true) {
        	System.out.println(ip + ":" + pc);
            byte opcode = program[pc++];
            if (opcode == OpCodes.OPCODE_CHAR) {
                inst_char();
            } else if (opcode == OpCodes.OPCODE_ANY) {
                inst_any();
            } else if (opcode == OpCodes.OPCODE_CHOICE) {
                inst_choice();
            } else if (opcode == OpCodes.OPCODE_JUMP) {
                inst_jump();
            } else if (opcode == OpCodes.OPCODE_CALL) {
                inst_call();
            } else if (opcode == OpCodes.OPCODE_RETURN) {
                inst_return();
            } else if (opcode == OpCodes.OPCODE_COMMIT) {
                inst_commit();
            } else if (opcode == OpCodes.OPCODE_FAIL) {
                inst_fail();
            } else if (opcode == OpCodes.OPCODE_END) {
                return inst_end();
            } else {
                throw new ParseException("unknown instruction:" + pc);
            }
        }
    }

    private boolean inst_end() {
        if (inputString.length == ip) {
            return true;
        } else {
            System.out.println("syntax error: " + ip);
            return false;
        }
    }

    private void inst_commit() {
        stack.pop();
        int addrOffset = readIntOperand();
        pc = pc + addrOffset;
    }

    private void inst_return() {
        ReturnEntry re = (ReturnEntry) stack.pop();
        this.pc = re.pc;
    }

    private void inst_call() {
        int addrOffset = readIntOperand();
        stack.push(new ReturnEntry(pc));
        pc = pc + addrOffset;
    }

    private void inst_jump() {
        int addrOffset = readIntOperand();
        pc = pc + addrOffset;
    }

    private void inst_choice() {
        int addrOffset = readIntOperand();
        stack.push(new BacktrackEntry(pc + addrOffset, ip));
    }

    private void inst_any() {
        if (inputString.length == ip) {
            inst_fail();
        } else {
            ip++;
        }
    }

    private void inst_char() {
        char c = inputString[ip];
        char operand = readCharOperand();
        if (c == operand)
            ip++;
        else
            inst_fail();
    }

    private void inst_fail() {
    	int failedip = ip;
        while (!stack.isEmpty()) {
            Entry entry = stack.pop();
            if (entry instanceof BacktrackEntry) {
                BacktrackEntry bentry = (BacktrackEntry) entry;
                ip = bentry.ip;
                pc = bentry.pc;
                return;
            }
        }
        System.out.println("syntax error: " + failedip);
        System.exit(-1);
    }

    private char readCharOperand() {
        return (char) ((program[pc++]) | (program[pc++] << 8));
    }

    private int readIntOperand() {
    	
    	int mask = 0x000000ff;
    	int operand = ((program[pc+3] & mask) << 24) | ((program[pc+2] & mask) << 16) | ((program[pc+1] & mask) << 8) | (program[pc] & mask);	
    	pc = pc + 4;
 
        return operand;
    }
}

class Entry {

}

class BacktrackEntry extends Entry {
    int pc;
    int ip;

    BacktrackEntry(int pc, int pos) {
        this.pc = pc;
        this.ip = pos;
    }
}

class ReturnEntry extends Entry {
    int pc;
    // int pos;
    // String id; //メモ化用

    ReturnEntry(int pc) {
        this.pc = pc;
    }
}