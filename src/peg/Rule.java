package peg;

public class Rule {

    private RuleNT nt;
    private ASTree rulebody;

    public Rule(RuleNT nt, ASTree rulebody) {
        this.nt = nt;
        this.rulebody = rulebody;
    }

    public RuleNT getNt() {
        return nt;
    }

    public ASTree getBody() {
        return rulebody;
    }
}

class RuleNT {

    String name;

    public RuleNT(String string) {
        this.name = string;
    }

}
