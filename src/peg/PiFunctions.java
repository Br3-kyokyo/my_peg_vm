package peg;

import java.util.List;
import consts.*;

public class PiFunctions {

    private PiFunctions() {
    }

    public static OpList Choice(OpList e1, OpList e2) {
        var list = new OpList();

        list.addOpcode(Opcode.OPCODE_CHOICE);
        list.addOperand(e1.size() + Opcode.BYTES + IntOperand.BYTES); // commit命令:5バイト
        list.addOpblock(e1);
        list.addOpcode(Opcode.OPCODE_COMMIT);
        list.addOperand(e2.size());
        list.addOpblock(e2);

        return list;
    }

    public static OpList Sequence(OpList e1, OpList e2) {
        var list = new OpList();
        list.addOpblock(e1);
        list.addOpblock(e2);
        return list;
    }

    public static OpList NT(String name) {
        var list = new OpList();

        list.addOpcode(Opcode.OPCODE_CALL);
        list.NTtempOpcodeMap.put(list.size(), name);
        list.addOperand(0); // tmp

        return list;
    }

    public static OpList Char(char c) {
        var list = new OpList();

        list.addOpcode(Opcode.OPCODE_CHAR);
        list.addOperand(c);

        return list;
    }

    public static OpList String(String s) {
        var list = new OpList();

        for (char c : s.toCharArray())
            list.addOpblock(Char(c));

        return list;
    }

    public static OpList Range(List<Character> range) throws RuntimeException {
        if (range.size() == 1) {
            return Char(range.remove(0));
        } else if (range.size() > 1) {
            var head = range.remove(0);
            return Choice(Char(head), Range(range));
        } else {
            throw new RuntimeException();
        }
    }

    public static OpList Any() {
        var list = new OpList();
        list.addOpcode(Opcode.OPCODE_ANY);
        return list;
    }

    public static OpList Option(OpList e) {
        var list = new OpList();

        list.addOpcode(Opcode.OPCODE_CHOICE);
        list.addOperand(e.size() + Opcode.BYTES + IntOperand.BYTES);
        list.addOpblock(e);
        list.addOpcode(Opcode.OPCODE_COMMIT);
        list.addOperand(0);

        return list;
    }

    public static OpList Repetation0(OpList e) {
        var list = new OpList();

        list.addOpcode(Opcode.OPCODE_CHOICE);
        list.addOperand(e.size() + Opcode.BYTES + IntOperand.BYTES);
        list.addOpblock(e);
        list.addOpcode(Opcode.OPCODE_COMMIT);
        list.addOperand(-(IntOperand.BYTES + Opcode.BYTES + e.size() + Opcode.BYTES + IntOperand.BYTES));
        return list;

    }

    public static OpList Repetation1(OpList e) {
        var list = new OpList();
        list.addOpblock(e);
        list.addOpblock(Repetation0(e));
        return list;
    }

    public static OpList NotPredicate(OpList e) {
        var list = new OpList();
        list.addOpcode(Opcode.OPCODE_CHOICE);
        list.addOperand(e.size() + Opcode.BYTES + IntOperand.BYTES + Opcode.BYTES);
        list.addOpblock(e);
        list.addOpcode(Opcode.OPCODE_COMMIT);
        list.addOperand(Opcode.BYTES);
        list.addOpcode(Opcode.OPCODE_FAIL);
        return list;
    }

    public static OpList AndPredicate(OpList e) {
        var list = new OpList();
        list.addOpblock(NotPredicate(NotPredicate(e)));
        return list;
    }

    public static OpList ReplaceCallNtAddr(OpList oplist) {
        for (var addrntmap : oplist.NTtempOpcodeMap.entrySet()) {
            var replaceTargetAddr = addrntmap.getKey();
            var nt = addrntmap.getValue();

            // 仮で埋めていたバイト列を削除
            for (int i = 0; i < Integer.BYTES; i++)
                oplist.remove(replaceTargetAddr);

            // 当該アドレスを埋める
            try {
                int offset = OpList.NTaddressMap.get(nt) - (replaceTargetAddr + 4);
                oplist.addOperand(replaceTargetAddr, offset);
            } catch (NullPointerException e) {
                System.out.println("存在しない非終端記号を参照しています。:" + nt);
                e.printStackTrace();
                System.exit(-1);
            }
        }

        // 0 1 2 3 4 5 6 7
        // c t

        return oplist;
    }
}