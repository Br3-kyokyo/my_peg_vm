public class OpCodes {
    private OpCodes() {
    }

    public static final char OPCODE_CHAR = 0xE000;
    public static final char OPCODE_ANY = 0xE001;
    public static final char OPCODE_CHOICE = 0xE002;
    public static final char OPCODE_JUMP = 0xE003;
    public static final char OPCODE_CALL = 0xE004;
    public static final char OPCODE_RETURN = 0xE005;
    public static final char OPCODE_COMMIT = 0xE006;
    public static final char OPCODE_FAIL = 0xE007;
    public static final char OPCODE_END = 0xE008;

}