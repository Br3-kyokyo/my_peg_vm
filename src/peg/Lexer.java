package peg;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {

    String exp = "(<-)|(/)|(\\&)|(\\!)|(\\?)|(\\*)|(\\+)|(\\()|(\\))|(\\.)|(-)|(\\[)|(\\])|([a-zA-Z_]\\w*)|\\'(\\\\x[0-f]{2}|\\\\\\'|[^\\'])\\'|\\\"((?:\\\\x[0-f]{2}|\\\\\\'|[^\\\"])*)\\\"|(\\\\x[0-f]{2}|\\w)";
    private Matcher matcher;

    private Queue<Token> tokens = new ArrayDeque<Token>();

    public Lexer(String grammer) throws ParseException {
        this.matcher = Pattern.compile(exp).matcher(grammer);

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                tokens.add(new LeftArrowToken());
            } else if (matcher.group(2) != null) {
                tokens.add(new SlashToken());
            } else if (matcher.group(3) != null) {
                tokens.add(new ModifireToken(Modifire.amplifire));
            } else if (matcher.group(4) != null) {
                tokens.add(new ModifireToken(Modifire.exclamation));
            } else if (matcher.group(5) != null) {
                tokens.add(new ModifireToken(Modifire.question));
            } else if (matcher.group(6) != null) {
                tokens.add(new ModifireToken(Modifire.asterisk));
            } else if (matcher.group(7) != null) {
                tokens.add(new ModifireToken(Modifire.plus));
            } else if (matcher.group(8) != null) {
                tokens.add(new LparenToken());
            } else if (matcher.group(9) != null) {
                tokens.add(new RparenToken());
            } else if (matcher.group(10) != null) {
                tokens.add(new DotToken());
            } else if (matcher.group(11) != null) {
                tokens.add(new HyphenToken());
            } else if (matcher.group(12) != null) {
                tokens.add(new LbracketToken());
            } else if (matcher.group(13) != null) {
                tokens.add(new RbracketToken());
            } else if (matcher.group(14) != null) {
                tokens.add(new NTNameToken(matcher.group(14)));
            } else if (matcher.group(15) != null) {
                tokens.add(new CharToken(matcher.group(15).charAt(0)));
            } else if (matcher.group(16) != null) {
                tokens.add(new StringToken(matcher.group(16)));
            } else if (matcher.group(17) != null) {
                tokens.add(new CharToken(matcher.group(17).charAt(0)));
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