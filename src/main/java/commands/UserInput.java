package commands;

import java.io.File;

// contains state for user input, command, options,stdout file, stdErrFile
public record UserInput(String userInput,
        String command,
        String options,
        StdOutFile stdOutFile,
        StdErrFile stdErrFile,
        File inputFile,File outputfile) {
}
