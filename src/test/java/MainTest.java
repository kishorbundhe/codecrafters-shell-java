import InputProcessor.InputProcessor;
import org.junit.jupiter.api.Test;
import pipe.PipelineStage;
import pipe.PipelineUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    private final InputProcessor inputProcessor = new InputProcessor();

    @Test
    void singleCommand() {
        List<PipelineStage> stages =
                PipelineUtils.getPipelineStages("echo hello", inputProcessor);

        assertEquals(1, stages.size());
        assertEquals("echo", stages.get(0).getCommand());
    }

    @Test
    void twoStagePipeline() {
        List<PipelineStage> stages =
                PipelineUtils.getPipelineStages(
                        "echo hello | wc",
                        inputProcessor);

        assertEquals(2, stages.size());
        assertEquals("echo", stages.get(0).getCommand());
        assertEquals("wc", stages.get(1).getCommand());
    }

    @Test
    void threeStagePipeline() {
        List<PipelineStage> stages =
                PipelineUtils.getPipelineStages(
                        "cat file | grep abc | wc",
                        inputProcessor);

        assertEquals(3, stages.size());
        assertEquals("cat", stages.get(0).getCommand());
        assertEquals("grep", stages.get(1).getCommand());
        assertEquals("wc", stages.get(2).getCommand());
    }

    @Test
    void trimsWhitespaceAroundPipe() {
        List<PipelineStage> stages =
                PipelineUtils.getPipelineStages(
                        " echo hello   |   wc ",
                        inputProcessor);

        assertEquals(2, stages.size());
        assertEquals("echo", stages.get(0).getCommand());
        assertEquals("wc", stages.get(1).getCommand());
    }

    @Test
    void preservesArguments() {
        List<PipelineStage> stages =
                PipelineUtils.getPipelineStages(
                        "echo hello world | grep hello",
                        inputProcessor);

        assertEquals(2, stages.size());

        assertEquals("echo", stages.get(0).getCommand());
        assertEquals("hello world", stages.get(0).getOptions());

        assertEquals("grep", stages.get(1).getCommand());
        assertEquals("hello", stages.get(1).getOptions());
    }
}
