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
}
