package commands;

import java.nio.file.Files;
import java.nio.file.Path;

public class CdCommand implements Command {

    @Override
    public boolean execute(String command, String options) {
        Path newPath = Path.of(options);
        if (newPath.isAbsolute()) {
            newPath = newPath.normalize();
        } else {
            Path path = Path.of(System.getProperty("user.dir")).resolve(newPath);
            newPath = path.normalize();
        }
        if (Files.exists(newPath) && Files.isDirectory(newPath)) {
            System.setProperty("user.dir", newPath.toString());
        } else {
            System.out.println("cd: " + options + ": No such file or directory");
        }

        return true;
    }

}
