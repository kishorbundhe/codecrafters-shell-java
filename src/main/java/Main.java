import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        for(;;) {
            if (!shouldContinueRunningCommand()) {
                break;
            }
        }
    }

    private static boolean shouldContinueRunningCommand() {
        System.out.print("$ ");
        Scanner scanner = new Scanner(System.in);
        String command = scanner.nextLine();
        if(command.equals("exit")) {
            return false;
        }
        System.out.println(command + ": command not found");
        return true;
    }
}
