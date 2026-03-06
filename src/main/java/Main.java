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

        String regex = "(?<!\\\\)\"([^\"]*?)\"(?!\\\\)|(?<!\\\\)'([^']*?)'(?!\\\\)";
        Pattern pattern = Pattern.compile(regex);
        options.replaceAll("\"\"", "");
        options.replaceAll("\'\'", "");
        StringBuilder copyOptions = new StringBuilder(options);
        StringBuilder escapedOptions = new StringBuilder();
        while (true) {
            Matcher matcher = pattern.matcher(copyOptions.toString());
            int start = 0;
            if (matcher.find()) {
                if (matcher.start() != 0) {
                    String s = copyOptions.toString().substring(0, matcher.start());
                    String substr = s.replaceAll("\\s+", " ");
                    escapedOptions.append(substr);
                }
                escapedOptions.append(copyOptions, matcher.start() + 1, matcher.end() - 1);
                copyOptions.delete(start, matcher.end());
            } else {
                StringBuilder temporary = new StringBuilder();
                for(int i=start;i<copyOptions.length();i++){
                    char ch = copyOptions.charAt(i);
                    if(ch=='\\'){
                        i++;
                        temporary.append(copyOptions.charAt(i));
                        continue;
                    }
                    temporary.append(ch);
                }
                escapedOptions.append(temporary.toString().replaceAll("\\s+", " "));
                break;
            }
            start = matcher.start();
        }

        options = escapedOptions.toString();
        return options;
    }

}
