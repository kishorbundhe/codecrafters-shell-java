import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        if (command.equals(ValidCommand.TYPE.getCommand())) {
            type(options);
        } else if (command.equals(ValidCommand.EXIT.getCommand()))
            return false;
        else if (command.equals(ValidCommand.ECHO.getCommand())) {
            echo(options);
        } else {
            Pair<Boolean, Path> commandIsPresentAndExecutable = commandIsPresentAndExecutable(command);
            Boolean isCommandPresentInSysPath = commandIsPresentAndExecutable.first();
            Path path = commandIsPresentAndExecutable.second();
            if (isCommandPresentInSysPath) {
                return handleExecutableCommand(path, options);
            } else
                commandNotFound(inputFromUser);
        }

        return true;
    }

    private static boolean handleExecutableCommand(Path command, String options) {
        if (options.isEmpty()) {
            throw new UnsupportedOperationException("there should be options for this command");
        }

        List<String> args = Stream.concat(
                Stream.of(command.toString()),
                Arrays.stream(options.split(" ")))
                .collect(Collectors.toList());

        ProcessBuilder processBuilder = new ProcessBuilder(args);
        processBuilder.inheritIO();
        try {
            Process process = processBuilder.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    private static void type(String options) {
        // since command = type, we need to check if options is a valid command
        if (!ValidCommand.isValidCommand(options)) {

            Pair<Boolean, Path> isCommandExecutableResult = commandIsPresentAndExecutable(options);
            if (!isCommandExecutableResult.first()) {
                commandNotFound(options);
            } else {
                System.out.println(options + " is " + isCommandExecutableResult.second());
            }
        }
    }

    private static Pair<Boolean, Path> commandIsPresentAndExecutable(String command) {
        // get the env PATH variable and check if the command exists in any of the
        // directories in PATH
        String path = System.getenv("PATH");
        String[] directories = path.split(File.pathSeparator);

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

    private static void echo(String options) {
        options = options.replaceAll("^\"|\"$", ""); // remove surrounding quotes if present
        System.out.println(options);
    }
}
