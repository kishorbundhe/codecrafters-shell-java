package commands;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomExecutable implements Command{
    @Override
    public boolean execute(String command, String options) {
        List<String> args = Stream.concat(
                Stream.of(command),
                Arrays.stream(options.split(" ")))
                .collect(Collectors.toList());

        ProcessBuilder processBuilder = new ProcessBuilder(args);
        processBuilder.inheritIO();
        try {
            Process process = processBuilder.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }
}
