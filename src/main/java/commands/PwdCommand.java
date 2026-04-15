package commands;

import pipe.PipelineStage;

import java.io.IOException;

public class PwdCommand implements Command {
    @Override
    public boolean execute(UserInput userInput) {
        System.out.println(System.getProperty("user.dir"));
        return true;
    }

    @Override
    public boolean execute(PipelineStage pipelineStage) {
        try {
            pipelineStage.getStdout().write(System.getProperty("user.dir").getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
