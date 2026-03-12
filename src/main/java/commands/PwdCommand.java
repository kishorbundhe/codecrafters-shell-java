package commands;

public class PwdCommand implements Command {
    @Override
    public boolean execute(UserInput userInput) {
        System.out.println(System.getProperty("user.dir"));
        return true;
    }
}
