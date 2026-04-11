import static commands.Command.commandIsPresentAndExecutable;
import static commands.Command.commandNotFound;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import commands.*;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import InputProcessor.InputProcessor;
import lineReader.CustomLineReader;

public class Main {

  private static final Map<String, Command> COMMAND_MAP = Map.of(
      ValidCommand.PWD.getCommand(), new PwdCommand(),
      ValidCommand.CD.getCommand(), new CdCommand(),
      ValidCommand.TYPE.getCommand(), new TypeCommand(),
      ValidCommand.EXIT.getCommand(), new ExitCommand(),
      ValidCommand.ECHO.getCommand(), new EchoComand()
  );

  public static void main(String[] args) throws Exception {

    try {
      Terminal terminal = TerminalBuilder.terminal();
      LineReader reader = CustomLineReader.configureLineReader(terminal);

      final PrintStream console = System.out;
      for (; ; ) {

        boolean shouldContinue = shouldContinueRunningCommand(reader, console);
        if (!System.out.equals(console)) System.setOut(console);
        if (!System.err.equals(console)) System.setErr(console);
        if (!shouldContinue) {
          break;
        }
      }
      terminal.close();
    } catch (IOException e) {
      System.err.println("Error creating terminal: " + e.getMessage());
    }
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
    }
    else {
      UserInput commandAndOption = inputProcessor.parseUserInput(inputFromUser, null, null);
      return processUserCommand(commandAndOption);
    }
  }

  private static boolean processUserCommand(UserInput userInput) {
    Command builtin = COMMAND_MAP.get(userInput.command());
    if (builtin != null) {
      return builtin.execute(userInput);
    }
    return handleExternalCommand(userInput);
  }

  

  private static boolean handleExternalCommand(UserInput userInput) {
    String escapeCommand = ShellUtils.resolveQuotes(userInput.command());
    Pair<Boolean, Path> commandResult = commandIsPresentAndExecutable(escapeCommand);

    if (commandResult.first()) {
      Path path = commandResult.second();
      UserInput externalInput = new UserInput(
          userInput.userInput(),
          path.getFileName().toString(),
          userInput.options(),
          userInput.stdOutFile(),
          userInput.stdErrFile(),
          userInput.inputFile(),
          userInput.outputfile());
      return new CustomExecutable().execute(externalInput);
    }

    commandNotFound(userInput.userInput());
    return true;
  }
}
