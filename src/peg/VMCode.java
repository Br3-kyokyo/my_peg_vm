package peg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VMCode {

    private int ip; // input position - 行における位置
    private int lp; // line position - 現在の行番号
    private String line;
    private List<String> input;
    private boolean packrat;

    private OpList bodycode;

    public VMCode(List<String> input, boolean packratparsing) {
        this.input = input;
        this.packrat = packratparsing;

        try {
            ASTree tree = Grammer();
            OpList oplist = tree.eval(new ParsingEnv(tree, packrat));

            System.out.println(tree.toString());

            this.bodycode = oplist;
        } catch (SyntaxError e) {
            System.out.println(lp + 1 + ":" + ip + ": SyntaxError");
            System.out.println(input.get(lp));

            for (int i = 0; i < input.size(); i++) {
                if (i == ip)
                    System.out.print("^");
                else
                    System.out.print(" ");
            }
            System.exit(-1);
        } catch (RuntimeException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public OpList body() {
        return bodycode; // unreachable (なはず…)
    }

    public OpList header() {
        OpList header = new OpList();
        if (packrat) {
            header.addOpcode((byte) 0x01);
            header.addOperand(bodycode.NTaddressMap.size());
        } else {
            header.addOpcode((byte) 0x00);
        }

        return header;
    }

    private ASTree Grammer() throws SyntaxError {

        List<ASTree> list = new ArrayList<ASTree>();

        for (lp = 0; lp < input.size(); lp++) {
            ip = 0;
            this.line = input.get(lp);

            int bip = ip;
            try {
                Spacing();
                EOL();
            } catch (SyntaxError e) {
                ip = bip;
                list.add(Difinition());
                EOL();
            }
        }
        return new GrammerStmnt(list);
    }

    private ASTree Difinition() throws SyntaxError {
        IdentifireStmnt id = (IdentifireStmnt) Identifire();
        LEFTARROW();
        ASTree expr = Expression();
        return new RuleStmnt(id.name, expr);
    }

    private ASTree Expression() throws SyntaxError {
        ASTree left = Sequence();
        try {
            SLASH();
        } catch (SyntaxError e) {
            return left;
        }
        ASTree right = Expression();
        return new ChoiceStmnt(Arrays.asList(left, right));
    }

    private ASTree Sequence() throws SyntaxError {
        ASTree left = Prefix();

        try {
            ASTree right = Sequence();
            return new SequenceStmnt(Arrays.asList(left, right));
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

        PrimaryStmnt pe = Suffix();
        if (mod != null)
            pe.modifire = mod;

        return pe;
    }

    private PrimaryStmnt Suffix() throws SyntaxError {
        PrimaryStmnt pe = Primary();
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

    private PrimaryStmnt Primary() throws SyntaxError {
        int bip = ip;

        try {
            ASTree identifire = Identifire();

            int bpredicateip = 0;
            try {
                bpredicateip = ip;
                LEFTARROW();
            } catch (SyntaxError e) {
                ip = bpredicateip;
                return new PrimaryStmnt(identifire, Modifire.none);
            }
            throw new SyntaxError(ip);
        } catch (SyntaxError e) {
            ip = bip;
        }

        try {
            OPEN();
            ASTree tree = Expression();
            CLOSE();
            return new PrimaryStmnt(tree, Modifire.none);
        } catch (SyntaxError e) {
            ip = bip;
        }

        try {
            ASTree cliteral = CharLiteral();
            return new PrimaryStmnt(cliteral, Modifire.none);
        } catch (SyntaxError e) {
            ip = bip;
        }

        try {
            StringStmnt sliteral = StringLiteral();
            return new PrimaryStmnt(sliteral, Modifire.none);
        } catch (SyntaxError e) {
            ip = bip;
        }

        try {
            BracketStmnt range = Class();
            return new PrimaryStmnt(range, Modifire.none);
        } catch (SyntaxError e) {
            ip = bip;
        }

        ASTree dot = DOT();
        return new PrimaryStmnt(dot, Modifire.none);

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
        return new IdentifireStmnt(sb.toString());
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
                sb.append(Char());
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
            sb.append(readRange(Arrays.asList('f', 'n', 'r', 't', '\'', '"', '[', ']', '\\', '-')));
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
            return line.charAt(ip++);
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
                EOL();
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
            readChar('\t');
        }
    }

    private void EOL() throws SyntaxError {
        // 改行文字がない場合
        if (line.length() == ip)
            return;

        // 改行文字がある場合
        try {
            readChar('\r');
            readChar('\n');
        } catch (SyntaxError e) {
        }
        try {
            readChar('\n');
        } catch (SyntaxError e2) {
            readChar('\r');
        }
    }

    private void EndOfFile() throws SyntaxError {
        if (line.length() == ip)
            return;
        throw new SyntaxError(ip);
    }

    private char readRange(char start, char end) throws SyntaxError {
        if (line.length() == ip)
            throw new SyntaxError(ip);
        char ic = line.charAt(ip);

        for (char c = start; c <= end; c++) {
            if (c == ic) {
                ip++;
                return c;
            }
        }

        throw new SyntaxError(ip);
    }

    private char readRange(List<Character> list) throws SyntaxError {
        if (line.length() == ip)
            throw new SyntaxError(ip);
        char ic = line.charAt(ip);

        for (char c : list) {
            if (c == ic) {
                ip++;
                return c;
            }
        }

        throw new SyntaxError(ip);
    }

    private char readChar(char c) throws SyntaxError {
        if (line.length() == ip)
            throw new SyntaxError(ip);

        char ic = line.charAt(ip);
        if (ic == c) {
            ip++;
            return c;
        } else {
            throw new SyntaxError(ip);
        }
    }

    private boolean peekChar(int offset, char c) {
        char ic = line.charAt(ip + offset);
        if (ic == c) {
            return true;
        } else {
            return false;
        }
    }

}