package commands;

import pipe.PipelineStage;
import pipe.PipelineUtils;

import java.nio.file.Path;

public class TypeCommand implements Command {
    @Override
    public boolean execute(UserInput userInput) {
        if (userInput.outputfile() != null) {
            Command.clearFiles(userInput.outputfile().getPath());
        }
        String options = ShellUtils.resolveQuotes(userInput.options());
        // since command = type, we need to check if options is a valid command
        if (!ValidCommand.isValidCommand(options)) {

            Pair<Boolean, Path> isCommandExecutableResult = Command.commandIsPresentAndExecutable(options);
            if (!isCommandExecutableResult.first()) {
                Command.commandNotFound(options);
            } else {
                System.out.println(options + " is " + isCommandExecutableResult.second());
            }
        }
        return true;
    }

    @Override
    public boolean execute(PipelineStage pipelineStage) {
        String options = ShellUtils.resolveQuotes(pipelineStage.getOptions());
        
        for (ValidCommand validCommand : ValidCommand.values()) {
            if (validCommand.getCommand().equals(options)) {
                PipelineUtils.writeOutput(pipelineStage, options + " is a shell builtin\n",false);
                return true;
            }
        }

        Pair<Boolean, Path> isCommandExecutableResult = Command.commandIsPresentAndExecutable(options);
        if (!isCommandExecutableResult.first()) {
            PipelineUtils.writeOutput(pipelineStage, options + ": not found\n",false);
        } else {
            PipelineUtils.writeOutput(pipelineStage, options + " is " + isCommandExecutableResult.second() + "\n",false);
        }
        return true;
    }
}
