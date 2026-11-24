package commands;

import pipe.PipelineStage;
import pipe.PipelineUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import static java.nio.file.Files.lines;
import static java.nio.file.Files.write;

public class HistoryCommand implements Command {

  private static final ArrayList<String> history = new ArrayList<>();

  private static final ArrayList<HistoryOptionSupport> options = new ArrayList<>();

  public HistoryCommand() {
    options.add(new HistoryRCommandSupport());
    options.add(new HistoryWCommandSupport());
  }

  @Override
  public boolean execute(PipelineStage pipelineStage) {
    //    if (!pipelineStage.getOptions().isEmpty() && pipelineStage.getOptions().contains("-r")) {
    //      return readFromFile(pipelineStage);
    //    }
    //    if (!pipelineStage.getOptions().isEmpty() && pipelineStage.getOptions().contains("-w")) {
    //      return writeToFile(pipelineStage);
    //    }
    //    if (!pipelineStage.getOptions().isEmpty() && pipelineStage.getOptions().contains("-a")) {
    //      return appendToFile(pipelineStage);
    //    }
    if (!pipelineStage.getOptions().isEmpty()) {
      options.forEach(option -> option.execute(pipelineStage, history));
      return true;
    }

    int n = history.size();
    try {
      n = Integer.parseInt(pipelineStage.getOptions());
    } catch (Exception e) {
    }
    // 10 , n= 2   9th and 10th
    if (!pipelineStage.getOptions().isEmpty() && n < history.size()) {
      for (int i = 0; i < n; i++) {
        // reverse order printing
        PipelineUtils.writeOutput(
            pipelineStage,
            (history.size() + 1 - n) + i + " " + history.get(history.size() - n + i),
            true);
      }
      return true;
    }
    for (int i = 0; i < n; i++) {
      PipelineUtils.writeOutput(pipelineStage, i + 1 + " " + history.get(i), true);
    }
    return true;
  }

  private boolean appendToFile(PipelineStage pipelineStage) {
    return false;
  }

  private boolean writeToFile(PipelineStage pipelineStage) {
    String[] temp = pipelineStage.getOptions().split(" ");
    String path = temp[1];
    try {
      File file = Files.createFile(Path.of(path)).toFile();
      for (String str : history) {
        Files.write(Path.of(file.getAbsolutePath()), str.getBytes(), StandardOpenOption.APPEND);
        Files.write(
            Path.of(file.getAbsolutePath()),
            System.lineSeparator().getBytes(),
            StandardOpenOption.APPEND);
      }
    } catch (IOException e) {
      // ignore
    }
    return true;
  }

  private static boolean readFromFile(PipelineStage pipelineStage) {
    String[] temp = pipelineStage.getOptions().split(" ");
    String path = temp[1];
    try {
      lines(Path.of(path)).forEach(history::add);
    } catch (IOException e) {
      // ignore
    }
    return true;
  }

  public static void add(String userInput) {
    if (!userInput.isEmpty()) history.add(userInput.trim());
  }

  public static void clearHistory() {
    history.clear();
  }

  public static void removeEntry(String userInput) {
    history.remove(userInput);
  }
}

interface HistoryOptionSupport {
  boolean execute(PipelineStage stage, ArrayList<String> history);
}

class HistoryRCommandSupport implements HistoryOptionSupport {

  @Override
  public boolean execute(PipelineStage stage, ArrayList<String> history) {
    boolean isRoption = stage.getOptions().contains("-r");
    if (!isRoption) {
      return false;
    } else {
      String[] temp = stage.getOptions().split(" ");
      String path = temp[1];
      try {
        lines(Path.of(path)).forEach(history::add);
      } catch (IOException e) {
        // ignore
      }
    }
    return true;
  }
}

class HistoryWCommandSupport implements HistoryOptionSupport {
  @Override
  public boolean execute(PipelineStage stage, ArrayList<String> history) {
    boolean isWoption = stage.getOptions().contains("-w");
    if (!isWoption) return false;
    else {
      String[] temp = stage.getOptions().split(" ");
      String path = temp[1];
      try {
        File file = Files.createFile(Path.of(path)).toFile();
        for (String str : history) {
          Files.write(Path.of(file.getAbsolutePath()), str.getBytes(), StandardOpenOption.APPEND);
          Files.write(
              Path.of(file.getAbsolutePath()),
              System.lineSeparator().getBytes(),
              StandardOpenOption.APPEND);
        }
      } catch (IOException e) {
        // ignore
      }
    }
    return true;
  }
}

class HistoryACommandSupport implements HistoryOptionSupport {

    @Override
    public boolean execute(PipelineStage stage, ArrayList<String> history) {
        return false;
    }
}