import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        for (;;) {
            if (!shouldContinueRunningCommand()) {
                break;
            }
        }
    }

    private static boolean shouldContinueRunningCommand() {
        System.out.print("$ ");
        Scanner scanner = new Scanner(System.in);
        String command = scanner.nextLine();

        if (command.contains("type")) {
            type(command);
        } else if (command.contains("exit"))
            return false;
        else if (command.contains("echo")) {
            echo(command);
        } else
            comandNotFound(command);

        return true;
    }

    private static void type(String command) {
        command = command
                .replaceFirst("type", "")
                .trim();
        ValidCommand.isValidCommand(command);
    }

    private static void comandNotFound(String command) {
        System.out.println(command + ": command not found");
    }

    private static void echo(String command) {
        command = command
                .replaceFirst("echo", "")
                .trim()
                .replace("\"", "");
        System.out.println(command);
    }
}
