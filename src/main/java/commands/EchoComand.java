package commands;

import pipe.PipelineStage;
import pipe.PipelineUtils;

public class EchoComand implements Command {
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
