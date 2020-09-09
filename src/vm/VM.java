package vm;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import consts.Opcode;

//構文解析に特化した仮想マシン
public class VM {

    private byte[] program;
    private char[] inputString;
    private List<String> inputLines;

    private int ip = 0; // 入力の解析位置(input parsing position)
    private int pc = 0; // プログラムカウンタ(program counter)
    private Deque<Entry> stack = new ArrayDeque<Entry>(); // Stack

    private TreeMap<Integer, Integer> ipLinenumMap = new TreeMap<Integer, Integer>();

    int farthestFailedPoint = 0;

    public VM(byte[] program, List<String> input) {
        this.program = program;
        this.inputLines = input;
        this.inputString = input.stream().collect(Collectors.joining(System.getProperty("line.separator")))
                .toCharArray();

        int lengthsum = 0;
        for (int i = 0; i < input.size(); i++) {
            ipLinenumMap.put(lengthsum, i);
            lengthsum = lengthsum + (input.get(i).length() + 1); // +1は改行文字分
        }
    }

    // 構文解析に成功した場合true、失敗した場合falseを返す。
    public boolean exec() throws UnknownInstructionException {
        try {
            while (true) {
                // System.out.println(ip + ":" + Integer.toHexString(pc));
                byte opcode = program[pc++];
                if (opcode == Opcode.OPCODE_CHAR) {
                    inst_char();
                } else if (opcode == Opcode.OPCODE_ANY) {
                    inst_any();
                } else if (opcode == Opcode.OPCODE_CHOICE) {
                    inst_choice();
                } else if (opcode == Opcode.OPCODE_JUMP) {
                    inst_jump();
                } else if (opcode == Opcode.OPCODE_CALL) {
                    inst_call();
                } else if (opcode == Opcode.OPCODE_RETURN) {
                    inst_return();
                } else if (opcode == Opcode.OPCODE_COMMIT) {
                    inst_commit();
                } else if (opcode == Opcode.OPCODE_FAIL) {
                    inst_fail();
                } else if (opcode == Opcode.OPCODE_END) {
                    inst_end();
                    return true;
                } else {
                    throw new UnknownInstructionException(pc);
                }
            }
        } catch (SyntaxError e) {
            errorReporting();
            return false;
        }
    }

    private void inst_end() throws SyntaxError {
        if (inputString.length == ip) {
            return;
        } else {
            throw new SyntaxError();
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

    private void inst_any() throws SyntaxError {
        if (inputString.length == ip) {
            inst_fail();
        } else {
            ip++;
        }
    }

    private void inst_char() throws SyntaxError {
        if (inputString.length == ip) {
            inst_fail();
            return;
        }
        char c = inputString[ip];
        char operand = readCharOperand();
        if (c == operand)
            ip++;
        else
            inst_fail();
    }

    private void inst_fail() throws SyntaxError {

        if (this.farthestFailedPoint < this.ip)
            this.farthestFailedPoint = this.ip;

        while (!stack.isEmpty()) {
            Entry entry = stack.pop();
            if (entry instanceof BacktrackEntry) {
                BacktrackEntry bentry = (BacktrackEntry) entry;
                ip = bentry.ip;
                pc = bentry.pc;
                return;
            }
        }
        throw new SyntaxError();
    }

    private void errorReporting() {
        int fp = farthestFailedPoint;

        java.util.Map.Entry<Integer, Integer> posLinenum = ipLinenumMap.floorEntry(fp);
        int rowip = posLinenum.getKey();
        int linenum = posLinenum.getValue();

        int charpos = fp - rowip;
        String row = inputLines.get(linenum);

        System.out.println((linenum + 1) + ":" + charpos + ": syntax error");
        System.out.println(row);
        for (int i = 0; i < (row.length() + 1); i++) {
            if (i == charpos)
                System.out.print("^");
            else
                System.out.print(" ");
        }

    }

    private char readCharOperand() {
        int mask = 0x00ff;
        return (char) ((program[pc++] & mask) | ((program[pc++] & mask) << 8));
    }

    private int readIntOperand() {

        int mask = 0x000000ff;
        int operand = ((program[pc + 3] & mask) << 24) | ((program[pc + 2] & mask) << 16)
                | ((program[pc + 1] & mask) << 8) | (program[pc] & mask);
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
