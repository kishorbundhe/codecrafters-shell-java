package commands;

public class ExitCommand implements Command {
    @Override
    public boolean execute(UserInput userInput) {
    
        return false; // returning false to indicate that the shell should exit
    }

}
