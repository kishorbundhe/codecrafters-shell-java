import static commands.Command.commandIsPresentAndExecutable;
import static commands.Command.commandNotFound;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import InputProcessor.InputProcessor;
import commands.CdCommand;
import commands.CustomExecutable;
import commands.EchoComand;
import commands.ExitCommand;
import commands.Pair;
import commands.PwdCommand;
import commands.TypeCommand;
import commands.UserInput;
import commands.ValidCommand;
import lineReader.CustomTabProcessor;

public class Main {
  public static void main(String[] args) throws Exception {

    try {
      Terminal terminal = TerminalBuilder.terminal();
      Collection<String> dynamicStrings = getCurrentCommands();
      Completer dynamicCompleter = new StringsCompleter(dynamicStrings);
      LineReader reader = CustomTabProcessor.configureLineReader(terminal, dynamicCompleter);

      final PrintStream console = System.out;
      for (; ; ) {

        boolean shouldContinue = shouldContinueRunningCommand(reader, console);
        if (!System.out.equals(console)) System.setOut(console);
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

  private static boolean shouldContinueRunningCommand(LineReader reader, PrintStream console) {

    String inputFromUser = reader.readLine("$ ").trim();
    if (inputFromUser.isBlank()) {
      return true;
    }
    boolean hasShellBuiltIns = false;
    if (inputFromUser.contains("|")) {
      for (ValidCommand command : ValidCommand.values()) {
        if (inputFromUser.contains(command.getCommand())) {
          hasShellBuiltIns = true;
        }
      }
    }
    InputProcessor inputProcessor = new InputProcessor();
    if (hasShellBuiltIns) {
      List<String> pipedCommands =
          Arrays.stream(inputFromUser.split("\\|")).map(String::trim).toList();
      File previousOutput;
      File currentOutput;
      try {
        Path previousTempFilePath = Paths.get("previousOutput.txt");
        Path nextTempFilePath = Paths.get("nextOutput.txt");
        if (Files.exists(previousTempFilePath)) {
          Files.delete(previousTempFilePath);
          Files.delete(nextTempFilePath);
        }
        previousOutput = Files.createFile(previousTempFilePath).toFile();
        currentOutput = Files.createFile(nextTempFilePath).toFile();
        FileOutputStream fos = new FileOutputStream(previousOutput, true);
        System.setOut(new PrintStream(fos));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      for (int index = 0; index < pipedCommands.size(); index++) {
        UserInput userInput =
            inputProcessor.parseUserInput(pipedCommands.get(index), previousOutput, currentOutput);
        processUserCommand(userInput);
        System.out.flush();
        if (index == pipedCommands.size() - 1) {
          System.setOut(console);
          try {
            List<String> readAllLines = Files.readAllLines(currentOutput.toPath());
            if (readAllLines.isEmpty()) {
              Files.readAllLines(previousOutput.toPath()).forEach(System.out::println);
            } else {
              readAllLines.forEach(System.out::println);
            }
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      }

      return true;
    } else {
      UserInput commandAndOption = inputProcessor.parseUserInput(inputFromUser, null, null);
      return processUserCommand(commandAndOption);
    }
  }

  private static boolean processUserCommand(UserInput userInput) {
    boolean shouldContinueRunning = true;

    if (userInput.command().equals(ValidCommand.PWD.getCommand())) {
      shouldContinueRunning = new PwdCommand().execute(userInput);
    } else if (userInput.command().equals(ValidCommand.CD.getCommand())) {
      shouldContinueRunning = new CdCommand().execute(userInput);
    } else if (userInput.command().equals(ValidCommand.TYPE.getCommand())) {
      shouldContinueRunning = new TypeCommand().execute(userInput);
    } else if (userInput.command().equals(ValidCommand.EXIT.getCommand()))
      shouldContinueRunning = new ExitCommand().execute(userInput);
    else if (userInput.command().equals(ValidCommand.ECHO.getCommand())) {
      String escapeOptions = escapeQuotes(userInput.options());
      userInput =
          new UserInput(
              "",
              userInput.command(),
              escapeOptions,
              userInput.stdOutFile(),
              userInput.stdErrFile(),
              userInput.inputFile(),
              userInput.outputfile());
      shouldContinueRunning = new EchoComand().execute(userInput);
    } else {
      String escapeCommand = escapeQuotes(userInput.command());
      Pair<Boolean, Path> commandIsPresentAndExecutablePair =
          commandIsPresentAndExecutable(escapeCommand);
      Boolean isCommandPresentInSysPath = commandIsPresentAndExecutablePair.first();
      Path path = commandIsPresentAndExecutablePair.second();
      if (isCommandPresentInSysPath) {
        userInput =
            new UserInput(
                userInput.userInput(),
                path.getFileName().toString(),
                userInput.options(),
                userInput.stdOutFile(),
                userInput.stdErrFile(),
                userInput.inputFile(),
                userInput.outputfile());
        // executable which are at sys path
        shouldContinueRunning = new CustomExecutable().execute(userInput);
      } else commandNotFound(userInput.userInput());
    }
    return shouldContinueRunning;
  }

  private static String escapeQuotes(String options) {
    // used for pattern matching of str = "test \" hello \world " this pattern does
    // not stop at
    // 'test \'
    // it captures the whole test \" hello \world
    String regex = "(?<!\\\\)\"((?:\\\\.|[^\"\\\\])*)\"(?!\\\\)|(?<!\\\\)'([^']*?)'(?!\\\\)";
    Pattern pattern = Pattern.compile(regex);
    options = options.replace("\"\"", "");
    options = options.replace("''", "");
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

  private static void processBackSlashInsideDoubleQuotes(
      StringBuilder copyOptions, StringBuilder escapedOptions, Matcher matcher, int start) {
    int i = matcher.start() + 1;
    int end = matcher.end() - 1;
    StringBuilder temporary = new StringBuilder();
    for (; i < end; i++) {
      char ch = copyOptions.charAt(i);
      if (ch == '\\'
          && ((i + 1 <= end)
              && (copyOptions.charAt(i + 1) == '\"' || copyOptions.charAt(i + 1) == '\\'))) {
        // this is escaping logic
        i++;
        temporary.append(copyOptions.charAt(i));
        continue;
      }
      temporary.append(ch);
    }
    escapedOptions.append(temporary);
    copyOptions.delete(start, matcher.end());
  }

  private static void processBackSlashInsideSingleQuotes(
      StringBuilder copyOptions, StringBuilder escapedOptions, Matcher matcher, int start) {
    escapedOptions.append(copyOptions, matcher.start() + 1, matcher.end() - 1);
    copyOptions.delete(start, matcher.end());
  }

  private static void beforeMatch(
      StringBuilder copyOptions, StringBuilder escapedOptions, Matcher matcher) {
    String s = copyOptions.toString().substring(0, matcher.start());
    String substr = s.replaceAll("\\s+", " ");
    escapedOptions.append(substr);
  }

  private static StringBuilder stringHasNoMatch(
      StringBuilder copyOptions, StringBuilder escapedOptions, int start) {
    // if the string is of the form : \"test 123"
    StringBuilder temporary = new StringBuilder();
    if (copyOptions.isEmpty()) return copyOptions;
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
