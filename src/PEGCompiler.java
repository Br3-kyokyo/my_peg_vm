import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

import peg.OpList;
import peg.VMCode;

public class PEGCompiler {
    // ファイルからデータを読み取って、仮想マシンコードを返す。
    public static void main(String[] args) {

        try {
            boolean packrat = false;

            List<String> input = Files.readAllLines(Path.of(args[0]), StandardCharsets.UTF_8);

            VMCode vmcode = new VMCode(input, packrat);

            OpList oplist = new OpList();
            oplist.addOpblock(vmcode.header());
            oplist.addOpblock(vmcode.body());

            outputFile(oplist.toBinary(), "vmcode.bin");
            outputFile(oplist.toString(), "vmcode.meta");
            outputFile(mapToCSVStr(oplist.NTaddressMap), "ntaddress.csv");

        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private static void outputFile(byte[] bytes, String filename) throws IOException {
        FileOutputStream fos = new FileOutputStream(filename);
        fos.write(bytes);
        fos.close();
    }

    private static void outputFile(String string, String filename) throws IOException {
        FileWriter fw = new FileWriter(filename);
        fw.write(string);
        fw.close();
    }

    private static String mapToCSVStr(HashMap<String, Integer> map) throws IOException {

        StringBuilder sb = new StringBuilder();
        for (var e : map.entrySet()) {
            sb.append(e.getValue());
            sb.append(",");
            sb.append(e.getKey());
            sb.append("\n");
        }

        return sb.toString();
    }
}