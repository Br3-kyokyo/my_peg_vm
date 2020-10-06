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
    public OpList eval(ParsingEnv env) throws RuntimeException {
        var oplist = new OpList();
        oplist.addOpcode(Opcode.OPCODE_CALL);
        oplist.addOperand(1);
        oplist.addOpcode(Opcode.OPCODE_END);

        for (ASTree tree : getChildren())
            oplist.addOpblock(tree.eval(env));

        // 仮の非終端記号アドレスへのオフセットオペランドを実際の値で埋める
        oplist.replaceTmpOperand();
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

    private String name;

    private static int nextid = 0;
    private int id;

    public RuleStmnt(String name, ASTree expr) {
        super(Arrays.asList(expr));
        this.name = name;
        id = nextid++;
    }

    @Override
    public OpList eval(ParsingEnv env) throws RuntimeException {
        var oplist = new OpList();
        var packrat = env.isPackrat();

        oplist.NTaddressMap.put(name, 0);

        if (packrat) {
            oplist.addOpcode(Opcode.OPCODE_MEMO);
            oplist.addOperand(id);
        }

        oplist.addOpblock(this.getExpression().eval(env));

        if (packrat)
            oplist.addOpcode(Opcode.OPCODE_WRITE);

        oplist.addOpcode(Opcode.OPCODE_RETURN);

        return oplist;
    }

    public ASTree getExpression() {
        return getChildren().get(0);
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(" -> ");
        sb.append(getExpression().toString());
        return sb.toString();
    }

    public int getid() {
        return id;
    }

    public String getName() {
        return name;
    }
}

class ChoiceStmnt extends ASTList {

    public ChoiceStmnt(List<ASTree> children) {
        super(children);
    }

    @Override
    public OpList eval(ParsingEnv env) {
        return PiFunctions.Choice(left().eval(env), right().eval(env));
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
    public OpList eval(ParsingEnv env) {
        OpList left = left().eval(env);
        OpList right = right().eval(env);

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
    public OpList eval(ParsingEnv env) {

        OpList bodycode = getChild().eval(env);

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