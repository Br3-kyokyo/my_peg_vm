package peg;

import java.util.HashMap;
import java.util.List;

public abstract class ASTLeaf extends ASTree {

}

class StringStmnt extends ASTLeaf {
    private final String value;

    public StringStmnt(final String value) {
        super();
        this.value = value;
    }

}

class CharStmnt extends ASTLeaf {
    private final char value;

    public CharStmnt(final char value) {
        super();
        this.value = value;
    }

}

class BracketStmnt extends ASTLeaf {
    private final List<Tuple<Character, Character>> body;

    public BracketStmnt(final List<Tuple<Character, Character>> body) {
        super();
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
}

class GrammerLeftTermStmnt extends ASTLeaf {

    String name;

    public GrammerLeftTermStmnt(final String name) {
        super();
        this.name = name;
    }
}

class NonTerminationStmnt extends ASTLeaf {
    String name;

    public NonTerminationStmnt(final String name) {
        super();
        this.name = name;
    }
}

class EmptyStmnt extends ASTLeaf {

    public EmptyStmnt() {
        super();
    }
}

class DotStmnt extends ASTLeaf {
    public DotStmnt() {
        super();
    }
}