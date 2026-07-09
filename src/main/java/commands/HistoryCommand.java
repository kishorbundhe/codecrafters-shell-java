package commands;

import org.jline.reader.History;
import pipe.PipelineStage;
import pipe.PipelineUtils;

import java.util.ArrayList;

public class HistoryCommand implements Command {

  private static final ArrayList<String> history = new ArrayList<>();

  @Override
  public boolean execute(PipelineStage pipelineStage) {
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
