package pipe;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class PipelineUtils {
  public static void writeOutput(PipelineStage stage, String content)  {
    try {
      if (stage.getStdout() != null) {
        stage.getStdout().write(content.getBytes());
        if (!(stage.getStdout() instanceof FileOutputStream)) {
            stage.getStdout().write(System.lineSeparator().getBytes());
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Pipeline communication error", e);
    }
  }
    public static void writeOutput(PipelineStage stage, String content, boolean newLine)  {
        try {
            if (stage.getStdout() != null) {
                stage.getStdout().write(content.getBytes());
                if (newLine) {
                    stage.getStdout().write(System.lineSeparator().getBytes());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Pipeline communication error", e);
        }
    }

  public static void writeError(PipelineStage stage, String content) {
    try {
      if (stage.getStderr() != null) {
        stage.getStderr().write(content.getBytes());
          if (!(stage.getStderr() instanceof FileOutputStream)) {
              stage.getStderr().write(System.lineSeparator().getBytes());
          }
      }
    } catch (IOException e) {
      throw new RuntimeException("Pipeline communication error", e);
    }
  }

  public static void writeOutput(PipelineStage stage, String content, Charset charset) {
    try {
      if (stage.getStdout() != null) {
        stage.getStdout().write(content.getBytes(charset));
        if (!(stage.getStdout() instanceof FileOutputStream)) {
            stage.getStdout().write(System.lineSeparator().getBytes());
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Pipeline communication error", e);
    }
  }

  public static void writeError(PipelineStage stage, String content, Charset charset) {
    try {
      if (stage.getStderr() != null) {
        stage.getStderr().write(content.getBytes(charset));
        if (!(stage.getStderr() instanceof FileOutputStream)) {
            stage.getStderr().write(System.lineSeparator().getBytes());
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Pipeline communication error", e);
    }
  }
}
