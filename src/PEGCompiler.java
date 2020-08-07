import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import peg.VMCodeGenerator;

public class PEGCompiler {
    // ファイルからデータを読み取って、仮想マシンコードを返す。
    public static void main(String[] args) {

        try {
            String input = readTextFromFileAll(args[0]);
            byte[] vmcode = VMCodeGenerator.generate(input);

            FileOutputStream fos = new FileOutputStream("vmcode.bin");
            fos.write(vmcode);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
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