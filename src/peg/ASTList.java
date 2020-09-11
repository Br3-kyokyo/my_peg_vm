package peg;

import java.util.Arrays;
import java.util.List;

import consts.Opcode;

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

class GrammerStmnt extends ASTList {

    public GrammerStmnt(List<ASTree> children) {
        super(children);
    }

    @Override
    public OpList eval() throws RuntimeException {
        var oplist = new OpList();
        oplist.addOpcode(Opcode.OPCODE_CALL);
        oplist.addOperand(1);
        oplist.addOpcode(Opcode.OPCODE_END);
        for (ASTree tree : getChildren()) {
            RuleStmnt dif = (RuleStmnt) tree;
            OpList.NTaddressMap.put(dif.getLeft().name, oplist.size());
            oplist.addOpblock(dif.eval());
        }
        oplist = PiFunctions.ReplaceCallNtAddr(oplist);
        return oplist;
    }
}

class RuleStmnt extends ASTList {

    public RuleStmnt(List<ASTree> children) {
        super(children);
    }

    @Override
    public OpList eval() throws RuntimeException {
        var oplist = new OpList();

        oplist.addOpblock(this.getExpression().eval());
        oplist.addOpcode(Opcode.OPCODE_RETURN);

        return oplist;
    }

    public IdentifireStmnt getLeft() {
        return (IdentifireStmnt) getChildren().get(0);
    }

    public ASTree getExpression() {
        return getChildren().get(1);
    }
}

class ChoiceStmnt extends ASTList {

    public ChoiceStmnt(List<ASTree> children) {
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

class SequenceStmnt extends ASTList {

    public SequenceStmnt(List<ASTree> children) {
        super(children);
    }

    @Override
    public OpList eval() {
        OpList left = left().eval();
        OpList right = right().eval();

        return PiFunctions.Sequence(left, right);
    }

    private ASTree left() {
        return getChildren().get(0);
    }

    private ASTree right() {
        return getChildren().get(1);
    }
}

class PrimaryStmnt extends ASTList {

    Modifire modifire;

    public PrimaryStmnt(ASTree child, Modifire modifire) {
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