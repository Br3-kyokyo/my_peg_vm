package peg;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class VMCodeGenerator {
    // PEG規則を仮想マシンコードに変換する。

    private static Lexer lexer;

    // インスタンスの作成を禁止: staticクラス
    private VMCodeGenerator() {
    };

    public static byte[] generate(List<String> pegGrammers) throws ParseException {

        // PEG構文木生成
        List<Rule> rule_list = ruleList(pegGrammers);

        // 構文木から仮想マシンコードを生成
        var oplist = new OpList();
        for (Rule rule : rule_list) {
            OpList.NTaddressMap.put(rule.getNt().name, oplist.size());
            oplist.addOpblock(rule.getBody().eval());
        }

//        var vmcode = oplist.toArray();
//        for (int i = 0; i < vmcode.length; i++)
//        	System.out.print(String.format("%02X", vmcode[i]) + " ");
//        System.out.println();

        oplist = PiFunctions.ReplaceCallNtAddr(oplist);

        return oplist.toArray();
    }

    private static List<Rule> ruleList(List<String> pegGrammers) throws ParseException {

        LinkedList<Rule> grammers = new LinkedList<Rule>();
        for (String pegGrammer : pegGrammers) {
            lexer = new Lexer(pegGrammer);
            grammers.add(rule(pegGrammer));
        }
        return grammers;
    }

    private static Rule rule(String pegGrammer) throws ParseException {

        var leftterm = lexer.read();
        var leftarrow = lexer.read();

        if (!(leftterm instanceof NTNameToken || leftarrow instanceof LeftArrowToken))
            throw new ParseException();

        var nt = new RuleNT(leftterm.toString());
        var rulebody = ParsingExpressionOr();

        return new Rule(nt, rulebody);
    }

    private static ASTree ParsingExpressionOr() throws ParseException {

        var left = ParsingExpressionSeq();
        if (lexer.peek() instanceof SlashToken) {
            lexer.read();
            var right = ParsingExpressionOr();
            return new ParsingExpressionOr(Arrays.asList(left, right));
        } else {
            return left;
        }
    }

    private static ASTree ParsingExpressionSeq() throws ParseException {
        var left = ParsingExpression();
        Token next = lexer.peek();
        if (!(next instanceof SlashToken || next instanceof EOLToken)) {
            var right = ParsingExpressionSeq();
            return new ParsingExpressionSequence(Arrays.asList(left, right));
        }
        return left;
    }

    private static ASTree ParsingExpression() throws ParseException {
        Token t;
        ASTree ParsingExpressionBody;
        Modifire modifire = Modifire.none;

        t = lexer.peek();
        if (t instanceof ModifireToken)
            modifire = PREFIX();

        ParsingExpressionBody = ParsingExpressionBody();

        t = lexer.peek();
        if (t instanceof ModifireToken)
            modifire = SUFFIX();

        return new ParsingExpression(ParsingExpressionBody, modifire);
    }

    private static ASTree ParsingExpressionBody() throws ParseException {
        Token t;
        ASTree parsingExpressionBody;
        t = lexer.peek();
        if (t instanceof LparenToken) {
            lexer.read();
            parsingExpressionBody = ParsingExpressionOr();
            lexer.read();
        } else if (t instanceof StringToken) {
            parsingExpressionBody = String();
        } else if (t instanceof CharToken) {
            parsingExpressionBody = Char();
        } else if (t instanceof LbracketToken) {
            parsingExpressionBody = Bracket();
        } else if (t instanceof NTNameToken) {
            parsingExpressionBody = NT();
        } else if (t instanceof EmptyToken) {
            parsingExpressionBody = EMP();
        } else if (t instanceof DotToken) {
            parsingExpressionBody = DOT();
        } else {
            parsingExpressionBody = null;
        }
        return parsingExpressionBody;
    }

    private static Modifire SUFFIX() throws ParseException {
        ModifireToken mt = (ModifireToken) lexer.read();
        if (mt.modifire != Modifire.plus && mt.modifire != Modifire.asterisk && mt.modifire != Modifire.question)
            throw new ParseException("サフィックスは+か*か?です。");
        return mt.modifire;
    }

    private static Modifire PREFIX() throws ParseException {
        ModifireToken mt = (ModifireToken) lexer.read();
        if (mt.modifire != Modifire.amplifire && mt.modifire != Modifire.exclamation)
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
        lexer.read(); // rbracket
        return new BracketStmnt(list);
    }

    private static ASTree Char() {
        char c = lexer.read().toString().charAt(0);
        return new CharStmnt(c);
    }

    private static ASTree String() {
        ASTree tree = new StringStmnt(lexer.read().toString());
        return tree;
    }
}