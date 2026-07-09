import static commands.Command.commandIsPresentAndExecutable;
import static commands.ValidCommand.containsShellBuiltIn;
import static pipe.PipelineUtils.getPipelineStages;

import InputProcessor.InputProcessor;
import commands.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import lineReader.CustomLineReader;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import pipe.PipelineStage;
import pipe.PipelineUtils;

public class Main {

  private static final Map<String, Command> COMMAND_MAP =
      Map.of(
          ValidCommand.PWD.getCommand(), new PwdCommand(),
          ValidCommand.CD.getCommand(), new CdCommand(),
          ValidCommand.TYPE.getCommand(), new TypeCommand(),
          ValidCommand.EXIT.getCommand(), new ExitCommand(),
          ValidCommand.ECHO.getCommand(), new EchoComand(),
          ValidCommand.HISTORY.getCommand(), new HistoryCommand());

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

    InputProcessor inputProcessor = new InputProcessor();
    if (!inputFromUser.contains("|")) {
      PipelineStage pipelineStage = inputProcessor.parsePipelineStage(inputFromUser);
      return processUserCommand(pipelineStage);
    }

    if (containsShellBuiltIn(inputFromUser)) {
      executeWithBuiltinPipeline(inputFromUser, inputProcessor);
    } else {
      // process pipelines should have pipe redirects
      executeExternalPipeline(inputFromUser, inputProcessor);
    }
    return true;
  }

  private static void executeWithBuiltinPipeline(
      String inputFromUser, InputProcessor inputProcessor) throws IOException {
    List<PipelineStage> stages = getPipelineStages(inputFromUser, inputProcessor);

    Path prevPath = Paths.get("previous.tmp");
    Path currPath = Paths.get("current.tmp");

    // Ensure clean state for pipe files
    Files.deleteIfExists(prevPath);
    Files.deleteIfExists(currPath);

    File previous = Files.createFile(prevPath).toFile();
    File current = Files.createFile(currPath).toFile();

    for (int i = 0; i != stages.size(); ++i) {
      // 1st stage should take input from previous and send output to current
      // 2nd stage should take input from previous and send output to current
      configureStage(i, stages, current, previous);

      processUserCommand(stages.get(i));
      File temp = previous; // temp = previous.txt
      previous = current; // PreviousFilePointer = current.txt
      current = temp; // currentPointer = previous.txt
    }
    PipelineUtils.writeOutput(stages.getLast(), Files.readString(previous.toPath()), false);
  }

  private static void configureStage(
      int i, List<PipelineStage> pipelineStages, File current, File previous)
      throws FileNotFoundException {
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
  }

  private static void executeExternalPipeline(String inputFromUser, InputProcessor inputProcessor) {
    List<PipelineStage> pipelineStagesWithoutAnyShellBuiltIn =
        getPipelineStages(inputFromUser, inputProcessor);
    pipelineStagesWithoutAnyShellBuiltIn.forEach(
        pipelineStage -> {
          pipelineStage.setCommand(ShellUtils.resolveQuotes(pipelineStage.getCommand()));
          pipelineStage.setErrorRedirect(ProcessBuilder.Redirect.PIPE);
          pipelineStage.setOutputRedirect(ProcessBuilder.Redirect.PIPE);
          pipelineStage.setInputRedirect(ProcessBuilder.Redirect.PIPE);
          Pair<Boolean, Path> commandResult =
              commandIsPresentAndExecutable(pipelineStage.getCommand());
          if (!commandResult.first()) {
            PipelineUtils.writeOutput(pipelineStage, pipelineStage.getCommand() + ": not found");
          }
        });
    new CustomExecutable().execute(pipelineStagesWithoutAnyShellBuiltIn);
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
      // Use the resolved full path for execution to handle special characters correctly
      pipelineStage.setCommand(escapeCommand);
      CustomExecutable customExecutable = new CustomExecutable();
      return customExecutable.execute(pipelineStage);
    }
    PipelineUtils.writeOutput(pipelineStage, pipelineStage.getCommand() + ": not found");
    return true;
  }
}
