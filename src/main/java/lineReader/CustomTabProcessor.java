package lineReader;

import static org.jline.reader.LineReader.BEEP;
import static org.jline.reader.LineReader.BELL_STYLE;
import static org.jline.reader.LineReader.EXPAND_OR_COMPLETE;
import static org.jline.reader.LineReader.TAB_WIDTH;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.jline.keymap.KeyMap;
import org.jline.reader.Binding;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Widget;
import org.jline.terminal.Terminal;

public class CustomTabProcessor {

    public static LineReader configureLineReader(Terminal terminal, Completer dynamicCompleter) {
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
        lineReader.getWidgets().put("customtab-widget", customTabWidget);
        KeyMap<Binding> keyMap = lineReader.getKeyMaps().get(LineReader.MAIN);
        keyMap.bind(customTabWidget, "\t");

        return lineReader;
    }
}
