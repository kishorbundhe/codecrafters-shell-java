package commands;

import org.jline.reader.History;
import pipe.PipelineStage;
import pipe.PipelineUtils;

import java.util.ArrayList;

public class HistoryCommand implements Command {

  private static final ArrayList<String> history = new ArrayList<>();

  @Override
  public boolean execute(PipelineStage pipelineStage) {
    for (int i = 0; i < history.size(); i++) {
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
