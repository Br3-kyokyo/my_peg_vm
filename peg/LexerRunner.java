package peg;

public class LexerRunner {
    public static void main(String[] args) {
        var lexer = new Lexer("’(’ ’postfix’ NUM COMMAND* ’)’");

        Token t;
        try {
            while (!((t = lexer.read()) instanceof EOLToken)) {
                System.out.println(t.toString());
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}