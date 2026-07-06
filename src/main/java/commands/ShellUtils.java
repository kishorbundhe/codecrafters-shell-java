package commands;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*Then apply the shell rules:
whitespace ends a token only outside quotes
quoted and unquoted text are appended to the same token
inside double quotes, \" and \\ are escapes
inside single quotes, everything is literal
outside quotes, \x means literal x*/

public class ShellUtils {
  private static final String REGEX =
      "(?<!\\\\)\"((?:\\\\.|[^\"\\\\])*)\"(?!\\\\)|(?<!\\\\)'([^']*?)'(?!\\\\)";
  private static final Pattern PATTERN = Pattern.compile(REGEX);

  // echo "example\"insidequotes"script\" - > output : "example"insidequotesscript""
  public static List<String> tokenize(String input) {
    List<String> tokens = new ArrayList<>();
    if (input == null || input.isBlank()) return tokens;
    // String processed = input.replace("\"\"", "").replace("''", "");
    StringBuilder sb = new StringBuilder(input);

    while (true) {
      Matcher matcher = PATTERN.matcher(sb.toString());

      if (sb.isEmpty()) break;
      if (matcher.find()) {
        // Handle unquoted text before the match
        if (matcher.start() > 0) {
          String before = sb.substring(0, matcher.start());
          for (String part : before.trim().split("\\s+")) {
            if (!part.isEmpty()) tokens.add(unescapeUnquoted(part));
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
          String[] split = remainder.split("\\s+");
          for (String part : split) {
            if (input.indexOf(part) > 0 && input.charAt(input.indexOf(part) - 1) != ' ') {
              String lastToken = tokens.removeLast();
              lastToken = lastToken + unescapeUnquoted(part);
              tokens.add(lastToken);
            } else {
              tokens.add(unescapeUnquoted(part));
            }
          }
        }
        break;
      }
    }
    return tokens;
  }

  public static List<String> resolveQuotesWithoutRegex(String input) {
    boolean insideDoubleQuotes = false;
    boolean insideSingleQuotes = false;
    boolean previousWasSpace = false;
    List<String> tokens = new ArrayList<>();
    StringBuilder sb = new StringBuilder();
    char[] chars = input.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      char aChar = chars[i];
      if (aChar == '"' && !insideSingleQuotes) {
        insideDoubleQuotes = !insideDoubleQuotes;
      } else if (aChar == '\'' && !insideDoubleQuotes) {
        insideSingleQuotes = !insideSingleQuotes;
      } else if (!insideSingleQuotes
          && aChar == '\\'
          && (i + 1) < chars.length
          && (chars[i + 1] == '"'
              || chars[i + 1] == '\''
              || chars[i + 1] == '\\'
              || chars[i + 1] == ' '
              || chars[i + 1] == '$'
              || chars[i + 1] == '`')) { // just skip this
        sb.append(chars[i + 1]);
        i++;
      } else if (!insideDoubleQuotes && !insideSingleQuotes && aChar == ' ') {
        if (!previousWasSpace) {
            tokens.add(sb.toString());
            tokens.add(String.valueOf(aChar));
            sb.delete(0, sb.length());
          previousWasSpace = true;
        }
      } else if (previousWasSpace && aChar == ' ') {

      } else {
        sb.append(aChar);
        previousWasSpace = false;
      }
    }
    if(!sb.isEmpty()) {
        tokens.add(sb.toString());
    }
    return tokens;
  }

  public static String resolveQuotes(String input) {

      List<String> tokens = resolveQuotesWithoutRegex(input);
      StringBuilder sb = new StringBuilder();
      for(String string : tokens) {
          sb.append(string);
      }
      return sb.toString();
  }

  private static String unescapeUnquoted(String s) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '\\' && i + 1 < s.length()) sb.append(s.charAt(++i));
      else sb.append(c);
    }
    return sb.toString();
  }

  // "\\t" -> \t
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
