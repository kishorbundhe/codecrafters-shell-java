package commands;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomExecutableTest {
  CustomExecutable executable = new CustomExecutable();

  @Test
  void prepareArguments() {
    String options = "/tmp/\"number 1\" /tmp/\"doublequote \\\" 2\" /tmp/\"backslash \\\\ 3\"";
    // options = options.replace("\"","\\\"");
    List<String> strings =
        ShellUtils.resolveQuotesWithoutRegex(options).stream().filter(i -> !i.equals(" ")).toList();
    Assertions.assertEquals("/tmp/number 1", strings.get(0));
    Assertions.assertEquals("/tmp/doublequote \" 2", strings.get(1));
    Assertions.assertEquals("/tmp/backslash \\ 3", strings.get(2));
  }

  @Test
  void example1() {
    String options = "/tmp/rat/\"number 1\"";
    System.out.println(ShellUtils.resolveQuotesWithoutRegex(options));
    Assertions.assertEquals(ShellUtils.resolveQuotesWithoutRegex(options).size(), 1);
  }

  @Test
  void example2() {
    String options = "/tmp/pig/\\_ignored_36 /tmp/pig/ignore_\\72 /tmp/pig/just_one_\\\\_35";
    List<String> strings =
        ShellUtils.resolveQuotesWithoutRegex(options).stream().filter(i -> !i.equals(" ")).toList();
    Assertions.assertEquals("/tmp/pig/_ignored_36", strings.get(0));
    Assertions.assertEquals("/tmp/pig/ignore_72", strings.get(1));
    Assertions.assertEquals("/tmp/pig/just_one_\\_35", strings.get(2));
  }
}
