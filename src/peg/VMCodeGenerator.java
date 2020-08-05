package peg;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import consts.OpCodes;
import sun.awt.www.content.audio.x_aiff;

public class VMCodeGenerator {
    // PEG規則を仮想マシンコードに変換する。
    // インスタンスの作成を禁止: staticクラス
    private VMCodeGenerator() {
    };

    private static int ip;

    public static byte[] generate(String input) throws SyntaxError {

        // PEG構文木生成
        // ASTree grammer = Grammer(pegGrammers);
        ASTree tree = Grammer(input);

        return null;

    }

    private static ASTree Grammer(String input) throws SyntaxError {

        ASTree difinition;
        List<ASTree> list = new ArrayList<ASTree>();

        Spacing();
        difinition = Difinition();
        list.add(difinition);

        try {
            while (true) {
                difinition = Difinition();
                list.add(difinition);
            }
        } catch (SyntaxError e) {
        }

        EndOfFile();

    }

    private static void EndOfFile() throws SyntaxError {
    }

    private static void Spacing() {
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
    }

    private static void Space() throws SyntaxError {
        int bip = ip;
        char c = read(ip);

        if (c == ' ') {
            return;
        } else if (c == '\t') {
            return;
        }

        ip = bip;
        EndOfLine();
    }

    private static char read(int ip) {
        return 0;
    }

    private static void EndOfLine() throws SyntaxError {
        int bip = ip;
        char c = read(ip);

        if (c == '\n') {
            return;
        } else if (c == '\n') {
            return;
        } else if (c == '\r') {
            return;
        }

        ip = bip;
        throw new SyntaxError(ip);
    }

    private static ASTree Difinition() throws SyntaxError {
        Identifire();
        LEFTARROW();
        Expression();
    }

    private static ASTree Expression() throws SyntaxError {
        Sequence();
    }

    private static void Sequence() {
    }

    private static ASTree Identifire() throws SyntaxError {
        IdentStart();
        while (true) {
            IdentCont();
        }
        Spacing();
    }

    private static char IdentCont() throws SyntaxError {
        try {
            return IdentStart();
        } catch (SyntaxError e) {
            Range('0', '9');
        }
    }

    private static char Range(char start, char end) throws SyntaxError {
        int bip = ip;
        char ic = read(ip);

        for (char c = start; c != end; c++) {
            if (c == ip)
                return c;
        }

        ip = bip;
        throw new SyntaxError(ip);
    }

    private static char IdentStart() {

        Range('a', 'z');
        Range('A', 'Z');
        Range('_', '_');

    }
}