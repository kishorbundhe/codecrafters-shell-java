package commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomExecutable implements Command {
    @Override
    public boolean execute(String command, String options) {
        List<String> args;
        if (command.contains("cat")) {
            Pattern pattern = Pattern.compile("'([^']*)'");
            String copyOptions = options;
            Matcher matcher = pattern.matcher(copyOptions);
            ArrayList<String> files = new ArrayList<>();
            while (matcher.find()) {
                String toReplace = matcher.group(0); 
                // group 0 =. 'world hello' group 1 = world hello
                files.add(toReplace);
                copyOptions = copyOptions.replaceFirst("'([^']*)'", "$1");
            }
            args = Stream.concat(
                    Stream.of(command),
                    files.stream())
                    .collect(Collectors.toList());
        } else {
            args = Stream.concat(
                    Stream.of(command),
                    Arrays.stream(options.split(" ")))
                    .collect(Collectors.toList());
        }

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
