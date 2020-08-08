package peg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VMCodeGenerator {

    private int ip;
    private String input;

    public VMCodeGenerator(String input) {
        ip = 0;
        this.input = input;
    }

    public OpList generate() throws SyntaxError {
        ASTree tree = Grammer(input);
        OpList oplist = tree.eval();
        return oplist;
    }

    private ASTree Grammer(String input) throws SyntaxError {

        List<ASTree> list = new ArrayList<ASTree>();

        Spacing();
        list.add(Difinition());
        try {
            while (true)
                list.add(Difinition());
        } catch (SyntaxError e) {
        }

        try {
            EndOfFile();
        } catch (SyntaxError e) {
            System.out.println("Syntax error.");
        }

        return new GrammerStmnt(list);
    }

    private ASTree Difinition() throws SyntaxError {
        ASTree id = Identifire();
        LEFTARROW();
        ASTree expr = Expression();
        return new DifinitionStmnt(Arrays.asList(id, expr));
    }

    private ASTree Expression() throws SyntaxError {
        ASTree left = Sequence();
        try {
            SLASH();
        } catch (SyntaxError e) {
            return left;
        }
        ASTree right = Expression();
        return new PEChoiceStmnt(Arrays.asList(left, right));
    }

    private ASTree Sequence() throws SyntaxError {
        ASTree left = null;

        try {
            left = Prefix();
        } catch (SyntaxError e) {
            return new EmptyStmnt();
        }

        try {
            ASTree right = Sequence();
            return new PESequenceStmnt(Arrays.asList(left, right));
        } catch (SyntaxError e) {
            return left;
        }
    }

    private ASTree Prefix() throws SyntaxError {
        Modifire mod = null;

        try {
            AND();
            mod = Modifire.amplifire;
        } catch (SyntaxError e) {
        }

        try {
            NOT();
            mod = Modifire.exclamation;
        } catch (SyntaxError e) {
        }

        ParsingExpression pe = Suffix();
        if (mod != null)
            pe.modifire = mod;

        return pe;
    }

    private ParsingExpression Suffix() throws SyntaxError {
        ParsingExpression pe = Primary();
        try {
            QUESTION();
            pe.modifire = Modifire.question;
            return pe;
        } catch (SyntaxError e) {
        }

        try {
            STAR();
            pe.modifire = Modifire.asterisk;
            return pe;
        } catch (SyntaxError e) {
        }

        try {
            PLUS();
            pe.modifire = Modifire.plus;
            return pe;
        } catch (SyntaxError e) {
        }

        return pe;

    }

    private ParsingExpression Primary() throws SyntaxError {
        int bip = ip;

        try {
            ASTree identifire = Identifire();

            int bpredicateip = 0;
            try {
                bpredicateip = ip;
                LEFTARROW();
            } catch (SyntaxError e) {
                ip = bpredicateip;
                return new ParsingExpression(identifire, Modifire.none);
            }
            throw new SyntaxError(ip);
        } catch (SyntaxError e) {
            ip = bip;
        }

        try {
            OPEN();
            ASTree tree = Expression();
            CLOSE();
            return new ParsingExpression(tree, Modifire.none);
        } catch (SyntaxError e) {
            ip = bip;
        }

        try {
            ASTree cliteral = CharLiteral();
            return new ParsingExpression(cliteral, Modifire.none);
        } catch (SyntaxError e) {
            ip = bip;
        }

        try {
            StringStmnt sliteral = StringLiteral();
            return new ParsingExpression(sliteral, Modifire.none);
        } catch (SyntaxError e) {
            ip = bip;
        }

        try {
            BracketStmnt range = Class();
            return new ParsingExpression(range, Modifire.none);
        } catch (SyntaxError e) {
            ip = bip;
        }

        ASTree dot = DOT();
        return new ParsingExpression(dot, Modifire.none);

    }

    // * Lexical Syntax *//
    private ASTree Identifire() throws SyntaxError {
        StringBuilder sb = new StringBuilder();
        sb.append(IdentStart());
        try {
            while (true)
                sb.append(IdentCont());
        } catch (SyntaxError e) {
        }
        Spacing();
        return new NonTerminationStmnt(sb.toString());
    }

    private char IdentStart() throws SyntaxError {
        try {
            return readRange('a', 'z');
        } catch (SyntaxError e) {
            try {
                return readRange('A', 'Z');
            } catch (SyntaxError e2) {
                return readRange('_', '_');
            }
        }
    }

    private char IdentCont() throws SyntaxError {
        try {
            return IdentStart();
        } catch (SyntaxError e) {
            return readRange('0', '9');
        }
    }

    private ASTree CharLiteral() throws SyntaxError {
        readChar('\'');
        if (peekChar(0, '\'')) {
            readChar('\'');
            Spacing();
            return new EmptyStmnt();
        } else {
            char c = Char();
            readChar('\'');
            Spacing();
            return new CharStmnt(c);
        }
    }

    private StringStmnt StringLiteral() throws SyntaxError {
        StringBuilder sb = new StringBuilder();
        readChar('"');
        while (true) {
            if (peekChar(0, '"')) {
                break;
            } else {
                sb.append(input.charAt(ip++));
            }
        }
        readChar('"');
        Spacing();
        return new StringStmnt(sb.toString());
    }

    private BracketStmnt Class() throws SyntaxError {
        List<List<Character>> tuplelist = new ArrayList<List<Character>>(); // タプルのリスト(内側のリストは要素数2)

        readChar('[');
        while (true) {
            if (peekChar(0, ']')) {
                break;
            } else {
                List<Character> ctuple = Range();
                tuplelist.add(ctuple);
            }
        }
        readChar(']');
        Spacing();

        return new BracketStmnt(tuplelist);
    }

    private List<Character> Range() throws SyntaxError {
        char c1 = Char();
        if (peekChar(0, '-')) {
            readChar('-');
            char c2 = Char();
            return Arrays.asList(c1, c2);
        } else {
            return Arrays.asList(c1, c1);
        }
    }

    private char Char() throws SyntaxError {
        int bip = ip;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(readChar('\\'));
            sb.append(readRange(Arrays.asList('n', 'r', 't', '\'', '"', '[', ']', '\\')));
            return Functions.unescape_perl_string(sb.toString()).charAt(0);
        } catch (SyntaxError e) {
            ip = bip;
        }

        try {
            StringBuilder sb = new StringBuilder();
            sb.append(readChar('\\'));
            sb.append(readChar('x'));
            sb.append(readRange('0', 'f'));
            sb.append(readRange('0', 'f'));
            return Functions.unescape_perl_string(sb.toString()).charAt(0);
        } catch (SyntaxError e) {
            ip = bip;
        }

        if (peekChar(0, '\\')) {
            throw new SyntaxError(ip);
        } else {
            return input.charAt(ip++);
        }
    }

    private void LEFTARROW() throws SyntaxError {
        readChar('<');
        readChar('-');
        Spacing();
    }

    private void SLASH() throws SyntaxError {
        readChar('/');
        Spacing();
    }

    private void AND() throws SyntaxError {
        readChar('&');
        Spacing();
    }

    private void NOT() throws SyntaxError {
        readChar('!');
        Spacing();
    }

    private void QUESTION() throws SyntaxError {
        readChar('?');
        Spacing();
    }

    private void STAR() throws SyntaxError {
        readChar('*');
        Spacing();
    }

    private void PLUS() throws SyntaxError {
        readChar('+');
        Spacing();
    }

    private void OPEN() throws SyntaxError {
        readChar('(');
        Spacing();
    }

    private void CLOSE() throws SyntaxError {
        readChar(')');
        Spacing();
    }

    private ASTree DOT() throws SyntaxError {
        readChar('.');
        Spacing();
        return new DotStmnt();
    }

    private void Spacing() {
        // Method space = thisclass.getMethod("Space");
        // Method comment = thisclass.getMethod("Comment");
        // space.
        // space.set
        // Or(Arrays.asList(space, comment);

        try {
            while (true) {
                try {
                    Space();
                } catch (SyntaxError e) {
                    Comment();
                }
            }
        } catch (SyntaxError e) {
        }
    }

    private void Comment() throws SyntaxError {
        readChar('#');
        while (true) {
            try {
                EndOfLine();
                break;
            } catch (Exception e) {
                ip++;
            }
        }
    }

    private void Space() throws SyntaxError {
        try {
            readChar(' ');
        } catch (SyntaxError e) {
            try {
                readChar('\t');
            } catch (SyntaxError e2) {
                EndOfLine();
            }
        }
    }

    private void EndOfLine() throws SyntaxError {
        try {
            readChar('\r');
            readChar('\n');
        } catch (SyntaxError e) {
            try {
                readChar('\n');
            } catch (SyntaxError e2) {
                readChar('\r');
            }
        }
    }

    private void EndOfFile() throws SyntaxError {
        if (input.length() == ip)
            return;
        throw new SyntaxError(ip);
    }

    private char readRange(char start, char end) throws SyntaxError {
        if (input.length() == ip)
            throw new SyntaxError(ip);
        char ic = input.charAt(ip);

        for (char c = start; c <= end; c++) {
            if (c == ic) {
                ip++;
                return c;
            }
        }

        throw new SyntaxError(ip);
    }

    private char readRange(List<Character> list) throws SyntaxError {
        if (input.length() == ip)
            throw new SyntaxError(ip);
        char ic = input.charAt(ip);

        for (char c : list) {
            if (c == ic) {
                ip++;
                return c;
            }
        }

        throw new SyntaxError(ip);
    }

    private char readChar(char c) throws SyntaxError {
        if (input.length() == ip)
            throw new SyntaxError(ip);

        char ic = input.charAt(ip);
        if (ic == c) {
            ip++;
            return c;
        } else {
            throw new SyntaxError(ip);
        }
    }

    private boolean peekChar(int offset, char c) {
        char ic = input.charAt(ip + offset);
        if (ic == c) {
            return true;
        } else {
            return false;
        }
    }

}