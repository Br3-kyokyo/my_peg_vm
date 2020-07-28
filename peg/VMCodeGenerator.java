package peg;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import peg.*;

public class VMCodeGenerator {
    // PEG規則を仮想マシンコードに変換する。

    private static Lexer lexer;

    private static VMCodeGeneratorResult result;

    // インスタンスの作成を禁止: staticクラス
    private VMCodeGenerator() {
    };

    public static String generate(List<String> pegGrammers) throws Exception {

        ASTree tree = grammerList(pegGrammers);
        String vmcode = tree.eval();

        return vmcode;
    }

    private static ASTree grammerList(List<String> pegGrammers) throws ParseException {

        LinkedList<ASTree> grammers = new LinkedList<ASTree>();
        for (String pegGrammer : pegGrammers) {
            lexer = new Lexer(pegGrammer);
            grammers.add(grammer(pegGrammer));
        }
        return new GrammerList(grammers);
    }

    private static ASTree grammer(String pegGrammer) throws ParseException {

        var leftterm = lexer.read();
        var leftarrow = lexer.read();

        if (!(leftterm instanceof NTNameToken || leftarrow instanceof LeftArrowToken))
            throw new ParseException();

        var lefttermstmnt = new GrammerLeftTermStmnt(leftterm.toString());
        var rightterm = ParsingExpressionOr();

        return new Grammer(Arrays.asList(lefttermstmnt, rightterm));

        // Matcher m = Pattern.compile("(.+)<-(.+)").matcher(pegGrammer);

        // if (m.matches()) {
        // String left_term = m.group(1);
        // String right_term = m.group(2);

        // NTaddressMap.put(left_term, vmcode_sb.length());

        // ParsingExpressionOr();

        // }

    }

    private static ASTree ParsingExpressionOr() throws ParseException {

        LinkedList<ASTree> list = new LinkedList<ASTree>();
        list.add(ParsingExpressionSeq());
        if (lexer.peek() instanceof SlashToken) {
            lexer.read();
            list.add(ParsingExpressionOr());
        }
        return new ParsingExpressionOr(list);
    }

    private static ASTree ParsingExpressionSeq() throws ParseException {
        LinkedList<ASTree> list = new LinkedList<ASTree>();
        list.add(ParsingExpression());
        Token next = lexer.peek();
        if (!(next instanceof SlashToken || next instanceof EOLToken))
            list.add(ParsingExpression());
        return new ParsingExpressionSequence(list);
    }

    private static ASTree ParsingExpression() throws ParseException {
        Token t;
        Modifire modifire = Modifire.none;
        ASTree ParsingExpressionBody;

        t = lexer.peek();
        if (t instanceof ModifireToken)
            modifire = PREFIX();

        t = lexer.peek();
        if (t instanceof LparenToken) {
            lexer.read();
            ParsingExpressionBody = ParsingExpressionOr();
            lexer.read();
        } else if (t instanceof DQuotationToken) {
            ParsingExpressionBody = String();
        } else if (t instanceof SQuotationToken) {
            ParsingExpressionBody = Char();
        } else if (t instanceof LbracketToken) {
            ParsingExpressionBody = Bracket();
        } else if (t instanceof NTNameToken) {
            ParsingExpressionBody = NT();
        } else if (t instanceof EmptyToken) {
            ParsingExpressionBody = EMP();
        } else if (t instanceof DotToken) {
            ParsingExpressionBody = DOT();
        } else {
            ParsingExpressionBody = null;
        }

        t = lexer.peek();
        if (t instanceof ModifireToken)
            modifire = SUFFIX();

        ParsingExpressionBody.modifire = modifire;

        return ParsingExpressionBody;
    }

    private static Modifire SUFFIX() throws ParseException {
        ModifireToken mt = (ModifireToken) lexer.read();
        if (mt.modifire != Modifire.plus || mt.modifire != Modifire.asterisk || mt.modifire != Modifire.question)
            throw new ParseException("サフィックスは&か!です。");
        return mt.modifire;
    }

    private static Modifire PREFIX() throws ParseException {
        ModifireToken mt = (ModifireToken) lexer.read();
        if (mt.modifire != Modifire.amplifire || mt.modifire != Modifire.exclamation)
            throw new ParseException("プレフィックスは&か!です。");
        return mt.modifire;
    }

    private static ASTree DOT() {
        lexer.read();
        return new DotStmnt();
    }

    private static ASTree EMP() {
        lexer.read();
        return new EmptyStmnt();
    }

    private static ASTree NT() {
        return new NonTerminationStmnt(lexer.read().toString());
    }

    private static ASTree Bracket() {
        lexer.read();
        var list = new LinkedList<Tuple<Character, Character>>();
        while (!(lexer.peek() instanceof RbracketToken)) {
            char start = lexer.read().toString().charAt(0);
            lexer.read(); // hyphen
            char end = lexer.read().toString().charAt(0);
            list.add(new Tuple<Character, Character>(start, end));
        }
        return new BracketStmnt(list);
    }

    private static ASTree Char() {
        lexer.read();
        return new CharStmnt(lexer.read().toString().charAt(0));
    }

    private static ASTree String() {
        lexer.read();
        ASTree tree = new StringStmnt(lexer.read().toString());
        lexer.read();
        return tree;
    }

    // private void grammer_(String peg_grammer) {
    // Matcher m = Pattern.compile("(.+)<-(.+)").matcher(peg_grammer);

    // if (m.matches()) {
    // String left_term = m.group(1);
    // String right_term = m.group(2);

    // NTaddressMap.put(left_term, vmcode_sb.length());

    // } else {
    // System.out.println("error: " + peg_grammer);
    // }

    // }

}