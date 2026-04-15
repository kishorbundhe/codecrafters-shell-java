package commands;

import pipe.PipelineStage;
import pipe.PipelineUtils;

import java.io.IOException;

public class PwdCommand implements Command {
    @Override
    public boolean execute(UserInput userInput) {
        System.out.println(System.getProperty("user.dir"));
        return true;
    }

    @Override
    public boolean execute(PipelineStage pipelineStage) {
        PipelineUtils.writeOutput(pipelineStage,System.getProperty("user.dir"));
        return true;
    }
}
