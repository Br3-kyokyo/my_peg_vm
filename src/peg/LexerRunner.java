package peg;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;

public class LexerRunner {
    public static void main(String[] args) {

        Path peg_filepath;
        List<String> peg_grammers = null;
        try {
            peg_filepath = Path.of("json.peg");
            peg_grammers = Files.readAllLines(peg_filepath, StandardCharsets.UTF_8);
        } catch (InvalidPathException e) {
            System.out.println(e.toString());
            return;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        for (var grammer : peg_grammers) {

            try {
                Token t;
                var lexer = new Lexer(grammer);
                while (!((t = lexer.read()) instanceof EOLToken))
                    System.out.println(t.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}