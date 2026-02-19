public enum ValidCommand {
    EXIT("exit"),
    TYPE("type"),
    ECHO("echo");

    private final String command;

    ValidCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public static void isValidCommand(String command) {
        for (ValidCommand validCommand : ValidCommand.values()) {
            if (validCommand.getCommand().equals(command)) {
                System.out.println(command + " is a shell builtin");
                return;
            }
        }
        System.out.println(command + ": not found");
    }
}
