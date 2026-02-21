import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Scanner;

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
        String command = scanner.nextLine();

        if (command.contains(ValidCommand.TYPE.getCommand())) {
            type(command);
        } else if (command.contains(ValidCommand.EXIT.getCommand()))
            return false;
        else if (command.contains(ValidCommand.ECHO.getCommand())) {
            echo(command);
        } else
            commandNotFound(command);

        return true;
    }

    private static void type(String command) {
        command = command
                .replaceFirst(ValidCommand.TYPE.getCommand(), "")
                .trim();
        if (!ValidCommand.isValidCommand(command)) {
            // get the env PATH variable and check if the command exists in any of the
            // directories in PATH
            String path = System.getenv("PATH");
            String[] directories = path.split(File.pathSeparator);

            Pair<Boolean, Path> result = commandIsPresentAndExecutable(command, directories);
            if (!result.first()) {
                commandNotFound(command);
            }
        }
    }

    private static Pair<Boolean, Path> commandIsPresentAndExecutable(String command, String[] directories) {

        for (String dir : directories) {
            // list all files in the directory and check if the command exists
            if (dir.contains(command)) {
                System.out.println(command + " is " + dir);
                return new Pair<>(true, Paths.get(dir));
            }
            Path dirPath = Paths.get(dir);
            final String finalCommand = command;
            boolean isDirectory = Files.isDirectory(dirPath);
            if (isDirectory) {
                try {
                    Optional<Path> foundPath = Files.list(dirPath)
                            .filter(file -> file.getFileName().toString().equals(finalCommand)
                                    && Files.isExecutable(file))
                            .findFirst();
                    if (foundPath.isPresent()) {
                        System.out.println(command + " is " + foundPath.get());
                        return new Pair<>(true, foundPath.get());
                    }
                } catch (IOException e) {

                    e.printStackTrace();
                }
            }

        }
        return new Pair<>(false, null);
    }

    private static void commandNotFound(String command) {
        System.out.println(command + ": not found");
    }

    private static void echo(String command) {
        command = command
                .replaceFirst(ValidCommand.ECHO.getCommand(), "")
                .trim()
                .replace("\"", "");
        System.out.println(command);
    }
}
