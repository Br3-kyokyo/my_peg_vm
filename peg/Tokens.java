package peg;

public class Token {
    private final String value;

    public Token(final String v) {
        this.value = v;
    }

    public String toString() {
        return value;
    }
}

class StringToken extends Token {

    public StringToken(final String v) {
        super(v);
        // TODO Auto-generated constructor stub
    }

}

class SQuotationToken extends Token {

    public SQuotationToken() {
        super("'");
    }
}

class SlashToken extends Token {
    public SlashToken() {
        super("/");
    }
}

class RparenToken extends Token {

    public RparenToken() {
        super(")");
        // TODO Auto-generated constructor stub
    }

}

class RbracketToken extends Token {

    public RbracketToken() {
        super("]");
    }

}

class NumToken extends Token {
    private int value;

    public NumToken(int i) {
        super(Integer.toString(i));
        this.value = i;
        // TODO Auto-generated constructor stub
    }

    public int getValue() {
        return value;
    }

}

class NTNameToken extends Token {

    public NTNameToken(final String v) {
        super(v);
        // TODO Auto-generated constructor stub
    }

}

class ModifireToken extends Token {

    Modifire modifire;

    public ModifireToken(Modifire modifire) {
        super(modifire.toString());
        this.modifire = modifire;
    }
}

class LparenToken extends Token {

    public LparenToken() {
        super("(");
        // TODO Auto-generated constructor stub
    }

}

class LeftArrowToken extends Token {

    public LeftArrowToken() {
        super("<-");
        // TODO Auto-generated constructor stub
    }

}

class LbracketToken extends Token {

    public LbracketToken() {
        super("[");
    }
}

class HyphenToken extends Token {

    public HyphenToken() {
        super("-");
        // TODO Auto-generated constructor stub
    }

}

class EOLToken extends Token {

    public EOLToken() {
        super("EOL");
        // TODO Auto-generated constructor stub
    }

}

class EmptyToken extends Token {

    public EmptyToken() {
        super("ε");
        // TODO Auto-generated constructor stub
    }

}

class DQuotationToken extends Token {

    public DQuotationToken() {
        super("\"");
    }
}

class DotToken extends Token {

    public DotToken() {
        super(".");
        // TODO Auto-generated constructor stub
    }

}

class CharToken extends Token {

    private final char value;

    public CharToken(final char c) {
        super(Character.toString(c));
        this.value = c;
        // TODO Auto-generated constructor stub
    }

    public char getValue() {
        return value;
    }

}