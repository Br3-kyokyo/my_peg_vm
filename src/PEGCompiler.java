import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import peg.OpList;
import peg.VMCodeGenerator;

public class PEGCompiler {
    // ファイルからデータを読み取って、仮想マシンコードを返す。
    public static void main(String[] args) {

        try {

            // String input = readTextFromFileAll(args[0]);
            List<String> input = Files.readAllLines(Path.of(args[0]), StandardCharsets.UTF_8);

            VMCodeGenerator vmcodeGenerator = new VMCodeGenerator(input);
            OpList oplist = vmcodeGenerator.generate();

            outputBinaryCode(oplist.toBinary(), "vmcode.bin");
            outputMetaData(oplist.toString(), "vmcode.meta");

        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private static void outputBinaryCode(byte[] vmcode_byte, String filename) throws IOException {
        FileOutputStream fos = new FileOutputStream(filename);
        fos.write(vmcode_byte);
        fos.close();
    }

    private static void outputMetaData(String string, String filename) throws IOException {
        FileWriter fw = new FileWriter(filename);
        fw.write(string);
        fw.close();
    }

    private static String readTextFromFileAll(String path) throws IOException {
        File f = new File(path);
        byte[] data = new byte[(int) f.length()];
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
        bis.read(data);
        bis.close();
        String fs = new String(data, "utf-8");
        return fs;
    }
}