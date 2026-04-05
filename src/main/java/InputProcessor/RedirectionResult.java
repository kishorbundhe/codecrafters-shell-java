package InputProcessor;

import commands.StdErrFile;
import commands.StdOutFile;

public record RedirectionResult(String cleanedInput, StdOutFile stdOut, StdErrFile stdErr) {}
