package peg;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import consts.Opcode;

public class OpList {

    private ArrayList<Byte> list = new ArrayList<Byte>();

    public HashMap<String, Integer> NTaddressMap = new HashMap<String, Integer>(); // NTの開始アドレス
    HashMap<Integer, String> NTtempOpcodeMap = new HashMap<Integer, String>(); // (アドレス番地, 参照したいNTの名前)

    public boolean addOpcode(byte b) {
        return list.add(b);
    }

    public boolean addOperand(int index, int n) {
        byte[] bytearray = ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN).putInt(n).array();
        List<Byte> bytelist = new ArrayList<Byte>();

        for (byte b : bytearray)
            bytelist.add(Byte.valueOf(b));

        return list.addAll(index, bytelist);
    }

    public boolean addOperand(int n) {
        return addOperand(list.size(), n);
    }

    public boolean addOperand(char c) {
        byte[] bytearray = ByteBuffer.allocate(Character.BYTES).order(ByteOrder.LITTLE_ENDIAN).putChar(c).array();
        List<Byte> bytelist = new ArrayList<Byte>();

        for (byte b : bytearray)
            bytelist.add(Byte.valueOf(b));

        return list.addAll(bytelist);
    }

    public boolean addOpblock(OpList added) {

        // 非終端記号開始位置の番地情報を揃える
        for (var ntaddrmap : added.NTaddressMap.entrySet())
            this.NTaddressMap.put(ntaddrmap.getKey(), ntaddrmap.getValue() + this.list.size());

        // 非終端記号呼び出しの番地情報を揃える
        for (var addrntmap : added.NTtempOpcodeMap.entrySet())
            this.NTtempOpcodeMap.put(this.list.size() + addrntmap.getKey(), addrntmap.getValue());

        // blist: 0 1 2 3
        // this: 0 1 2 3 4 5 6 7
        // 2が該当する位置とする。call命令が入っている。
        // この場合、元のマップでは2にntがマップされている
        // 計算後は
        // 8+2 = 10
        // 0 1 2 3 4 5 6 7 0 1 2 3

        return list.addAll(added.list);
    }

    public int size() {
        return list.size();
    }

    public int remove(int index) {
        return list.remove(index);
    }

    public byte[] toBinary() {
        byte[] bytes = new byte[list.size()];

        for (int i = 0; i < list.size(); i++)
            bytes[i] = list.get(i);

        return bytes;
    }

    public void replaceTmpOperand() {
        for (var addrntmap : this.NTtempOpcodeMap.entrySet()) {
            var replaceTargetAddr = addrntmap.getKey();
            var nt = addrntmap.getValue();

            // 仮で埋めていたバイト列を削除
            for (int i = 0; i < Integer.BYTES; i++)
                this.remove(replaceTargetAddr);

            // 当該アドレスを埋める
            try {
                int offset = this.NTaddressMap.get(nt) - (replaceTargetAddr + 4);
                this.addOperand(replaceTargetAddr, offset);
            } catch (NullPointerException e) {
                System.out.println("存在しない非終端記号を参照しています。:" + nt);
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }

    // バイト列を文字列に変換
    public String toString() {
        StringBuilder sb = new StringBuilder();

        int i = 0;
        while (i < list.size()) {
            sb.append(Integer.toHexString(i));
            sb.append(": ");
            switch (list.get(i++)) {
                case Opcode.OPCODE_CHAR:
                    sb.append("char ");
                    sb.append(readCharOperand(i));
                    i = i + 2;
                    sb.append("\n");
                    break;
                case Opcode.OPCODE_ANY:
                    sb.append("any\n");
                    break;
                case Opcode.OPCODE_CHOICE:
                    sb.append("choise ");
                    sb.append(readIntOperand(i));
                    i = i + 4;
                    sb.append("\n");
                    break;
                case Opcode.OPCODE_JUMP:
                    sb.append("jump ");
                    sb.append(readIntOperand(i));
                    i = i + 4;
                    sb.append("\n");
                    break;
                case Opcode.OPCODE_CALL:
                    sb.append("call ");
                    sb.append(readIntOperand(i));
                    i = i + 4;
                    sb.append("\n");
                    break;
                case Opcode.OPCODE_RETURN:
                    sb.append("return\n");
                    break;
                case Opcode.OPCODE_COMMIT:
                    sb.append("commit ");
                    sb.append(readIntOperand(i));
                    i = i + 4;
                    sb.append("\n");
                    break;
                case Opcode.OPCODE_FAIL:
                    sb.append("fail\n");
                    break;
                case Opcode.OPCODE_END:
                    sb.append("end\n");
                    break;
            }
        }

        return sb.toString();
    }

    private char readCharOperand(int i) {
        int mask = 0x00ff;
        return (char) ((list.get(i) & mask) | ((list.get(i + 1) & mask) << 8));
    }

    private int readIntOperand(int i) {
        int mask = 0x000000ff;
        int operand = ((list.get(i + 3) & mask) << 24) | ((list.get(i + 2) & mask) << 16)
                | ((list.get(i + 1) & mask) << 8) | (list.get(i) & mask);
        return operand;
    }
}