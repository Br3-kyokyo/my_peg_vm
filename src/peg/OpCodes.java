package peg;

public class OpCodes {
    private OpCodes() {
    }

    public static final byte OPCODE_CAHR = 0x00;
    public static final byte OPCODE_ANY = 0x01;
    public static final byte OPCODE_CHOICE = 0x02;
    public static final byte OPCODE_JUMP = 0x03;
    public static final byte OPCODE_CALL = 0x04;
    public static final byte OPCODE_RETURN = 0x05;
    public static final byte OPCODE_COMMIT = 0x06;
    public static final byte OPCODE_FAIL = 0x07;
    public static final byte OPCODE_END = 0x08;

}