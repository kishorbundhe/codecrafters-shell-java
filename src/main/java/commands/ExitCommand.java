package commands;

public class ExitCommand implements Command {
    @Override
    public boolean execute(String command, String options) {

        return false; // returning false to indicate that the shell should exit
    }

}
