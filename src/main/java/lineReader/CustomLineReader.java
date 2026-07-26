package lineReader;

import static org.jline.reader.LineReader.BEEP;
import static org.jline.reader.LineReader.BELL_STYLE;
import static org.jline.reader.LineReader.EXPAND_OR_COMPLETE;
import static org.jline.reader.LineReader.TAB_WIDTH;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import commands.ValidCommand;
import org.jline.keymap.KeyMap;
import org.jline.reader.*;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;

public class CustomLineReader {

    public static LineReader configureLineReader(Terminal terminal) {
        Collection<String> dynamicStrings = getCurrentCommands();
        Completer dynamicCompleter = new StringsCompleter(dynamicStrings);
        AtomicInteger tabCount = new AtomicInteger(0);
        History history = new DefaultHistory();
        LineReader lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .parser(new DisableEscapingChars())
                .completer(dynamicCompleter)
                .history(history)
                .variable(BELL_STYLE, "audible")
                .variable(TAB_WIDTH, 2)
                .option(LineReader.Option.AUTO_LIST, true) // Automatically list options+
                .option(LineReader.Option.LIST_PACKED, true) // Display completions in a compact form
                .option(LineReader.Option.AUTO_MENU, false) // Show menu automatically
                .option(LineReader.Option.MENU_COMPLETE, true) // Cycle through completions
                .build();

        Widget customTabWidget = getTabWidget(terminal, lineReader, dynamicCompleter, tabCount);
        lineReader.getWidgets().put("customtab-widget", customTabWidget);
        KeyMap<Binding> keyMap = lineReader.getKeyMaps().get(LineReader.MAIN);
        keyMap.bind(customTabWidget, "\t");

        return lineReader;
    }

    private static Widget getTabWidget(Terminal terminal, LineReader lineReader, Completer dynamicCompleter, AtomicInteger tabCount) {
        Widget customTabWidget = () -> {
            boolean istab = lineReader.getLastBinding().equals("\t");
            List<Candidate> candidates = new ArrayList<>();
            final String wordUserHastyped = lineReader.getBuffer().toString();
            dynamicCompleter.complete(lineReader, lineReader.getParsedLine(), candidates);
            Set<String> uniqueCandidates = candidates.stream().sorted().map(Candidate::value)
                    .collect(Collectors.toSet());
            List<String> matchedCandidates = uniqueCandidates.stream()
                    .sorted()
                    .filter(
                            candidate -> candidate.toLowerCase().startsWith(wordUserHastyped.toLowerCase()))
                    .toList();
            if (matchedCandidates.isEmpty()) {
                lineReader.callWidget(BEEP);

            } else if (matchedCandidates.size() == 1) {
                lineReader.callWidget(EXPAND_OR_COMPLETE);
            } else if (istab && tabCount.get() == 0) {
                if (matchedCandidates.getLast().contains(matchedCandidates.getFirst())) {
                    lineReader.getBuffer().clear();
                    lineReader.getBuffer().write(matchedCandidates.getFirst());
                    lineReader.callWidget(
                            LineReader.REDRAW_LINE); // it redraws the line removing user input
                    lineReader.getTerminal().flush();

                    return true;
                }
                lineReader.callWidget(BEEP);
                tabCount.incrementAndGet();
            } else if (tabCount.get() == 1 && istab) {
                lineReader.getTerminal().writer().println();
                for (String complete : matchedCandidates) {
                    terminal.writer().print(complete + "  ");
                }

                lineReader.getTerminal().writer().println();
                lineReader.getTerminal().flush();
                lineReader.callWidget(
                        LineReader.REDRAW_LINE); // it redraws the line removing user input
                lineReader.callWidget(LineReader.REDISPLAY); // it creates the whole display again
                tabCount.set(0);
            }
            return true;
        };
        return customTabWidget;
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
                    List<String> temp =
                            Files.list(dirPath)
                                    .filter(Files::isExecutable)
                                    .map(file -> file.getFileName().toString())
                                    .toList();
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
}
