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
        if (command.contains("exit"))
            return false;
        else if (command.contains("echo"))
            System.out.println("echo " + command.substring(5));
        else
            System.out.println(command + ": command not found");

        return true;
    }
}
