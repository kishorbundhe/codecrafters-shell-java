package commands;

import java.util.ArrayList;
import java.util.List;

/*Then apply the shell rules:
whitespace ends a token only outside quotes
quoted and unquoted text are appended to the same token
inside double quotes, \" and \\ are escapes
inside single quotes, everything is literal
outside quotes, \x means literal x*/

public class ShellUtils {
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
          && (i + 1) < chars.length) { // just skip this
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
}
