package peg;

import java.util.Arrays;
import java.util.List;

public abstract class ASTList extends ASTree {
    public List<ASTree> children;

    public ASTList(List<ASTree> children) {
        super();
        this.children = children;
    }

    public List<ASTree> getChildren() {
        return children;
    }
}

class GrammerList extends ASTList {
    public GrammerList(List<ASTree> children) {
        super(children);
    }
}

class Grammer extends ASTList {
    public Grammer(List<ASTree> children) {
        super(children);
    }
}

class ParsingExpressionOr extends ASTList {

    public ParsingExpressionOr(List<ASTree> children) {
        super(children);
    }
}

class ParsingExpressionSequence extends ASTList {

    public ParsingExpressionSequence(List<ASTree> children) {
        super(children);
    }
}

class ParsingExpression extends ASTList {

    Modifire modifire;

    public ParsingExpression(ASTree child, Modifire modifire) {
        super(Arrays.asList(child));
        this.modifire = modifire;
    }

    public ASTree getChild() {
        return super.children.get(0);
    }
}