import static commands.Command.commandIsPresentAndExecutable;
import static commands.Command.commandNotFound;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import InputProcessor.InputProcessor;
import commands.CdCommand;
import commands.Command;
import commands.CustomExecutable;
import commands.EchoComand;
import commands.ExitCommand;
import commands.Pair;
import commands.PwdCommand;
import commands.ShellUtils;
import commands.TypeCommand;
import commands.UserInput;
import commands.ValidCommand;
import lineReader.CustomLineReader;
import pipe.PipelineStage;
import pipe.PipelineUtils;

public class Main {

  private static final Map<String, Command> COMMAND_MAP =
      Map.of(
          ValidCommand.PWD.getCommand(), new PwdCommand(),
          ValidCommand.CD.getCommand(), new CdCommand(),
          ValidCommand.TYPE.getCommand(), new TypeCommand(),
          ValidCommand.EXIT.getCommand(), new ExitCommand(),
          ValidCommand.ECHO.getCommand(), new EchoComand());

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

  private static boolean shouldContinueRunningCommand(LineReader reader, PrintStream console)
      throws IOException {

    String inputFromUser = reader.readLine("$ ").trim();
    if (inputFromUser.isBlank()) {
      return true;
    }

    boolean hasShellBuiltIns = false;
    InputProcessor inputProcessor = new InputProcessor();
    // I need to refactor this to handle echo raspberry\\nblueberry | wc this properly
    if (inputFromUser.contains("|")) {
      for (ValidCommand command : ValidCommand.values()) {
        if (inputFromUser.contains(command.getCommand())) {
          hasShellBuiltIns = true;
        }
      }
      if (hasShellBuiltIns) {
        //        List<String> pipedCommands =
        //            Arrays.stream(inputFromUser.split("\\|")).map(String::trim).toList();
        //        File previousOutput;
        //        File currentOutput;
        //        try {
        //          Path previousTempFilePath = Paths.get("previousOutput.txt");
        //          Path nextTempFilePath = Paths.get("nextOutput.txt");
        //          if (Files.exists(previousTempFilePath)) {
        //            Files.delete(previousTempFilePath);
        //            Files.delete(nextTempFilePath);
        //          }
        //          previousOutput = Files.createFile(previousTempFilePath).toFile();
        //          currentOutput = Files.createFile(nextTempFilePath).toFile();
        //          FileOutputStream fos = new FileOutputStream(previousOutput, true);
        //          System.setOut(new PrintStream(fos));
        //        } catch (IOException e) {
        //          throw new RuntimeException(e);
        //        }
        //
        //        for (int index = 0; index < pipedCommands.size(); index++) {
        //          UserInput userInput =
        //              inputProcessor.parseUserInput(
        //                  pipedCommands.get(index), previousOutput, currentOutput);
        //
        //          processUserCommand(userInput);
        //
        //          if (index == pipedCommands.size() - 1) {
        //            System.setOut(console);
        //            try {
        //              List<String> readAllLines = Files.readAllLines(currentOutput.toPath());
        //              if (readAllLines.isEmpty()) {
        //                Files.readAllLines(previousOutput.toPath()).forEach(System.out::println);
        //              } else {
        //                readAllLines.forEach(System.out::println);
        //              }
        //            } catch (IOException e) {
        //              throw new RuntimeException(e);
        //            }
        //          }
        //        }
        //        return true;
        String[] split = inputFromUser.split("\\|");
        List<PipelineStage> pipelineStages =
            Arrays.stream(split).map(String::trim).map(inputProcessor::parsePipelineStage).toList();

        Path prevPath = Paths.get("previous.tmp");
        Path currPath = Paths.get("current.tmp");

        // Ensure clean state for pipe files
        Files.deleteIfExists(prevPath);
        Files.deleteIfExists(currPath);

        File previous = Files.createFile(prevPath).toFile();
        File current = Files.createFile(currPath).toFile();

        for (int i = 0; i != pipelineStages.size(); ++i) {
          // 1st stage should take input from previous and send output to current
          // 2nd stage should take input from previous and send output to current
          if (i == 0) {
            pipelineStages.get(i).setStdin(InputStream.nullInputStream());
            pipelineStages.get(i).setStdout(new FileOutputStream(current));
            pipelineStages.get(i).setOutputRedirect(ProcessBuilder.Redirect.to(current));

          } else if (i == pipelineStages.size() - 1) {
            // end of the stages
            pipelineStages.getLast().setStdout(System.out);
            pipelineStages.getLast().setStdin(new FileInputStream(previous));
            pipelineStages.getLast().setInputRedirect(ProcessBuilder.Redirect.from(previous));
            pipelineStages.getLast().setOutputRedirect(ProcessBuilder.Redirect.to(current));
          } else {
            try (FileInputStream fis = new FileInputStream(previous);
                FileOutputStream fos = new FileOutputStream(current)) {
              pipelineStages.get(i).setStdin(fis);
              pipelineStages.get(i).setStdout(fos);
            } catch (IOException e) {
              e.printStackTrace();
            }
            pipelineStages.get(i).setInputRedirect(ProcessBuilder.Redirect.from(previous));
            pipelineStages.get(i).setOutputRedirect(ProcessBuilder.Redirect.to(current));
          }
          processUserCommand(pipelineStages.get(i));
          File temp = previous; // temp = previous.txt
          previous = current; // PreviousFilePointer = current.txt
          current = temp; // currentPointer = previous.txt
        }
        PipelineUtils.writeOutput(pipelineStages.getLast(), Files.readString(previous.toPath()),false);
        return true;
      } else {
        // process pipelines should have pipe redirects
        String[] split = inputFromUser.split("\\|");
        List<PipelineStage> pipelineStagesWithoutAnyShellBuiltIn =
            Arrays.stream(split).map(String::trim).map(inputProcessor::parsePipelineStage).toList();
        pipelineStagesWithoutAnyShellBuiltIn.forEach(
            pipelineStage -> {
              pipelineStage.setCommand(ShellUtils.resolveQuotes(pipelineStage.getCommand()));
              pipelineStage.setErrorRedirect(ProcessBuilder.Redirect.PIPE);
              pipelineStage.setOutputRedirect(ProcessBuilder.Redirect.PIPE);
              pipelineStage.setInputRedirect(ProcessBuilder.Redirect.PIPE);
              Pair<Boolean, Path> commandResult =
                  commandIsPresentAndExecutable(pipelineStage.getCommand());
              if (!commandResult.first()) {
                PipelineUtils.writeOutput(
                    pipelineStage, pipelineStage.getCommand() + ": not found");
              }
            });
        new CustomExecutable().execute(pipelineStagesWithoutAnyShellBuiltIn);
        return true;
      }
    } else {
      PipelineStage pipelineStage = inputProcessor.parsePipelineStage(inputFromUser);
      return processUserCommand(pipelineStage);
    }
  }

