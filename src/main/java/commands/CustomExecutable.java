package commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import InputProcessor.InputProcessor;

public class CustomExecutable implements Command {
  @Override
  public boolean execute(UserInput userInput) {
    if (userInput.userInput().contains("|")) {
      // process piped command
      List<ProcessBuilder> processBuilders = buildPipeBasedProcess(userInput.userInput());
      executePipeBasedCommand(processBuilders);
      return true;
    }

    // process without pipe command
    ProcessBuilder processBuilder = buildNonPipeBasedProcess(userInput);
    executeNonPipeBasedCommand(processBuilder);
    return true;
  }

  private ProcessBuilder buildNonPipeBasedProcess(UserInput userInput) {
    BuildProcessArguments result = getBuildProcessArguments(userInput);
    ProcessBuilder processBuilder = new ProcessBuilder(result.args());
    if (userInput.inputFile() != null) {
      processBuilder.redirectInput(ProcessBuilder.Redirect.from(userInput.inputFile()));
      processBuilder.redirectOutput(ProcessBuilder.Redirect.to(userInput.outputfile()));
    } else {
      processBuilder.inheritIO();
    }
    if (result.stdFileName() != null && !result.stdFileName().isEmpty()) {
      if (result.stdOutAppend()) {
        processBuilder.redirectOutput(
            ProcessBuilder.Redirect.appendTo(new File(result.stdFileName())));
      } else {
        processBuilder.redirectOutput(new File(result.stdFileName()));
      }
    }
    if (result.stdErrFileName() != null && !result.stdErrFileName().isEmpty()) {
      if (result.stdErrAppend()) {
        processBuilder.redirectError(
            ProcessBuilder.Redirect.appendTo(new File(result.stdErrFileName())));
      } else {
        processBuilder.redirectError(new File(result.stdErrFileName()));
      }
    }
    return processBuilder;
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
    String command = userInput.command();
    String options = userInput.options();
    String stdFileName = userInput.stdOutFile().stdOutFile();
    boolean stdOutAppend = userInput.stdOutFile().append();
    String stdErrFileName = userInput.stdErrFile().stdErrFile();
    boolean stdErrAppend = userInput.stdErrFile().append();
    List<String> args;
    if (command.equalsIgnoreCase("cat")) {
      args = getCatCommandAndFiles(command, options);
    } else {
      args = getCommandArgs(command, options);
    }
    return new BuildProcessArguments(stdFileName, stdOutAppend, stdErrFileName, stdErrAppend, args);
  }

  private record BuildProcessArguments(
      String stdFileName,
      boolean stdOutAppend,
      String stdErrFileName,
      boolean stdErrAppend,
      List<String> args) {}

  private List<ProcessBuilder> buildPipeBasedProcess(String unChangedInputFromUser) {
    InputProcessor inputProcessor = new InputProcessor();
    String[] split = unChangedInputFromUser.split("\\|");
    List<String> commands = Arrays.stream(split).map(String::trim).toList();
    List<UserInput> parsedInputs =
        commands.stream().map(c -> inputProcessor.parseUserInput(c, null, null)).toList();
    List<ProcessBuilder> processBuilders =
        parsedInputs.stream()
            .map(this::getBuildProcessArguments)
            .map(bpa -> new ProcessBuilder(bpa.args))
            .toList();
    return processBuilders;
  }

  private List<String> getCommandArgs(String command, String options) {
    if (options.isBlank()) {
      List<String> optionsList = new ArrayList<>();
      optionsList.add(command);
      return optionsList;
    }
    return Stream.concat(Stream.of(command), Arrays.stream(options.split(" ")))
        .collect(Collectors.toList());
  }

  private List<String> getCatCommandAndFiles(String command, String options) {
    List<String> args;
    ArrayList<String> files = escapeQuotes(options);
    args = Stream.concat(Stream.of(command), files.stream()).collect(Collectors.toList());
    return args;
  }

  private ArrayList<String> escapeQuotes(String options) {
    ArrayList<String> files = new ArrayList<>();
    String regex = "(?<!\\\\)\"((?:\\\\.|[^\"\\\\])*)\"(?!\\\\)|(?<!\\\\)'([^']*?)'(?!\\\\)";
    Pattern pattern = Pattern.compile(regex);

    options = options.replace("\"\"", "");
    options = options.replace("''", "");
    StringBuilder copyOptions = new StringBuilder(options);

    while (true) {
      Matcher matcher = pattern.matcher(copyOptions.toString());
      int start = 0;
      if (matcher.find()) {
        StringBuilder escapedOptions = new StringBuilder();
        if (matcher.start() != 0) {
          beforeMatch(copyOptions, matcher, escapedOptions);
        }
        if (copyOptions.charAt(matcher.start()) == '\"') {
          // Double quotes
          processBackSlashInsideDoubleQuotes(files, copyOptions, matcher, start, escapedOptions);

        } else {
          // single quotes
          processBackSlashInsideSingleQuotes(files, copyOptions, matcher, start, escapedOptions);
        }

      } else {
        StringBuilder temporary = new StringBuilder();
        if (copyOptions.isEmpty()) break;
        for (int i = start; i < copyOptions.length(); i++) {
          char ch = copyOptions.charAt(i);
          if (ch == '\\') {
            i++;
            temporary.append(copyOptions.charAt(i));
            continue;
          }
          temporary.append(ch);
        }
        String nameOfFiles[] = temporary.toString().split(" ");
        Arrays.stream(nameOfFiles).forEach(file -> files.add(file));
        break;
      }
      start = matcher.start();
    }

    return files;
  }

  private void processBackSlashInsideDoubleQuotes(
      ArrayList<String> files,
      StringBuilder copyOptions,
      Matcher matcher,
      int start,
      StringBuilder escapedOptions) {
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
    escapedOptions.append(temporary.toString());
    files.add(escapedOptions.toString());
    copyOptions.delete(start, matcher.end());
  }

  private void processBackSlashInsideSingleQuotes(
      ArrayList<String> files,
      StringBuilder copyOptions,
      Matcher matcher,
      int start,
      StringBuilder escapedOptions) {
    files.add(
        escapedOptions.append(copyOptions, matcher.start() + 1, matcher.end() - 1).toString());
    copyOptions.delete(start, matcher.end());
  }

  private void beforeMatch(
      StringBuilder copyOptions, Matcher matcher, StringBuilder escapedOptions) {
    String s = copyOptions.toString().substring(0, matcher.start());
    String substr = s.replaceAll("\\s+", "");
    if (!substr.isBlank()) escapedOptions.append(substr);
  }
}
