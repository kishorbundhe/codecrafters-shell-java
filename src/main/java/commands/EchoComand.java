package commands;
public class EchoComand implements Command {
    @Override
    public boolean execute(String command, String options) {
        //options = options.replaceAll("^\"|\"$", ""); // remove surrounding quotes if present
        System.out.println(options);
        return true;
    }

}
