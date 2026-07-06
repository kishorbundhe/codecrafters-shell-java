package commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import InputProcessor.InputProcessor;
import pipe.PipelineStage;

public class CustomExecutable implements Command {
  @Override
  public boolean execute(UserInput userInput) {
    if (userInput.userInput().contains("|")) {
      executePipeBasedCommand(buildPipeBasedProcess(userInput.userInput()));
    } else {
      executeNonPipeBasedCommand(buildNonPipeBasedProcess(userInput));
    }
    return true;
  }

  @Override
  public boolean execute(PipelineStage pipelineStage) {
    ProcessBuilder pb = build(pipelineStage);
    executeNonPipeBasedCommand(pb);
    return true;
  }

  public boolean execute(ProcessBuilder pb) {
    executeNonPipeBasedCommand(pb);
    return true;
  }

  public void execute(List<PipelineStage> pipelineStages) {
    List<ProcessBuilder> processBuilders = new ArrayList<>();
    for (PipelineStage pipelineStage : pipelineStages) {
      processBuilders.add(build(pipelineStage));
    }
    executePipeBasedCommand(processBuilders);
  }

  public ProcessBuilder build(PipelineStage pipelineStage) {
    List<String> args = prepareArguments(pipelineStage.getCommand(), pipelineStage.getOptions());
    ProcessBuilder pb = new ProcessBuilder(args);
    // Default to inheriting IO unless redirected
    pb.inheritIO();

    // Handle redirections from Main.java (Piping)
    if (pipelineStage.getInputRedirect() != null) {
      pb.redirectInput(pipelineStage.getInputRedirect());
    }
    // Handle explicit user-defined redirections (e.g. > or 2>)
    if (pipelineStage.getOutputRedirect() != null) {
      pb.redirectOutput(pipelineStage.getOutputRedirect());
    }
    // Handle explicit user-defined redirections for error
    if (pipelineStage.getErrorRedirect() != null) {
      pb.redirectError(pipelineStage.getErrorRedirect());
    }
    return pb;
  }

  public List<ProcessBuilder> buildPipeBasedProcess(String unChangedInputFromUser) {
    InputProcessor inputProcessor = new InputProcessor();
    String[] split = unChangedInputFromUser.split("\\|");
    List<String> commands = Arrays.stream(split).map(String::trim).toList();
    return commands.stream()
        .map(c -> inputProcessor.parseUserInput(c, null, null))
        .map(this::getBuildProcessArguments)
        .map(bpa -> new ProcessBuilder(bpa.args))
        .toList();
  }

  private ProcessBuilder buildNonPipeBasedProcess(UserInput userInput) {
    BuildProcessArguments bpa = getBuildProcessArguments(userInput);
    ProcessBuilder pb = new ProcessBuilder(bpa.args());

    // Default to inheriting IO unless redirected
    pb.inheritIO();

    // Handle redirections from Main.java (Piping)
    if (userInput.inputFile() != null) pb.redirectInput(userInput.inputFile());
    if (userInput.outputfile() != null) pb.redirectOutput(userInput.outputfile());

    // Handle explicit user-defined redirections (e.g. > or 2>)
    if (bpa.stdFileName() != null && !bpa.stdFileName().isEmpty()) {
      pb.redirectOutput(
          bpa.stdOutAppend()
              ? ProcessBuilder.Redirect.appendTo(new File(bpa.stdFileName()))
              : ProcessBuilder.Redirect.to(new File(bpa.stdFileName())));
    }
    // Handle explicit user-defined redirections for error
    if (bpa.stdErrFileName() != null && !bpa.stdErrFileName().isEmpty()) {
      pb.redirectError(
          bpa.stdErrAppend()
              ? ProcessBuilder.Redirect.appendTo(new File(bpa.stdErrFileName()))
              : ProcessBuilder.Redirect.to(new File(bpa.stdErrFileName())));
    }

    return pb;
  }

  private static void executeNonPipeBasedCommand(ProcessBuilder processBuilder) {
    try {
      Process process = processBuilder.start();
      process.waitFor();
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  private static void executePipeBasedCommand(List<ProcessBuilder> processBuilders) {
    processBuilders.getLast().redirectOutput(ProcessBuilder.Redirect.INHERIT);
    try {
      List<Process> processes = ProcessBuilder.startPipeline(processBuilders);
      for (Process process : processes) {
        process.waitFor();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private BuildProcessArguments getBuildProcessArguments(UserInput userInput) {
    List<String> args = prepareArguments(userInput.command(), userInput.options());
    return new BuildProcessArguments(
        userInput.stdOutFile().stdOutFile(),
        userInput.stdOutFile().append(),
        userInput.stdErrFile().stdErrFile(),
        userInput.stdErrFile().append(),
        args);
  }

  private record BuildProcessArguments(
      String stdFileName,
      boolean stdOutAppend,
      String stdErrFileName,
      boolean stdErrAppend,
      List<String> args) {}

  public List<String> prepareArguments(String command, String options) {
    List<String> args = new ArrayList<>();
    args.add(command.trim());

    if (options != null && !options.isBlank()) {
      List<String> tokenize = ShellUtils.resolveQuotesWithoutRegex(options);
      for (String token : tokenize) {
          if(token.equals(" ")) {continue;}
        args.add(token);
      }
    }
    return args;
  }
}
