package peg;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {

    String exp = "(<-)|\\'(\\\\\\'|[^\\']|\\\\x[0-f]{2})\\'|\\\"((?:\\\\\\\"|[^\\\"])*)\\\"|([a-zA-Z_][a-zA-Z_0-9]*)|(ε)|(\\p{Punct})|([0-9]+)";
    private Matcher matcher;

    private Queue<Token> tokens = new ArrayDeque<Token>();

    public Lexer(String grammer) throws ParseException {
        this.matcher = Pattern.compile(exp).matcher(grammer);

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                tokens.add(new LeftArrowToken());
            } else if (matcher.group(2) != null) {

                tokens.add(charToken());
            } else if (matcher.group(3) != null) {
                tokens.add(new StringToken(matcher.group(3)));
            } else if (matcher.group(4) != null) {
                tokens.add(new NTNameToken(matcher.group(4)));
            } else if (matcher.group(5) != null) {
                tokens.add(new EmptyToken());
            } else if (matcher.group(6) != null) {
                tokens.add(punctToken());
            } else if (matcher.group(7) != null) {
                tokens.add(new NumToken(Integer.parseInt(matcher.group(7))));
            } else {
                throw new ParseException("呼ばれるはずのない分岐");
            }
        }
        tokens.add(new EOLToken());
    }

    private Token charToken() {
        String match = matcher.group(2);
        char c;
        if(match.charAt(0) == '\\'){
            c = 
        }else{
            c = match.charAt(index)
        }

        if()

        return new CharToken(.charAt(0));
    }

    public Token read() {
        return tokens.remove();
    }

    public Token peek() {
        return tokens.peek();
    }

    private Token punctToken() throws ParseException {
        switch (matcher.group(6)) {
            case "(":
                return new LparenToken();
            case ")":
                return new RparenToken();
            case "[":
                return new LbracketToken();
            case "]":
                return new RbracketToken();
            case "\"":
                return new DQuotationToken();
            case "'":
                return new SQuotationToken();
            case "&":
                return new ModifireToken(Modifire.amplifire);
            case "?":
                return new ModifireToken(Modifire.question);
            case "!":
                return new ModifireToken(Modifire.exclamation);
            case "+":
                return new ModifireToken(Modifire.plus);
            case "*":
                return new ModifireToken(Modifire.asterisk);
            case ".":
                return new DotToken();
            case "-":
                return new HyphenToken();
            case "/":
                return new SlashToken();
        }
        throw new ParseException("unknown punctToken");
    }
}