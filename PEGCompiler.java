import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;

public class PEGCompiler {
    // ファイルからデータを読み取って、仮想マシンコードを返す。
    public static void main(String[] args) {

        Path peg_filepath;
        try {
            peg_filepath = Path.of(args[1]);
        } catch (InvalidPathException e) {
            System.out.println(e.toString());
            return;
        }

        try {
            List<String> peg_grammers = Files.readAllLines(peg_filepath, StandardCharsets.UTF_8);
            String vmcode = VMCodeGenarator.generate(peg_grammers);
        } catch (IOException e) {
            System.out.println(e.toString());
            return;
        } catch (Exception e) {
            System.out.println(e.toString());
            return;
        }
    }
}