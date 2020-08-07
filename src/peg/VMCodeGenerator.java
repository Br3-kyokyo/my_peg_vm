package peg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import consts.OpCodes;

public class VMCodeGenerator {
    // PEG規則を仮想マシンコードに変換する。
    // インスタンスの作成を禁止: staticクラス
    private VMCodeGenerator() {
    };

    private static int ip;
    private static String input;

    public static byte[] generate(String _input) throws SyntaxError {

        // PEG構文木生成
        // ASTree grammer = Grammer(pegGrammers);
        ip = 0;
        input = _input;

        ASTree tree = Grammer(input);
        OpList oplist = tree.eval();
        return oplist.toArray();
    }

    private static ASTree Grammer(String input) throws SyntaxError {

        List<ASTree> list = new ArrayList<ASTree>();

        Spacing();
        list.add(Difinition());
        try {
            while (true)
                list.add(Difinition());
        } catch (SyntaxError e) {
        }
        EndOfFile();

        return new GrammerStmnt(list);
    }

    private static ASTree Difinition() throws SyntaxError {
        ASTree id = Identifire();
        LEFTARROW();
        ASTree expr = Expression();
        return new DifinitionStmnt(Arrays.asList(id, expr));
    }

    private static ASTree Expression() throws SyntaxError {
        ASTree left = Sequence();
        try {
            SLASH();
        } catch (Exception e) {
            return left;
        }
        ASTree right = Expression();
        return new PEChoiceStmnt(Arrays.asList(left, right));
    }

    private static ASTree Sequence() throws SyntaxError {
        ASTree left = Prefix();
        try {
            ASTree right = Sequence();
            return new PESequenceStmnt(Arrays.asList(left, right));
        } catch (Exception e) {
            return left;
        }
    }

    private static ASTree Prefix() throws SyntaxError {
        Modifire mod = null;
        try {
            try {
                AND();
                mod = Modifire.amplifire;
            } catch (SyntaxError e) {
                NOT();
                mod = Modifire.exclamation;
            }
        } catch (Exception e) {
        }

        ParsingExpression pe = Suffix();
        if (mod != null)
            pe.modifire = mod;

        return pe;
    }

    private static ParsingExpression Suffix() throws SyntaxError {
        ParsingExpression pe = Primary();
        try {
            try {
                QUESTION();
                pe.modifire = Modifire.question;
                return pe;
            } catch (SyntaxError e) {
                try {
                    STAR();
                    pe.modifire = Modifire.asterisk;
                    return pe;
                } catch (Exception e2) {
                    PLUS();
                    pe.modifire = Modifire.plus;
                    return pe;
                }
            }
        } catch (SyntaxError e) {
            return pe;
        }
    }

    private static ParsingExpression Primary() throws SyntaxError {

        try {
            int bip = ip;
            int bpredicateip = 0;

            ASTree identifire = Identifire();
            try {
                bpredicateip = ip;
                LEFTARROW();
            } catch (SyntaxError e) {
                ip = bpredicateip;
                return new ParsingExpression(identifire, Modifire.none);
            }

            ip = bip;
            throw new SyntaxError(ip);

        } catch (SyntaxError e) {
            try {
                OPEN();
                ASTree tree = Expression();
                CLOSE();
                return new ParsingExpression(tree, Modifire.none);
            } catch (Exception e2) {
                try {
                    ASTree cliteral = CharLiteral();
                    return new ParsingExpression(cliteral, Modifire.none);
                } catch (Exception e3) {
                    try {
                        StringStmnt sliteral = StringLiteral();
                        return new ParsingExpression(sliteral, Modifire.none);
                    } catch (Exception e5) {
                        try {
                            BracketStmnt range = Class();
                            return new ParsingExpression(range, Modifire.none);
                        } catch (Exception e4) {
                            ASTree dot = DOT();
                            return new ParsingExpression(dot, Modifire.none);
                        }
                    }

                }
            }
        }

    }

    // * Lexical Syntax *//
    private static ASTree Identifire() throws SyntaxError {
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

    private static char IdentStart() throws SyntaxError {
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

    private static char IdentCont() throws SyntaxError {
        try {
            return IdentStart();
        } catch (SyntaxError e) {
            return readRange('0', '9');
        }
    }

    private static ASTree CharLiteral() throws SyntaxError {
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

    private static StringStmnt StringLiteral() throws SyntaxError {
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

    private static BracketStmnt Class() throws SyntaxError {
        List<List<Character>> tuplelist = new ArrayList<List<Character>>(); // タプルのリスト(内側のリストは要素数二つに限る)

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

    private static List<Character> Range() throws SyntaxError {
        char c1 = Char();
        if (peekChar(0, '-')) {
            readChar('-');
            char c2 = Char();
            return Arrays.asList(c1, c2);
        } else {
            return Arrays.asList(c1, c1);
        }
    }

    private static char Char() throws SyntaxError {
        int bip = ip;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(readChar('\\'));
            sb.append(readRange(Arrays.asList('n', 'r', 't', '\'', '"', '[', ']', '\\')));
            return Functions.unescape_perl_string(sb.toString()).charAt(0);
        } catch (Exception e) {
            try {
                ip = bip;
                StringBuilder sb = new StringBuilder();
                sb.append(readChar('\\'));
                sb.append(readChar('x'));
                sb.append(readRange('0', 'f'));
                sb.append(readRange('0', 'f'));
                return Functions.unescape_perl_string(sb.toString()).charAt(0);
            } catch (Exception e2) {
                ip = bip;
                if (peekChar(0, '\\')) {
                    throw new SyntaxError(ip);
                } else {
                    return input.charAt(ip++);
                }
            }
        }
    }

    private static void LEFTARROW() throws SyntaxError {
        readChar('<');
        readChar('-');
        Spacing();
    }

    private static void SLASH() throws SyntaxError {
        readChar('/');
        Spacing();
    }

    private static void AND() throws SyntaxError {
        readChar('&');
        Spacing();
    }

    private static void NOT() throws SyntaxError {
        readChar('!');
        Spacing();
    }

    private static void QUESTION() throws SyntaxError {
        readChar('?');
        Spacing();
    }

    private static void STAR() throws SyntaxError {
        readChar('*');
        Spacing();
    }

    private static void PLUS() throws SyntaxError {
        readChar('+');
        Spacing();
    }

    private static void OPEN() throws SyntaxError {
        readChar('(');
        Spacing();
    }

    private static void CLOSE() throws SyntaxError {
        readChar(')');
        Spacing();
    }

    private static ASTree DOT() throws SyntaxError {
        readChar('.');
        Spacing();
        return new DotStmnt();
    }

    private static void Spacing() {
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

    private static void Comment() throws SyntaxError {
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

    private static void Space() throws SyntaxError {
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

    private static void EndOfLine() throws SyntaxError {
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

    private static void EndOfFile() throws SyntaxError {
        if (input.length() == ip)
            return;
        throw new SyntaxError(ip);
    }

    private static char readRange(char start, char end) throws SyntaxError {
        if (input.length() == ip)
            throw new SyntaxError(ip);
        char ic = input.charAt(ip);

        for (char c = start; c != end; c++) {
            if (c == ic) {
                ip++;
                return c;
            }
        }

        throw new SyntaxError(ip);
    }

    private static char readRange(List<Character> list) throws SyntaxError {
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

    private static char readChar(char c) throws SyntaxError {
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

    private static boolean peekChar(int offset, char c) {
        char ic = input.charAt(ip + offset);
        if (ic == c) {
            return true;
        } else {
            return false;
        }
    }

}