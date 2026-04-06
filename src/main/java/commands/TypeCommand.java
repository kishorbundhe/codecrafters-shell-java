package commands;

import java.nio.file.Path;

public class TypeCommand implements Command {
    @Override
    public boolean execute(UserInput userInput) {
        Command.clearFiles(userInput.outputfile().getPath());
        String options = userInput.options();
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

}
