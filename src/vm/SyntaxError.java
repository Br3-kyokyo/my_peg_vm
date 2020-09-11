package vm;

public class SyntaxError extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    boolean charmismatch = false;
    char predicated = ' ';
    char actual = ' ';

    public SyntaxError() {
        super();
    }

    public SyntaxError(char predicated, char actual) {
        super();
        this.charmismatch = true;
        this.predicated = predicated;
        this.actual = actual;
    }

}