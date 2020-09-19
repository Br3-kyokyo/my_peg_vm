
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import vm.*;

public class Main {

    public static void main(String[] args) {

        try {
            byte[] parsingProgram = Files.readAllBytes(Path.of(args[0]));
            List<String> input = Files.readAllLines(Path.of(args[1]), StandardCharsets.UTF_8);
            // char[] inputString = readAll(args[1]).toCharArray();

            long startTime = System.currentTimeMillis();
            System.out.println("開始時刻：" + startTime + " ms");
            
            VM vm = new VM(parsingProgram, input);
            vm.exec();
     
            long endTime = System.currentTimeMillis();
     
            System.out.println("終了時刻：" + endTime + " ms");
            System.out.println("処理時間：" + (endTime - startTime) + " ms");
        
            

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static String readAll(final String path) throws IOException {
        return Files.lines(Path.of(path), StandardCharsets.UTF_8)
                .collect(Collectors.joining(System.getProperty("line.separator")));
    }
}