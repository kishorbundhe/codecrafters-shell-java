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
        if (command.equalsIgnoreCase("cat")) {
            args = getCatCommandAndFiles(command, options);
        } else {
            args = getCommandArgs(command, options);
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

    private List<String> getCommandArgs(String command, String options) {
        return Stream.concat(
                Stream.of(command),
                Arrays.stream(options.split(" ")))
                .collect(Collectors.toList());
    }

    private List<String> getCatCommandAndFiles(String command, String options) {
        List<String> args;
        ArrayList<String> files = escapeQuotes(options);
        args = Stream.concat(
                Stream.of(command),
                files.stream())
                .collect(Collectors.toList());
        return args;
    }

    private ArrayList<String> escapeQuotes(String options) {
        Pattern pattern;
        String regex;
        if (options.startsWith("\"") && options.endsWith("\"")) {
            regex = "\"([^\"]*)\"";
            pattern = Pattern.compile(regex);
        } else {
            regex = "'([^']*)'";
            pattern = Pattern.compile(regex);
        }

        String copyOptions = options;
        Matcher matcher = pattern.matcher(copyOptions);
        ArrayList<String> files = new ArrayList<>();
        while (matcher.find()) {
            String toReplace = matcher.group(1);
            // group 0 =. 'world hello' group 1 = world hello
            files.add(toReplace);
            copyOptions = copyOptions.replaceFirst(regex, "$1");
        }
        return files;
    }
}
