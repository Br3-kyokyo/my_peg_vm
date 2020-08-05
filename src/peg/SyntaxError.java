package peg;

public class SyntaxError extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public SyntaxError(int ip) {
        super(ip + ":Syntax Error");
    }

}
