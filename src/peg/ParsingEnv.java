package peg;

public class ParsingEnv {
    private GrammerStmnt root;
    private boolean packrat;

    public ParsingEnv(ASTree root, boolean packrat) {
        this.root = (GrammerStmnt) root;
        this.packrat = packrat;
    }

    public boolean isPackrat() {
        return packrat;
    }

    public GrammerStmnt getTreeRoot() {
        return root;
    }
}