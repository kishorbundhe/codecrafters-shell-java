package commands;

// contains state for user input, command, options,stdout file, stdErrFile
public record UserInput(String userInput, String command, String options , String stdOutFile, String stdErrFile) {
}
