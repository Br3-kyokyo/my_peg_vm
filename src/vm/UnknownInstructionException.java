package vm;

public class UnknownInstructionException extends Exception {

    public UnknownInstructionException(int pc) {
        super("pc: " + pc);
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;

}
