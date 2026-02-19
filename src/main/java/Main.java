import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        for(;;) {
            getCommand();
        }
    }

    private static void getCommand() {
        System.out.print("$ ");
        Scanner scanner = new Scanner(System.in);
        String command = scanner.nextLine();
        System.out.println(command + ": command not found");
    }
}
