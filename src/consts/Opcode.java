package consts;

public class Opcode extends Op {
    private Opcode() {
    }

    // Primary Opration - 主要命令
    public static final byte OPCODE_CHAR = 0x00;
    public static final byte OPCODE_ANY = 0x01;
    public static final byte OPCODE_CHOICE = 0x02;
    public static final byte OPCODE_JUMP = 0x03;
    public static final byte OPCODE_CALL = 0x04;
    public static final byte OPCODE_RETURN = 0x05;
    public static final byte OPCODE_COMMIT = 0x06;
    public static final byte OPCODE_FAIL = 0x07;
    public static final byte OPCODE_END = 0x08;

    // Optimisation Operation - 最適化命令
    public static final byte OPCODE_FAILTWICE = 0x09;
    public static final byte OPCODE_PARTIALCOMMIT = 0x0a;
    public static final byte OPCODE_BACKCOMMIT = 0x0b;

    // PackratParsing Operation - PackratParser化のために追加
    public static final byte OPCODE_MEMO = 0x0c;
    public static final byte OPCODE_WRITE = 0x0d;

    public static final byte OPCODE_LOG = 0x0e;

    public static final int BYTES = 1;

    public static String getName(byte b) {
        switch (b) {
            case OPCODE_CHAR:
                return "char";
            case OPCODE_ANY:
                return "any";
            case OPCODE_CHOICE:
                return "choice";
            case OPCODE_JUMP:
                return "jump";
            case OPCODE_CALL:
                return "call";
            case OPCODE_RETURN:
                return "return";
            case OPCODE_COMMIT:
                return "commit";
            case OPCODE_FAIL:
                return "fail";
            case OPCODE_END:
                return "end";
            default:
                return "unknown";
        }
    }
}