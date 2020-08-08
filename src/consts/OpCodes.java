package consts;

public class OpCodes {
    private OpCodes() {
    }

    public static final byte OPCODE_CHAR = 0x00;
    public static final byte OPCODE_ANY = 0x01;
    public static final byte OPCODE_CHOICE = 0x02;
    public static final byte OPCODE_JUMP = 0x03;
    public static final byte OPCODE_CALL = 0x04;
    public static final byte OPCODE_RETURN = 0x05;
    public static final byte OPCODE_COMMIT = 0x06;
    public static final byte OPCODE_FAIL = 0x07;
    public static final byte OPCODE_END = 0x08;

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