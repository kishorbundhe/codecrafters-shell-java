package commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomExecutableTest {
  CustomExecutable executable = new CustomExecutable();

  @Test
  void prepareArguments() {
    String options = "/tmp/\"number 1\" /tmp/\"doublequote \\\" 2\" /tmp/\"backslash \\\\ 3\"";
   // options = options.replace("\"","\\\"");
    System.out.println(ShellUtils.resolveQuotes(options));
  }
  @Test
    void example1(){
      String options = "/tmp/rat/\"number 1\"";
      System.out.println(ShellUtils.resolveQuotesWithoutRegex(options));
  }
}
