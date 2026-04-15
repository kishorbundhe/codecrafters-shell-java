package commands;

import java.nio.file.Files;
import java.nio.file.Path;

import pipe.PipelineStage;
import pipe.PipelineUtils;

public class CdCommand implements Command {

  @Override
  public boolean execute(UserInput userInput) {
    String options = userInput.options();
    Path newPath;
    if (options.equalsIgnoreCase("~")) {
      newPath = Path.of(System.getenv("HOME"));
      cdToPath(null, newPath);
      return true;
    }
    newPath = Path.of(options);
    if (newPath.isAbsolute()) {
      newPath = newPath.resolve(options).normalize();
    } else {
      // relative
      Path path = Path.of(System.getProperty("user.dir")).resolve(newPath);
      newPath = path.normalize();
    }
    cdToPath(options, newPath);
    return true;
  }

  @Override
  public boolean execute(PipelineStage pipelineStage) {
    String options = pipelineStage.getOptions();
    Path newPath;
    if (options.equalsIgnoreCase("~")) {
      newPath = Path.of(System.getenv("HOME"));
      cdToPath(null, newPath);
      return true;
    }
    newPath = Path.of(options);
    if (newPath.isAbsolute()) {
      newPath = newPath.resolve(options).normalize();
    } else {
      // relative
      Path path = Path.of(System.getProperty("user.dir")).resolve(newPath);
      newPath = path.normalize();
    }
    if (Files.exists(newPath) && Files.isDirectory(newPath)) {
      System.setProperty("user.dir", newPath.toString());
    } else {
      PipelineUtils.writeOutput(pipelineStage, "cd: " + options + ": No such file or directory");
    }
    return true;
  }

  private void cdToPath(String options, Path newPath) {
    if (Files.exists(newPath) && Files.isDirectory(newPath)) {
      System.setProperty("user.dir", newPath.toString());
    } else {
      System.out.println("cd: " + options + ": No such file or directory");
    }
  }
}
