import commands.*;
import lineReader.DisableEscapingChars;
import org.jline.keymap.KeyMap;
import org.jline.reader.*;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static commands.Command.commandIsPresentAndExecutable;
import static commands.Command.commandNotFound;
import static org.jline.reader.LineReader.*;

public class Main {
    public static void main(String[] args) throws Exception {
        try {
            Terminal terminal = TerminalBuilder.terminal();
            Collection<String> dynamicStrings = getCurrentCommands();
            Completer dynamicCompleter = new StringsCompleter(dynamicStrings);

            LineReader reader = configureLineReader(terminal, dynamicCompleter);
            final PrintStream console = System.out;
            for (; ; ) {

                boolean shouldContinue = shouldContinueRunningCommand(reader);
                if (!System.out.equals(console))
                    System.setOut(console);
                if (!System.err.equals(console)) {
                    System.setErr(console);
                }
                if (!shouldContinue) {
                    break;
                }
            }

            terminal.close();
        } catch (IOException e) {
            System.err.println("Error creating terminal: " + e.getMessage());
        }
    }

    private static LineReader configureLineReader(Terminal terminal, Completer dynamicCompleter) {
        AtomicInteger tabCount = new AtomicInteger(0);
        LineReader lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .parser(new DisableEscapingChars())
                .completer(dynamicCompleter)
                .variable(BELL_STYLE, "audible")
                .variable(TAB_WIDTH, 2)
                .option(LineReader.Option.AUTO_LIST, true) // Automatically list options+
                .option(LineReader.Option.LIST_PACKED, true) // Display completions in a compact form
                .option(LineReader.Option.AUTO_MENU, false) // Show menu automatically
                .option(LineReader.Option.MENU_COMPLETE, true) // Cycle through completions
                .build();

        Widget customTabWidget = () -> {

            boolean istab = lineReader.getLastBinding().equals("\t");
            List<Candidate> candidates = new ArrayList<>();
            final String wordUserHastyped = lineReader.getBuffer().toString();
            dynamicCompleter.complete(lineReader, lineReader.getParsedLine(), candidates);
            Set<String> uniqueCandidates = candidates
                    .stream()
                    .sorted()
                    .map(Candidate::value).collect(Collectors.toSet());
            List<String> matchedCandidates = uniqueCandidates.stream().sorted()
                    .filter(candidate -> candidate.toLowerCase().startsWith(wordUserHastyped.toLowerCase()))
                    .toList();
            if (matchedCandidates.isEmpty()) {
                lineReader.callWidget(BEEP);

            } else if (matchedCandidates.size() == 1) {
                lineReader.callWidget(EXPAND_OR_COMPLETE);
            } else if (istab && tabCount.get() == 0) {
                lineReader.getTerminal().writer().println();
                if (matchedCandidates.getLast().contains(matchedCandidates.getFirst())) {
                    lineReader.getBuffer().clear();
                    terminal.writer().print(matchedCandidates.getFirst() + "  ");
                    lineReader.getTerminal().writer().println();
                    lineReader.getTerminal().flush();
                    lineReader.getBuffer().write(matchedCandidates.getFirst());
                    lineReader.callWidget(LineReader.REDRAW_LINE);
                    lineReader.callWidget(LineReader.REDISPLAY);
                    return true;
                }
                lineReader.callWidget(BEEP);
                tabCount.incrementAndGet();
            } else if (tabCount.get() == 1 && istab) {
                lineReader.getTerminal().writer().println();
                for (String complete : matchedCandidates) {
                    terminal.writer()
                            .print(complete + "  ");
                }

                lineReader.getTerminal().writer().println();
                lineReader.getTerminal().flush();
                lineReader.callWidget(LineReader.REDRAW_LINE);
                lineReader.callWidget(LineReader.REDISPLAY);
                tabCount.set(0);
            }
            return true;
        };
        lineReader.getWidgets().put("customtab-widget", customTabWidget);
        KeyMap<Binding> keyMap = lineReader.getKeyMaps().get(LineReader.MAIN);
        keyMap.bind(customTabWidget, "\t");

        return lineReader;
    }

    private static Collection<String> getCurrentCommands() {
        String path = System.getenv("PATH");
        String[] directories = path.split(File.pathSeparator);
        List<String> commandsFromPath = new ArrayList<>();

        for (String dir : directories) {
            // list all files in the directory and check if the command exists
            Path dirPath = Paths.get(dir);
            boolean isDirectory = Files.isDirectory(dirPath);
            if (isDirectory) {
                try {
                    List<String> temp = Files.list(dirPath)
                            .filter(file -> Files.isExecutable(file))
                            .map(file -> file.getFileName().toString())
                            .collect(Collectors.toList());
                    commandsFromPath.addAll(temp);

                } catch (IOException e) {

                    e.printStackTrace();
                }
            }

        }
        List<String> defaultCommands = new ArrayList<>();
        for (ValidCommand values : ValidCommand.values()) {
            defaultCommands.add(values.getCommand());
        }

        defaultCommands.addAll(commandsFromPath);
        Collections.sort(defaultCommands);
        return defaultCommands;
    }

