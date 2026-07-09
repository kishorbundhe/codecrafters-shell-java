package commands;

public enum ValidCommand {
    EXIT("exit"),
    TYPE("type"),
    PWD("pwd"),
    CD("cd"),
    ECHO("echo"),
    HISTORY("history");

    private final String command;

    ValidCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public static boolean isValidCommand(String command) {
        for (ValidCommand validCommand : ValidCommand.values()) {
            if (validCommand.getCommand().equals(command)) {
                System.out.println(command + " is a shell builtin");
                return true;
            }
        }
        return false;
    }

    public static boolean containsShellBuiltIn(String inputFromUser) {
        for (ValidCommand command : ValidCommand.values()) {
            if (inputFromUser.contains(command.getCommand())) {
                return true;
            }
        }
        return false;
    }
}
