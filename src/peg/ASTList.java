package peg;

import java.util.Arrays;
import java.util.List;

public abstract class ASTList extends ASTree {
    private List<ASTree> children;

    public ASTList(List<ASTree> children) {
        super();
        this.children = children;
    }

    public List<ASTree> getChildren() {
        return children;
    }
}

class ParsingExpressionOr extends ASTList {

    public ParsingExpressionOr(List<ASTree> children) {
        super(children);
    }

    @Override
    public OpList eval() {
        return PiFunctions.Choice(left().eval(), right().eval());
    }

    private ASTree left() {
        return getChildren().get(0);
    }

    private ASTree right() {
        return getChildren().get(1);
    }
}

class ParsingExpressionSequence extends ASTList {

    public ParsingExpressionSequence(List<ASTree> children) {
        super(children);
    }

    @Override
    public OpList eval() {
        return PiFunctions.Sequence(left().eval(), right().eval());
    }

    private ASTree left() {
        return getChildren().get(0);
    }

    private ASTree right() {
        return getChildren().get(1);
    }
}

class ParsingExpression extends ASTList {

    Modifire modifire;

    public ParsingExpression(ASTree child, Modifire modifire) {
        super(Arrays.asList(child));
        this.modifire = modifire;
    }

    public ASTree getChild() {
        return super.getChildren().get(0);
    }

    @Override
    public OpList eval() {

        OpList bodycode = getChild().eval();

        if (modifire == Modifire.question) {
            return PiFunctions.Option(bodycode);
        } else if (modifire == Modifire.asterisk) {
            return PiFunctions.Repetation0(bodycode);
        } else if (modifire == Modifire.plus) {
            return PiFunctions.Repetation1(bodycode);
        } else if (modifire == Modifire.exclamation) {
            return PiFunctions.NotPredicate(bodycode);
        } else if (modifire == Modifire.amplifire) {
            return PiFunctions.AndPredicate(bodycode);
        } else if (modifire == Modifire.none) {
            return bodycode;
        } else {
            System.exit(-1); // 不明なenum値
        }

        return null; // 呼ばれない
    }
}