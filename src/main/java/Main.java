import static commands.Command.commandIsPresentAndExecutable;
import static commands.Command.commandNotFound;

import java.nio.file.Path;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import commands.CdCommand;
import commands.CustomExecutable;
import commands.EchoComand;
import commands.ExitCommand;
import commands.Pair;
import commands.PwdCommand;
import commands.TypeCommand;
import commands.ValidCommand;

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
        if (inputFromUser.isBlank()) {
            return true;
        }
        String command = inputFromUser.split(" ")[0];
        String options = inputFromUser.replaceFirst(command, "").trim();
        if (command.equals(ValidCommand.PWD.getCommand())) {
            return new PwdCommand().execute(command, options);
        } else if (command.equals(ValidCommand.CD.getCommand())) {
            return new CdCommand().execute(command, options);
        } else if (command.equals(ValidCommand.TYPE.getCommand())) {
            return new TypeCommand().execute(command, options);
        } else if (command.equals(ValidCommand.EXIT.getCommand()))
            return new ExitCommand().execute(command, options);
        else if (command.equals(ValidCommand.ECHO.getCommand())) {
            options = escapeSingleQuotes(command, options);
            return new EchoComand().execute(command, options);
        } else {
            Pair<Boolean, Path> commandIsPresentAndExecutablePair = commandIsPresentAndExecutable(command);
            Boolean isCommandPresentInSysPath = commandIsPresentAndExecutablePair.first();
            Path path = commandIsPresentAndExecutablePair.second();
            if (isCommandPresentInSysPath) {
                return new CustomExecutable().execute(path.getFileName().toString(), options);
            } else
                commandNotFound(inputFromUser);
        }

        return true;
    }

    // 'world hello' 'shell''script' example''test
    private static String escapeSingleQuotes(String command, String options) {
        Pattern pattern;
        String regex;
        if (options.startsWith("\"")) {
            options = options.replaceAll("\"\"", "");
            regex = "\"([^\"]*)\"";
            pattern = Pattern.compile(regex);
        } else {
            options = options.replaceAll("\'\'", "");
            regex = "'([^']*)'";
            pattern = Pattern.compile(regex);
        }

        String copyOptions = options;
        Matcher matcher = pattern.matcher(copyOptions);
        while (matcher.find()) {
            String toReplace = matcher.group(1); // group 0 =. 'world hello' group 1 = world hello
            toReplace = toReplace.replaceAll(" ", "@"); // replace spaces with @
            copyOptions = copyOptions.replaceFirst(regex, toReplace);
        }
        options = copyOptions;
        options = options.replaceAll("\\s+", " ");
        options = options.replaceAll("@", " ");
        return options;

    }

}
