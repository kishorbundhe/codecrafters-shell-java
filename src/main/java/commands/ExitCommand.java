package commands;

import pipe.PipelineStage;

public class ExitCommand implements Command {

    @Override
    public boolean execute(PipelineStage pipelineStage) {
        return false;
    }

}
