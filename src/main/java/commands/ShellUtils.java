package commands;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShellUtils {
    private static final String REGEX = "(?<!\\\\)\"((?:\\\\.|[^\"\\\\])*)\"(?!\\\\)|(?<!\\\\)'([^']*?)'(?!\\\\)";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    public static List<String> tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        if (input == null || input.isBlank())
            return tokens;

       // remove "  " to "" and '  ' to ''
        String processed = input.replace("\"\"", "").replace("''", "");
        StringBuilder sb = new StringBuilder(processed);

        while (true) {
            Matcher matcher = PATTERN.matcher(sb.toString());
            if (matcher.find()) {
                // Handle unquoted text before the match
                if (matcher.start() > 0) {
                    String before = sb.substring(0, matcher.start());
                    for (String part : before.trim().split("\\s+")) {
                        if (!part.isEmpty())
                            tokens.add(unescapeUnquoted(part));
                    }
                }

                // Handle the quoted part
                String content = sb.substring(matcher.start(), matcher.end());
                if (content.startsWith("\"")) {
                    // handle inside double quotes
                    tokens.add(unescapeDoubleQuotes(content.substring(1, content.length() - 1)));
                } else {
                    // handle inside single quote
                    tokens.add(content.substring(1, content.length() - 1));
                }
                sb.delete(0, matcher.end());
            } else {
                // Handle remaining unquoted text
                String remainder = sb.toString().trim();
                if (!remainder.isEmpty()) {
                    for (String part : remainder.split("\\s+")) {
                        tokens.add(unescapeUnquoted(part));
                    }
                }
                break;
            }
        }
        return tokens;
    }


    public static String resolveQuotes(String input) {
        return String.join(" ", tokenize(input));
    }

    private static String unescapeUnquoted(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length())
                sb.append(s.charAt(++i));
            else
                sb.append(c);
        }
        return sb.toString();
    }

    private static String unescapeDoubleQuotes(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(i + 1);
                if (next == '\"' || next == '\\') {
                    sb.append(next);
                    i++;
                    continue;
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }
}