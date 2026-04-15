package commands;

import pipe.PipelineStage;

public class ExitCommand implements Command {
    @Override
    public boolean execute(UserInput userInput) {
    
        return false; // returning false to indicate that the shell should exit
    }

    @Override
    public boolean execute(PipelineStage pipelineStage) {
        return false;
    }

}
