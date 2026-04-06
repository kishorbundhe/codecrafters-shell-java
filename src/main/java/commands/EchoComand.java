package commands;

public class EchoComand implements Command {
  @Override
  public boolean execute(UserInput userInput) {
    Command.clearFiles(userInput.outputfile().getPath());
    String options = userInput.options();
    // options = options.replaceAll("^\"|\"$", ""); // remove surrounding quotes if present
    System.out.println(options);
    return true;
  }
}
