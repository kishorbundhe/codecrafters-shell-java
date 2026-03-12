import static commands.Command.commandIsPresentAndExecutable;
import static commands.Command.commandNotFound;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import commands.CdCommand;
import commands.CustomExecutable;
import commands.EchoComand;
import commands.ExitCommand;
import commands.Pair;
import commands.PwdCommand;
import commands.TypeCommand;
import commands.UserInput;
import commands.ValidCommand;

public class Main {
    public static void main(String[] args) throws Exception {
        try (Scanner scanner = new Scanner(System.in)) {
            for (;;) {
                final PrintStream console = System.out;
                boolean shouldContinue = shouldContinueRunningCommand(scanner);
                if (!System.out.equals(console))
                    System.setOut(console);
                if (!shouldContinue) {
                    break;
                }
            }
        }
    }

    private static boolean shouldContinueRunningCommand(Scanner scanner) {
        System.out.print("$ ");
        String inputFromUser = scanner.nextLine().trim();
        String command, options, stdOutFileName = "";

        if (inputFromUser.isBlank()) {
            return true;
        }
        if (inputFromUser.contains(">") || inputFromUser.contains("1>")) {

            String[] split = inputFromUser.split(">|1>");
            inputFromUser = split[0].trim();
            stdOutFileName = split[1].trim();
        }
        if (stdOutFileName != null && !stdOutFileName.isEmpty()) {
            try {
                System.setOut(new PrintStream(stdOutFileName));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (inputFromUser.startsWith("\"")) {
            String regex = "\"([^\"]*)\"";
            Matcher matcher = Pattern.compile(regex).matcher(inputFromUser);
            boolean hasMatch = matcher.find();
            if (hasMatch) {
                command = matcher.group(0);
                options = inputFromUser.substring(matcher.end(), inputFromUser.length()).trim();
            } else {
                System.out.println("could not process the command");
                return true;
            }
        } else if (inputFromUser.startsWith("'")) {
            String regex = "'([^']*)'"; // '([^']*?)'
            Matcher matcher = Pattern.compile(regex).matcher(inputFromUser);
            boolean hasMatch = matcher.find();
            if (hasMatch) {
                command = matcher.group(0);
                options = inputFromUser.substring(matcher.end(), inputFromUser.length()).trim();
            } else {
                System.out.println("could not process the command");
                return true;
            }
        } else {
            command = inputFromUser.split(" ")[0];
            options = inputFromUser.replaceFirst(command, "").trim();
        }

        UserInput userInput = new UserInput(command, options, stdOutFileName);
        if (command.equals(ValidCommand.PWD.getCommand())) {
            return new PwdCommand().execute(userInput);
        } else if (command.equals(ValidCommand.CD.getCommand())) {
            return new CdCommand().execute(userInput);
        } else if (command.equals(ValidCommand.TYPE.getCommand())) {
            return new TypeCommand().execute(userInput);
        } else if (command.equals(ValidCommand.EXIT.getCommand()))
            return new ExitCommand().execute(userInput);
        else if (command.equals(ValidCommand.ECHO.getCommand())) {
            options = escapeQuotes(options);
            return new EchoComand().execute(userInput);
        } else {
            command = escapeQuotes(command);

            Pair<Boolean, Path> commandIsPresentAndExecutablePair = commandIsPresentAndExecutable(command);
            Boolean isCommandPresentInSysPath = commandIsPresentAndExecutablePair.first();
            Path path = commandIsPresentAndExecutablePair.second();
            if (isCommandPresentInSysPath) {
                userInput = new UserInput(path.getFileName().toString(), options, stdOutFileName);
                return new CustomExecutable().execute(userInput);
            } else
                commandNotFound(inputFromUser);
        }
        return true;
    }

    // 'world hello' 'shell''script' example''test
    private static String escapeQuotes(String options) {

        String regex = "(?<!\\\\)\"((?:\\\\.|[^\"\\\\])*)\"(?!\\\\)|(?<!\\\\)'([^']*?)'(?!\\\\)";
        Pattern pattern = Pattern.compile(regex);
        options = options.replaceAll("\"\"", "");
        options = options.replaceAll("\'\'", "");
        StringBuilder copyOptions = new StringBuilder(options);
        StringBuilder escapedOptions = new StringBuilder();
        while (true) {
            Matcher matcher = pattern.matcher(copyOptions.toString());
            int start = 0;
            if (matcher.find()) {
                if (matcher.start() != 0) {
                    beforeMatch(copyOptions, escapedOptions, matcher);
                }
                // after the match starts, check if it is double quote or single quotes
                if (copyOptions.charAt(matcher.start()) == '\"') {
                    // if it is inside double quotes then we need escape ",\
                    processBackSlashInsideDoubleQuotes(copyOptions, escapedOptions, matcher, start);
                } else {
                    // if it is inside single quote
                    processBackSlashInsideSingleQuotes(copyOptions, escapedOptions, matcher, start);
                }

            } else {
                copyOptions = stringHasNoMatch(copyOptions, escapedOptions, start);
                break;
            }
            start = matcher.start();
        }

        options = escapedOptions.toString();
        return options;
    }

    private static void processBackSlashInsideDoubleQuotes(StringBuilder copyOptions, StringBuilder escapedOptions,
            Matcher matcher,
            int start) {
        int i = matcher.start() + 1;
        int end = matcher.end() - 1;
        StringBuilder temporary = new StringBuilder();
        for (; i < end; i++) {
            char ch = copyOptions.charAt(i);
            if (ch == '\\' && ((i + 1 <= end)
                    && (copyOptions.charAt(i + 1) == '\"' || copyOptions.charAt(i + 1) == '\\'))) {
                // this is escaping logic
                i++;
                temporary.append(copyOptions.charAt(i));
                continue;
            }
            temporary.append(ch);
        }
        escapedOptions.append(temporary.toString());
        copyOptions.delete(start, matcher.end());
    }

    private static void processBackSlashInsideSingleQuotes(StringBuilder copyOptions, StringBuilder escapedOptions,
            Matcher matcher, int start) {
        escapedOptions.append(copyOptions, matcher.start() + 1, matcher.end() - 1);
        copyOptions.delete(start, matcher.end());
    }

    private static void beforeMatch(StringBuilder copyOptions, StringBuilder escapedOptions, Matcher matcher) {
        String s = copyOptions.toString().substring(0, matcher.start());
        String substr = s.replaceAll("\\s+", " ");
        escapedOptions.append(substr);
    }

    private static StringBuilder stringHasNoMatch(StringBuilder copyOptions, StringBuilder escapedOptions, int start) {
        // if the string is of the form : \"test 123"
        StringBuilder temporary = new StringBuilder();
        copyOptions = new StringBuilder(copyOptions.toString().replaceAll("\\s+", " "));
        for (int i = start; i < copyOptions.length(); i++) {
            char ch = copyOptions.charAt(i);
            if (ch == '\\') {
                i++;
                temporary.append(copyOptions.charAt(i));
                continue;
            }
            temporary.append(ch);
        }
        escapedOptions.append(temporary.toString());
        return copyOptions;
    }

}
