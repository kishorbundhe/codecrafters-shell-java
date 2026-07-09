package commands;

import pipe.PipelineStage;

public class HistoryCommand implements Command {

    @Override
    public boolean execute(PipelineStage pipelineStage) {
        return true;
    }
}
