package peg;

import java.util.ArrayList;
import java.util.List;

public abstract class ASTLeaf extends ASTree {

}

class StringStmnt extends ASTLeaf {
    private final String value;

    public StringStmnt(final String value) {
        super();
        this.value = value;
    }

    @Override
    public OpList eval() {
        return PiFunctions.String(value);
    }

}

class CharStmnt extends ASTLeaf {
    private final char value;

    public CharStmnt(final char value) {
        super();
        this.value = value;
    }

    @Override
    public OpList eval() {
        return PiFunctions.Char(value);
    }

}

class BracketStmnt extends ASTLeaf {
    private final List<List<Character>> tuplelist; // タプルのリスト(内側のリストは要素数二つに限る)

    public BracketStmnt(final List<List<Character>> tuplelist) {
        super();
        this.tuplelist = tuplelist;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (final var pair : tuplelist)
            sb.append("(" + pair.get(0) + "," + pair.get(1) + ")");
        sb.append("]");
        return sb.toString();
    }

    @Override
    public OpList eval() throws RuntimeException {

        var charlist = new ArrayList<Character>();
        for (var tuple : tuplelist)
            for (char c = tuple.get(0); c <= tuple.get(1); c++)
                charlist.add(c);

        return PiFunctions.Range(charlist);
    }
}

class NonTerminationStmnt extends ASTLeaf {
    String name;

    public NonTerminationStmnt(final String name) {
        super();
        this.name = name;
    }

    @Override
    public OpList eval() {
        return PiFunctions.NT(name);
    }
}

class EmptyStmnt extends ASTLeaf {

    public EmptyStmnt() {
        super();
    }

    @Override
    public OpList eval() {
        return new OpList(); // 空
    }
}

class DotStmnt extends ASTLeaf {
    public DotStmnt() {
        super();
    }

    @Override
    public OpList eval() {
        return PiFunctions.Any();
    }
}