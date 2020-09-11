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

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        StringBuilder sb = new StringBuilder();
        for (ASTree tree : getChildren()) {
            sb.append(tree.toString());
            sb.append('\n');
        }
        return sb.toString();
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

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        StringBuilder sb = new StringBuilder();
        sb.append(getLeft().toString());
        sb.append(" -> ");
        sb.append(getExpression().toString());
        return sb.toString();
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

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        StringBuilder sb = new StringBuilder();
        sb.append(left().toString());
        sb.append(" / ");
        sb.append(right().toString());
        return sb.toString();
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

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        StringBuilder sb = new StringBuilder();
        sb.append(left().toString());
        sb.append(" ");
        sb.append(right().toString());
        return sb.toString();
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

    @Override
    public String toString() {

        // TODO Auto-generated method stub
        StringBuilder sb = new StringBuilder();
        if (modifire == Modifire.question) {
            sb.append("Opt(");
        } else if (modifire == Modifire.asterisk) {
            sb.append("Rep0(");
        } else if (modifire == Modifire.plus) {
            sb.append("Rep1(");
        } else if (modifire == Modifire.exclamation) {
            sb.append("Not(");
        } else if (modifire == Modifire.amplifire) {
            sb.append("And(");
        } else if (modifire == Modifire.none) {
        } else {
            System.exit(-1); // 不明なenum値
        }

        sb.append(getChild().toString());

        if (modifire != Modifire.none)
            sb.append(")");

        return sb.toString();
    }
}