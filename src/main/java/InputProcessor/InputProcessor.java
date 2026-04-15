package InputProcessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import commands.ShellUtils;
import commands.StdErrFile;
import commands.StdOutFile;
import commands.UserInput;
import pipe.PipelineStage;

public class InputProcessor {

  public UserInput parseUserInput(String inputFromUser, File previousFile, File currentOutput) {
    RedirectionResult redirection = extractRedirectionInfo(inputFromUser);
    setupRedirectionStreams(redirection.stdOut(), redirection.stdErr());
    CommandParts commandParts = parseCommandAndOptions(redirection.cleanedInput());

    return new UserInput(
        redirection.cleanedInput(),
        commandParts.command(),
        commandParts.options(),
        redirection.stdOut(),
        redirection.stdErr(),
        previousFile,
        currentOutput);
  }


  public PipelineStage parsePipelineStage(String inputFromPipeline) {
      PipelineStage pipelineStage = new PipelineStage();
      pipelineStage.setStdin(System.in);
      pipelineStage.setStdout(System.out);
      pipelineStage.setStderr(System.err);
      RedirectionResult redirection = extractRedirectionInfo(inputFromPipeline);
      StdOutFile stdOutFile = redirection.stdOut();
      if (!stdOutFile.stdOutFile().isEmpty()) {
          try {
              FileOutputStream fos = new FileOutputStream(stdOutFile.stdOutFile(), stdOutFile.append());
              pipelineStage.setStdout(fos);
              pipelineStage.setOutputRedirect(stdOutFile.append()
                      ? ProcessBuilder.Redirect.appendTo(new File(stdOutFile.stdOutFile()))
                      : ProcessBuilder.Redirect.to(new File(stdOutFile.stdOutFile())));
          } catch (FileNotFoundException e) {
              e.printStackTrace();
          }
      }
      StdErrFile stdErrFile = redirection.stdErr();
      if (!stdErrFile.stdErrFile().isEmpty()) {
          try {
              FileOutputStream fos = new FileOutputStream(stdErrFile.stdErrFile(), stdErrFile.append());
              pipelineStage.setStderr(fos);
              pipelineStage.setErrorRedirect(stdErrFile.append()
                      ? ProcessBuilder.Redirect.appendTo(new File(stdErrFile.stdErrFile()))
                      : ProcessBuilder.Redirect.to(new File(stdErrFile.stdErrFile())));
          } catch (FileNotFoundException e) {
              e.printStackTrace();
          }
      }
      CommandParts commandParts = parseCommandAndOptions(redirection.cleanedInput());
      pipelineStage.setCommand(commandParts.command());
      pipelineStage.setOptions(commandParts.options());
      return pipelineStage;
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

  private void setupRedirectionStreams(StdOutFile stdOut, StdErrFile stdErr) {

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

  private CommandParts parseCommandAndOptions(String input) {
    String command = "";
    String options = "";

    if (input.startsWith("\"")) {
      String regex = "\"([^\"]*)\"";
      Matcher matcher = Pattern.compile(regex).matcher(input);
      boolean hasMatch = matcher.find();
      if (hasMatch) {
          // group 0 is excluding double quotes
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
