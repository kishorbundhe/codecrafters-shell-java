package commands;

import pipe.PipelineStage;
import pipe.PipelineUtils;

public class EchoComand implements Command {
  @Override
  public boolean execute(UserInput userInput) {
    if (userInput.outputfile() != null) {
      Command.clearFiles(userInput.outputfile().getPath());
    }
    userInput = prepareEchoInput(userInput);
    String options = userInput.options();
    System.out.println(options);
    return true;
  }

    @Override
    public boolean execute(PipelineStage pipelineStage) {
        String escapeOptions = ShellUtils.resolveQuotes(pipelineStage.getOptions());
        PipelineUtils.writeOutput(pipelineStage, escapeOptions);
        return true;
    }

    private static UserInput prepareEchoInput(UserInput userInput) {
    String escapeOptions = ShellUtils.resolveQuotes(userInput.options());
    return new UserInput(
        "",
        userInput.command(),
        escapeOptions,
        userInput.stdOutFile(),
        userInput.stdErrFile(),
        userInput.inputFile(),
        userInput.outputfile());
  }
}
