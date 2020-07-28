package peg;

import java.util.HashMap;
import java.util.List;

public abstract class ASTLeaf extends ASTree {

    public ASTLeaf() {
        super(Modifire.none);
    }

    public ASTLeaf(final Modifire modifire) {
        super(modifire);
    }

    // public abstract String toString();
}

class StringStmnt extends ASTLeaf {
    private final String value;

    public StringStmnt(final String value) {
        super();
        this.value = value;
    }

    public StringStmnt(final String value, final Modifire modifire) {
        super(modifire);
        this.value = value;
    }

    @Override
    public void eval(final VMCodeGeneratorResult result) {
        for (final char c : value.toCharArray())
            result.add_char(c);
    }

    @Override
    public void printTree() {
        // TODO Auto-generated method stub

    }

}

class CharStmnt extends ASTLeaf {
    private final char value;

    public CharStmnt(final char value) {
        super();
        this.value = value;
    }

    public CharStmnt(final char value, final Modifire modifire) {
        super(modifire);
        this.value = value;
    }

    @Override
    public String toString() {
        return "(" + value + ")";
    }

    @Override
    public void eval(final VMCodeGeneratorResult result) {
        result.add_char(value);
    }
}

class BracketStmnt extends ASTLeaf {
    private final List<Tuple<Character, Character>> body;

    public BracketStmnt(final List<Tuple<Character, Character>> body) {
        super();
        this.body = body;
    }

    public BracketStmnt(final List<Tuple<Character, Character>> body, final Modifire modifire) {
        super(modifire);
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (final var pair : body)
            sb.append("(" + pair.first + "," + pair.first + ")");
        sb.append("]");
        return sb.toString();
    }

    @Override
    public void eval(final VMCodeGeneratorResult result) {
        result.

    }
}

class GrammerLeftTermStmnt extends ASTLeaf {

    static HashMap<String, Integer> NTaddressMap;
    String name;

    public GrammerLeftTermStmnt(final String name) {
        super();
        this.name = name;
    }

    @Override
    public void printTree() {
        // TODO Auto-generated method stub

    }

    @Override
    public void eval(VMCodeGeneratorResult result) {
        result.addNewGrammer(name);
    }
}

class NonTerminationStmnt extends ASTLeaf {
    String name;

    public NonTerminationStmnt(final String name) {
        super();
        this.name = name;
    }

    public NonTerminationStmnt(final String name, final Modifire modifire) {
        super(modifire);
        this.name = name;
    }
}

class EmptyStmnt extends ASTLeaf {

    public EmptyStmnt() {
        super();
    }

    public EmptyStmnt(final Modifire modifire) {
        super(modifire);
    }
}

class DotStmnt extends ASTLeaf {
    public DotStmnt() {
        super();
    }

    public DotStmnt(final Modifire modifire) {
        super(modifire);
    }
}