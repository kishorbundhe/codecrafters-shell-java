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
        options = escapeSingleQuotes(command, options);
        if (command.equals(ValidCommand.PWD.getCommand())) {
            return new PwdCommand().execute(command, options);
        } else if (command.equals(ValidCommand.CD.getCommand())) {
            return new CdCommand().execute(command, options);
        } else if (command.equals(ValidCommand.TYPE.getCommand())) {
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

    // 'world hello' 'shell''script' example''test
    private static String escapeSingleQuotes(String command, String options) {

        options = options.replaceAll("\'\'", "");
        Pattern pattern = Pattern.compile("'([^']*)'");
        String copyOptions = options;
        Matcher matcher = pattern.matcher(copyOptions);
        while (matcher.find()) {
            String toReplace = matcher.group(1); // group 0 =. 'world hello' group 1 = world hello
            toReplace = toReplace.replaceAll("\\s+", "@"); // replace spaces with @
            copyOptions = copyOptions.replaceFirst("'([^']*)'", toReplace);
        }
        options = copyOptions;
        options = options.replaceAll("\\s+", " ");
        options = options.replaceAll("@", " ");
        return options;
      
    }

}
