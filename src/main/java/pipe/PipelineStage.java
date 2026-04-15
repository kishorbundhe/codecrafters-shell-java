package pipe;

import java.io.InputStream;
import java.io.OutputStream;

public class PipelineStage {

  String command;
  String options;
  InputStream stdin; // from where shell builtin should take input
  OutputStream stdout; // from where shell builtin should take output
  OutputStream stderr; // from where shell builtin should send error output
  ProcessBuilder.Redirect inputRedirect; // since it is just file name, only used for process
  ProcessBuilder.Redirect outputRedirect; // since it is file name, only used for process
  ProcessBuilder.Redirect errorRedirect; // since it is file name , only used for process

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public String getOptions() {
    return options;
  }

  public void setOptions(String options) {
    this.options = options;
  }

  public InputStream getStdin() {
    return stdin;
  }

  public void setStdin(InputStream stdin) {
    this.stdin = stdin;
  }

  public OutputStream getStdout() {
    return stdout;
  }

  public void setStdout(OutputStream stdout) {
    this.stdout = stdout;
  }

  public OutputStream getStderr() {
    return stderr;
  }

  public void setStderr(OutputStream stderr) {
    this.stderr = stderr;
  }

  public ProcessBuilder.Redirect getInputRedirect() {
    return inputRedirect;
  }

  public void setInputRedirect(ProcessBuilder.Redirect inputRedirect) {
    this.inputRedirect = inputRedirect;
  }

  public ProcessBuilder.Redirect getOutputRedirect() {
    return outputRedirect;
  }

  public void setOutputRedirect(ProcessBuilder.Redirect outputRedirect) {
    this.outputRedirect = outputRedirect;
  }

  public ProcessBuilder.Redirect getErrorRedirect() {
    return errorRedirect;
  }

  public void setErrorRedirect(ProcessBuilder.Redirect errorRedirect) {
    this.errorRedirect = errorRedirect;
  }
}
