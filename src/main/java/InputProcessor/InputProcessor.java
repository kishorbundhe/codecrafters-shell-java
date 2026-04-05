package InputProcessor;

import commands.StdErrFile;
import commands.StdOutFile;
import commands.UserInput;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputProcessor {

  public UserInput parseUserInput(String inputFromUser) {
    RedirectionResult redirection = extractRedirectionInfo(inputFromUser);
    setupRedirectionStreams(redirection.stdOut(), redirection.stdErr());
    CommandParts commandParts = parseCommandAndOptions(redirection.cleanedInput());

    return new UserInput(
        redirection.cleanedInput(),
        commandParts.command(),
        commandParts.options(),
        redirection.stdOut(),
        redirection.stdErr());
  }

  private static RedirectionResult extractRedirectionInfo(String input) {
    String stdOutFile = "", stdErrFile = "";
    boolean stdOutAppend = false, stdErrAppend = false;
    String cleanedInput = input;

    if (input.contains("2>>")) {
      String[] split = input.split("2>>");
      cleanedInput = split[0].trim();
      stdErrFile = split[1].trim();
      stdErrAppend = true;
    } else if (input.contains("2>")) {
      String[] split = input.split("2>");
      cleanedInput = split[0].trim();
      stdErrFile = split[1].trim();
    } else if (input.contains(">>") || input.contains("1>>")) {
      String[] split = input.split(">>|1>>");
      cleanedInput = split[0].trim();
      stdOutFile = split[1].trim();
      stdOutAppend = true;
    } else if (input.contains(">") || input.contains("1>")) {
      String[] split = input.split(">|1>");
      cleanedInput = split[0].trim();
      stdOutFile = split[1].trim();
    }

    return new RedirectionResult(
        cleanedInput,
        new StdOutFile(stdOutFile, stdOutAppend),
        new StdErrFile(stdErrFile, stdErrAppend));
  }

  private static void setupRedirectionStreams(StdOutFile stdOut, StdErrFile stdErr) {

    if (!stdOut.stdOutFile().isEmpty()) {
      try {
        FileOutputStream fos = new FileOutputStream(stdOut.stdOutFile(), stdOut.append());
        System.setOut(new PrintStream(fos));
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    }
    if (!stdErr.stdErrFile().isEmpty()) {
      try {
        FileOutputStream fos = new FileOutputStream(stdErr.stdErrFile(), stdErr.append());
        System.setErr(new PrintStream(fos));
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    }
  }

  private static CommandParts parseCommandAndOptions(String input) {
    String command = "";
    String options = "";

    // used to handle executable/command with names which starts with double quotes
    if (input.startsWith("\"")) {
      String regex = "\"([^\"]*)\"";
      Matcher matcher = Pattern.compile(regex).matcher(input);
      boolean hasMatch = matcher.find();
      if (hasMatch) {
        command = matcher.group(0);
        options = input.substring(matcher.end()).trim();
      }
    } else if (input.startsWith("'")) {
      String regex = "'([^']*)'"; // '([^']*?)'
      Matcher matcher = Pattern.compile(regex).matcher(input);
      boolean hasMatch = matcher.find();
      if (hasMatch) {
        command = matcher.group(0);
        options = input.substring(matcher.end()).trim();
      }
    } else {
      command = input.split(" ")[0];
      options = input.replaceFirst(Pattern.quote(command), "").trim();

    }
    return new CommandParts(command, options);
  }
}
