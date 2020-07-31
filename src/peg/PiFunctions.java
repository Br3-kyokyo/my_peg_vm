package peg;

import java.util.List;

public class PiFunctions {

    private PiFunctions() {
    }

    public static OpList Choice(OpList e1, OpList e2) {
        var list = new OpList();

        list.addOpcode(OpCodes.OPCODE_CHOICE);
        list.addOperand(e1.size() + 5); //commit命令:5バイト
        list.addOpblock(e1);
        list.addOpcode(OpCodes.OPCODE_COMMIT);
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

        list.addOpcode(OpCodes.OPCODE_CALL);
        list.NTtempOpcodeMap.put(list.size(), name);
        list.addOperand(0); // tmp

        return list;
    }

    public static OpList Char(char c) {
        var list = new OpList();

        list.addOpcode(OpCodes.OPCODE_CAHR);
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
        list.addOpcode(OpCodes.OPCODE_ANY);
        return list;
    }

    public static OpList Option(OpList e) {
        var list = new OpList();

        list.addOpcode(OpCodes.OPCODE_CHOICE);
        list.addOperand(e.size() + 5);
        list.addOpblock(e);
        list.addOpcode(OpCodes.OPCODE_COMMIT);
        list.addOperand(0);

        return list;
    }

    public static OpList Repetation0(OpList e) {
        var list = new OpList();

        list.addOpcode(OpCodes.OPCODE_CHOICE);
        list.addOperand(e.size() + 5);
        list.addOpblock(e);
        list.addOpcode(OpCodes.OPCODE_COMMIT);
        list.addOperand(-(e.size() + 5 + 4 + 1));
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
        list.addOpcode(OpCodes.OPCODE_CHOICE);
        list.addOperand(e.size() + 5 + 1);
        list.addOpblock(e);
        list.addOpcode(OpCodes.OPCODE_COMMIT);
        list.addOperand(1);
        list.addOpcode(OpCodes.OPCODE_FAIL);
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
            oplist.addOperand(replaceTargetAddr, OpList.NTaddressMap.get(nt) - (replaceTargetAddr + 4));
        }

        // 0 1 2 3 4 5 6 7
        // c t

        return oplist;
    }
}