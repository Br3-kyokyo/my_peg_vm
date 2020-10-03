package vm;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import consts.Opcode;

//構文解析に特化した仮想マシン
public class VM {

    private boolean packrat;
    private int ruleNums;

    private byte[] program;
    private char[] inputString;
    private List<String> inputLines;

    private int ip = 0; // 入力の解析位置(input parsing position)
    private int pc = 0; // プログラムカウンタ(program counter)
    private Deque<Entry> stack = new ArrayDeque<Entry>(); // Stack
    private MemoTable memoTable;

    private TreeMap<Integer, Integer> ipLinenumMap = new TreeMap<Integer, Integer>();

    Failure farthestFailure = new Failure(0, 0, ' ', ' ');

    private int memonum = 0;
    private int loopnum = 0;
    private int backtracknum = 0;

    public VM(byte[] _program, List<String> _input) throws Exception {

        this.program = _program;
        this.inputLines = _input;
        this.inputString = inputLines.stream().collect(Collectors.joining(System.getProperty("line.separator")))
                .toCharArray();

        packrat = isPackrat(program[pc++]);

        if (packrat) {
            ruleNums = readIntOperand();
            // memoTable = new MemoTable(ruleNums, inputString.length + 1);
            memoTable = new MemoTable(ruleNums);
        }

        int lengthsum = 0;
        for (int i = 0; i < inputLines.size(); i++) {
            ipLinenumMap.put(lengthsum, i);
            lengthsum = lengthsum + (inputLines.get(i).length() + 1); // +1は改行文字分
        }
    }

    // 構文解析に成功した場合true、失敗した場合falseを返す。
    public boolean exec() throws UnknownInstructionException {
        try {
            while (true) {
                loopnum++;
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
                    System.out.println("btnum:" + backtracknum);
                    System.out.println("loop:" + loopnum);
                    System.out.println("memo:" + memonum);
                    return true;
                } else if (opcode == Opcode.OPCODE_FAILTWICE) {
                    inst_failtwice();
                } else if (opcode == Opcode.OPCODE_PARTIALCOMMIT) {
                    inst_partialcommit();
                } else if (opcode == Opcode.OPCODE_BACKCOMMIT) {
                    inst_backcommit();
                } else if (opcode == Opcode.OPCODE_MEMO) {
                    inst_memo();
                } else if (opcode == Opcode.OPCODE_WRITE) {
                    inst_write();
                } else if (opcode == Opcode.OPCODE_LOG) {
                    System.out.print(readCharOperand());
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
        stack.push(new ReturnEntry(pc, ip));
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
        if (inputString.length == ip) { // TODO =<の方がいい？
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
            inst_fail(operand, c);
    }

    private void inst_fail(char predicated, char actual) throws SyntaxError {
        if (this.farthestFailure.ip <= this.ip)
            this.farthestFailure = new Failure(pc, ip, predicated, actual);

        boolean success = backtrack();
        if (!success)
            throw new SyntaxError(predicated, actual);
    }

    private void inst_fail() throws SyntaxError {
        boolean success = backtrack();
        if (!success)
            throw new SyntaxError();
    }

    private void inst_failtwice() throws SyntaxError {
        stack.pop(); // inst_fail()でも多分大丈夫
        inst_fail();
    }

    private void inst_partialcommit() {
        int operand = readIntOperand();

        BacktrackEntry e = (BacktrackEntry) stack.pop();
        stack.push(new BacktrackEntry(e.pc, this.ip));
        pc = pc + operand;
    }

    private void inst_backcommit() {
        int operand = readIntOperand();

        BacktrackEntry be = (BacktrackEntry) stack.pop();

        pc = pc + operand;
        ip = be.ip;
    }

    private void inst_memo() throws SyntaxError {
        int ruleid = readIntOperand();

        ((ReturnEntry) stack.peek()).id = ruleid;

        MemoEntry memoEntry = memoTable.get(ruleid, ip);

        if (memoEntry == null)
            return;

        memonum++;

        if (memoEntry.success) {
            this.ip = memoEntry.ip;
            inst_return();
        } else {
            inst_fail();
        }
    }

    private void inst_write() {
        ReturnEntry rentry = (ReturnEntry) stack.peek();
        // memoTable.set(rentry.id, rentry.ip, ip, true);
        memoTable.set(rentry.id, rentry.ip, new MemoEntry(ip, true));

    }

    private boolean backtrack() {
        while (!stack.isEmpty()) {
            Entry entry = stack.pop();
            backtracknum++;
            if (entry instanceof BacktrackEntry) {
                BacktrackEntry bentry = (BacktrackEntry) entry;
                ip = bentry.ip;
                pc = bentry.pc;
                return true;
            } else if (packrat && entry instanceof ReturnEntry) {
                ReturnEntry rentry = (ReturnEntry) entry;
                // memoTable.set(rentry.id, rentry.ip, -1, false);
                memoTable.set(rentry.id, rentry.ip, new MemoEntry(-1, false));
            }
        }
        return false;
    }

    private void errorReporting() {
        Failure f = farthestFailure;

        java.util.Map.Entry<Integer, Integer> posLinenum = ipLinenumMap.floorEntry(f.ip);
        int rowip = posLinenum.getKey();
        int linenum = posLinenum.getValue();

        int charpos = f.ip - rowip;
        String row = inputLines.get(linenum);

        System.out.println("pc:" + f.pc);
        System.out.println((linenum + 1) + ":" + charpos + "(" + f.ip + "): syntax error");
        System.out.println(row);
        for (int i = 0; i < (row.length() + 1); i++) {
            if (i == charpos)
                System.out.print("^");
            else
                System.out.print(" ");
        }

        System.out.println();
        System.out.println("predicated: " + "'" + f.predicated + "'");
        System.out.println("actual: " + "'" + f.actual + "'");

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

    private boolean isPackrat(byte b) throws Exception {
        if (b == 0x00) {
            return false;
        } else if (b == 0x01) {
            return true;
        } else {
            throw new Exception("Packrat flag must be 0x00 or 0x01");
        }
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
    int ip; // メモ化用
    int id = -1; // メモ化用

    ReturnEntry(int pc, int ip) {
        this.pc = pc;
        this.ip = ip;
    }
}

class Failure {

    int pc;
    int ip;
    char predicated;
    char actual;

    public Failure(int pc, int ip, char predicated, char actual) {
        this.pc = pc;
        this.ip = ip;
        this.predicated = predicated;
        this.actual = actual;
    }
}
