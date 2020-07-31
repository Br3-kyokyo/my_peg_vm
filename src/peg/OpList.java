package peg;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OpList {

    private ArrayList<Byte> list = new ArrayList<Byte>();

    static HashMap<String, Integer> NTaddressMap = new HashMap<String, Integer>(); // NTの開始アドレス
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

    public boolean addOpblock(OpList bList) {

        for (var addrntmap : bList.NTtempOpcodeMap.entrySet())
            this.NTtempOpcodeMap.put(this.list.size() + addrntmap.getKey(), addrntmap.getValue());

        // blist: 0 1 2 3
        // this: 0 1 2 3 4 5 6 7

        // 2が該当する位置とする。call命令が入っている。
        // この場合、元のマップでは2にntがマップされている
        // 計算後は
        // 8+2 = 10

        // 0 1 2 3 4 5 6 7 0 1 2 3

        return list.addAll(bList.list);
    }

    public int size() {
        return list.size();
    }

    public int remove(int index) {
        return list.remove(index);
    }

    public byte[] toArray() {
        byte[] bytes = new byte[list.size()];

        for (int i = 0; i < list.size(); i++)
            bytes[i] = list.get(i);

        return bytes;
    }
}