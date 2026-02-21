import static commands.Command.commandIsPresentAndExecutable;
import static commands.Command.commandNotFound;

import java.nio.file.Path;
import java.util.Scanner;

import commands.CustomExecutable;
import commands.EchoComand;
import commands.ExitCommand;
import commands.Pair;
import commands.PwdCommand;
import commands.TypeCommand;

public class Main {
    public static void main(String[] args) throws Exception {
        try (Scanner scanner = new Scanner(System.in)) {
            for (;;) {
                if (!shouldContinueRunningCommand(scanner)) {
                    break;
                }
            }
        }
    }

    private static boolean shouldContinueRunningCommand(Scanner scanner) {
        System.out.print("$ ");
        String inputFromUser = scanner.nextLine();
        String command = inputFromUser.split(" ")[0];
        String options = inputFromUser.replaceFirst(command, "").trim();

        if(command.equals(ValidCommand.PWD.getCommand())) {
            return new PwdCommand().execute(command, options);
        }
        if (command.equals(ValidCommand.TYPE.getCommand())) {
            return new TypeCommand().execute(command, options);
        } else if (command.equals(ValidCommand.EXIT.getCommand()))
            return new ExitCommand().execute(command, options);
        else if (command.equals(ValidCommand.ECHO.getCommand())) {
            return new EchoComand().execute(command, options);
        } else {
            Pair<Boolean, Path> commandIsPresentAndExecutable = commandIsPresentAndExecutable(command);
            Boolean isCommandPresentInSysPath = commandIsPresentAndExecutable.first();
            Path path = commandIsPresentAndExecutable.second();
            if (isCommandPresentInSysPath) {
                return new CustomExecutable().execute(path.getFileName().toString(), options);
            } else
                commandNotFound(inputFromUser);
        }

        return true;
    }

}
