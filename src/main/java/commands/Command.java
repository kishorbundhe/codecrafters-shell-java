package commands;

import pipe.PipelineStage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public interface Command {
    boolean execute(UserInput userInput);
    boolean execute(PipelineStage pipelineStage);

    public static Pair<Boolean, Path> commandIsPresentAndExecutable(String command) {
        // get the env PATH variable and check if the command exists in any of the
        // directories in PATH
        String path = System.getenv("PATH");
        String[] directories = path.split(File.pathSeparator);
        for (String dir : directories) {
            // list all files in the directory and check if the command exists
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

    static void commandNotFound(String command) {
        System.out.println(command + ": not found");
    }

     static void clearFiles(String filePath) {
         try {
             BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath));
             writer.write("");
             writer.flush();
         } catch (IOException e) {}
     }
}
