package commands;

public class PwdCommand implements Command {
    @Override
    public boolean execute(String command, String options) {
        System.out.println(System.getProperty("user.dir"));
        return true;
    }
}
