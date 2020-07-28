package peg;

import java.util.List;

public abstract class ASTList extends ASTree {
    public List<ASTree> children;

    public ASTList(List<ASTree> children) {
        super(Modifire.none);
        this.children = children;
    }

    public ASTList(List<ASTree> children, Modifire modifire) {
        super(modifire);
        this.children = children;
    }

    public List<ASTree> getChildren() {
        return children;
    }
}

class GrammerList extends ASTList {
    public GrammerList(List<ASTree> children) {
        super(children, Modifire.none);
    }

    @Override
    public void printTree() {
        // TODO Auto-generated method stub
    }

    @Override
    public void eval(VMCodeGeneratorResult result) {
        for (ASTree grammer : children)
            grammer.eval(result);
    }
}

class Grammer extends ASTList {
    public Grammer(List<ASTree> children) {
        super(children, Modifire.none);
    }

    @Override
    public void eval(VMCodeGeneratorResult result) {
        leftterm().eval(result);
        rightterm().eval(result);
    }

    private ASTree leftterm() {
        return children.get(0);
    }

    private ASTree rightterm() {
        return children.get(1);
    }

    @Override
    public void printTree() {
        // TODO Auto-generated method stub

    }

}

class ParsingExpressionOr extends ASTList {

    public ParsingExpressionOr(List<ASTree> children) {
        super(children);
    }

    public ParsingExpressionOr(List<ASTree> children, Modifire modifire) {
        super(children, modifire);
    }

    @Override
    public void printTree() {
        // TODO Auto-generated method stub
    }

    @Override
    public void eval(VMCodeGeneratorResult result) {
        // TODO Auto-generated method stub

    }
}

class ParsingExpressionSequence extends ASTList {

    public ParsingExpressionSequence(List<ASTree> children) {
        super(children);
    }

    public ParsingExpressionSequence(List<ASTree> children, Modifire modifire) {
        super(children, modifire);
    }
}