  private static boolean processUserCommand(UserInput userInput) {
    Command builtin = COMMAND_MAP.get(userInput.command());
    if (builtin != null) {
      return builtin.execute(userInput);
    }
    return handleExternalCommand(userInput);
  }

  private static boolean processUserCommand(PipelineStage pipelineStage) {
    Command builtin = COMMAND_MAP.get(pipelineStage.getCommand());
    if (builtin != null) {
      return builtin.execute(pipelineStage);
    }
    return handleExternalCommand(pipelineStage);
  }

  public static boolean handleExternalCommand(PipelineStage pipelineStage) {
    String escapeCommand = ShellUtils.resolveQuotes(pipelineStage.getCommand());
    Pair<Boolean, Path> commandResult = commandIsPresentAndExecutable(escapeCommand);
    if (commandResult.first()) {
      CustomExecutable customExecutable = new CustomExecutable();
      return customExecutable.execute(pipelineStage);
    }
    PipelineUtils.writeOutput(pipelineStage, pipelineStage.getCommand() + ": not found");
    return true;
  }

  private static boolean handleExternalCommand(UserInput userInput) {
    String escapeCommand = ShellUtils.resolveQuotes(userInput.command());
    Pair<Boolean, Path> commandResult = commandIsPresentAndExecutable(escapeCommand);

    if (commandResult.first()) {
      Path path = commandResult.second();
      UserInput externalInput =
          new UserInput(
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
