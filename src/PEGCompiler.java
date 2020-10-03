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
import peg.VMCodeGenerator;

public class PEGCompiler {
    // ファイルからデータを読み取って、仮想マシンコードを返す。
    public static void main(String[] args) {

        try {
            boolean packrat = true;

            List<String> input = Files.readAllLines(Path.of(args[0]), StandardCharsets.UTF_8);

            VMCodeGenerator vmcodeGenerator = new VMCodeGenerator(input);
            OpList oplist = vmcodeGenerator.generate(packrat);

            byte[] bin = makeBinaryData(oplist, packrat);
            output(bin, "vmcode.bin");

            output(oplist.toString(), "vmcode.meta");

            String ntAddressListStr = mapToCSVStr(oplist.NTaddressMap);
            output(ntAddressListStr, "ntaddress.csv");

        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private static byte[] makeBinaryData(OpList oplist, boolean packrat) throws IOException {

        byte[] header;
        if (packrat) {
            byte[] flag = { 0x01 };
            byte[] bytearray = ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN)
                    .putInt(oplist.NTaddressMap.size()).array();
            header = concat(flag, bytearray);
        } else {
            byte[] flag = { 0x00 };
            header = flag;
        }

        byte[] body = oplist.toBinary();

        return concat(header, body);
    }

    private static void output(byte[] bytes, String filename) throws IOException {
        FileOutputStream fos = new FileOutputStream(filename);
        fos.write(bytes);
        fos.close();
    }

    private static void output(String string, String filename) throws IOException {
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

    private static byte[] concat(byte[]... b) {
        byte[] r = new byte[] {};
        for (int i = 0; i < b.length; i++) {
            byte[] f = r, s = b[i];
            r = new byte[f.length + s.length];
            System.arraycopy(f, 0, r, 0, f.length);
            System.arraycopy(s, 0, r, f.length, s.length);
        }
        return r;
    }
}