    private static boolean shouldContinueRunningCommand(LineReader reader) {
        // System.out.print("$ ");
        String inputFromUser = reader.readLine("$ ").trim();
        if (inputFromUser.isBlank()) {
            return true;
        }

        UserInput commandAndOption = processUserCommand(inputFromUser);

        if (commandAndOption.command().equals(ValidCommand.PWD.getCommand())) {
            return new PwdCommand().execute(commandAndOption);
        } else if (commandAndOption.command().equals(ValidCommand.CD.getCommand())) {
            return new CdCommand().execute(commandAndOption);
        } else if (commandAndOption.command().equals(ValidCommand.TYPE.getCommand())) {
            return new TypeCommand().execute(commandAndOption);
        } else if (commandAndOption.command().equals(ValidCommand.EXIT.getCommand()))
            return new ExitCommand().execute(commandAndOption);
        else if (commandAndOption.command().equals(ValidCommand.ECHO.getCommand())) {
            String escapeOptions = escapeQuotes(commandAndOption.options());
            UserInput userInput = new UserInput("", commandAndOption.command(), escapeOptions,
                    commandAndOption.stdOutFile(), commandAndOption.stdErrFile());
            return new EchoComand().execute(userInput);
        } else {
            String escapeCommand = escapeQuotes(commandAndOption.command());
            Pair<Boolean, Path> commandIsPresentAndExecutablePair = commandIsPresentAndExecutable(escapeCommand);
            Boolean isCommandPresentInSysPath = commandIsPresentAndExecutablePair.first();
            Path path = commandIsPresentAndExecutablePair.second();
            if (isCommandPresentInSysPath) {
                UserInput userInput = new UserInput(inputFromUser, path.getFileName().toString(),
                        commandAndOption.options(), commandAndOption.stdOutFile(), commandAndOption.stdErrFile());
                return new CustomExecutable().execute(userInput);
            } else
                commandNotFound(commandAndOption.userInput());
        }
        return true;
    }

    private static UserInput processUserCommand(String inputFromUser) {
        String command = "", options = "", stdOutFileName = "", stdErrFileName = "";
        boolean stdOutAppend = false;
        boolean stdErrAppend = false;
        if (inputFromUser.contains("2>>")) {
            String[] split = inputFromUser.split("2>>");
            inputFromUser = split[0].trim();
            stdErrFileName = split[1].trim();
            stdErrAppend = true;
        } else if (inputFromUser.contains("2>")) {
            String[] split = inputFromUser.split("2>");
            inputFromUser = split[0].trim();
            stdErrFileName = split[1].trim();
        } else if (inputFromUser.contains(">>") || inputFromUser.contains("1>>")) {
            String[] split = inputFromUser.split(">>|1>>");
            inputFromUser = split[0].trim();
            stdOutFileName = split[1].trim();
            stdOutAppend = true;
        } else if (inputFromUser.contains(">") || inputFromUser.contains("1>")) {
            String[] split = inputFromUser.split(">|1>");
            inputFromUser = split[0].trim();
            stdOutFileName = split[1].trim();
        }

        if (!stdOutFileName.isEmpty()) {
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(stdOutFileName, stdOutAppend);
                System.setOut(new PrintStream(fileOutputStream));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (!stdErrFileName.isEmpty()) {
            try {
                FileOutputStream fileErrorStream = new FileOutputStream(stdErrFileName, stdErrAppend);
                System.setErr(new PrintStream(fileErrorStream));
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
                options = inputFromUser.substring(matcher.end()).trim();
            }
        } else if (inputFromUser.startsWith("'")) {
            String regex = "'([^']*)'"; // '([^']*?)'
            Matcher matcher = Pattern.compile(regex).matcher(inputFromUser);
            boolean hasMatch = matcher.find();
            if (hasMatch) {
                command = matcher.group(0);
                options = inputFromUser.substring(matcher.end()).trim();
            }
        } else {
            command = inputFromUser.split(" ")[0];
            options = inputFromUser.replaceFirst(command, "").trim();
        }

        return new UserInput(inputFromUser, command, options, new StdOutFile(stdOutFileName, stdOutAppend),
                new StdErrFile(stdErrFileName, stdErrAppend));
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
        if (copyOptions.isEmpty())
            return copyOptions;
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
