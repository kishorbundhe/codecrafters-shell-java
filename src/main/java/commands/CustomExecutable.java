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
        ArrayList<String> files = new ArrayList<>();
        String regex = "\"([^\"]*?)\"|'([^']*?)'";
        Pattern pattern = Pattern.compile(regex);
        options.replaceAll("\"\"", "");
        options.replaceAll("\'\'", "");
        StringBuilder copyOptions = new StringBuilder(options);
        StringBuilder escapedOptions = new StringBuilder();
        while (true) {
            boolean hasNoMatch = false;
            Matcher matcher = pattern.matcher(copyOptions.toString());
            int start = 0;
            if (matcher.find()) {
                if (matcher.start() != 0) {
                    String s = copyOptions.toString().substring(0, matcher.start());
                    String substr = s.replaceAll("\\s+", " ");
                    escapedOptions.append(substr);
                }
                files.add(escapedOptions.append(copyOptions, matcher.start() + 1, matcher.end() - 1).toString());
                copyOptions.delete(start, matcher.end());
            } else {
                hasNoMatch = true;
                escapedOptions.append(copyOptions);
                break;
            }
            start = matcher.start();
            if (hasNoMatch) {
                break;
            }

        }

        return files;
    }
}
