import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;

import peg.VMCodeGenerator;

public class PEGCompiler {
    // ファイルからデータを読み取って、仮想マシンコードを返す。
    public static void main(String[] args) {

        Path peg_filepath;
        try {
            peg_filepath = Path.of(args[0]);
        } catch (InvalidPathException e) {
            System.out.println(e.toString());
            return;
        }

        try {
            List<String> peg_grammers = Files.readAllLines(peg_filepath, StandardCharsets.UTF_8);
            byte[] vmcode = VMCodeGenerator.generate(peg_grammers);

            for (int i = 0; i < vmcode.length; i++)
                System.out.print(String.format("%02X", vmcode[i]) + " ");

        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }
}