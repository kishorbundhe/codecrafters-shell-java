package commands;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ShellUtilsTest {

  @Test
  void resolvesConcatenatedQuotedAndUnquoted() {
    String input = "\"example\\\"insidequotes\"script\\\"";
    Assertions.assertEquals("example\"insidequotesscript\"", ShellUtils.resolveQuotes(input));
  }

  @Test
  void resolvesSimpleDoubleQuotes() {
    Assertions.assertEquals("hello world", ShellUtils.resolveQuotes("\"hello world\""));
  }

  @Test
  void concatenatesAdjacentQuotedStrings() {
    Assertions.assertEquals("foobar", ShellUtils.resolveQuotes("\"foo\"\"bar\""));
  }

  @Test
  void concatenatesQuotedAndUnquotedText() {
    Assertions.assertEquals("foobar", ShellUtils.resolveQuotes("\"foo\"bar"));
  }

  @Test
  void concatenatesUnquotedAndQuotedText() {
    Assertions.assertEquals("foobar", ShellUtils.resolveQuotes("foo\"bar\""));
  }

  @Test
  void handlesEscapedQuoteInsideDoubleQuotes() {
    Assertions.assertEquals("a\"b", ShellUtils.resolveQuotes("\"a\\\"b\""));
  }

  @Test
  void handlesEscapedBackslashInsideDoubleQuotes() {
    Assertions.assertEquals("a\\b", ShellUtils.resolveQuotes("\"a\\\\b\""));
  }

  @Test
  void handlesEscapedCharactersOutsideQuotes() {
    Assertions.assertEquals("hello world", ShellUtils.resolveQuotes("hello\\ world"));
  }

  @Test
  void handlesMultipleArguments() {
    Assertions.assertEquals("echo hello world", ShellUtils.resolveQuotes("echo hello world"));
  }

  @Test
  void handlesQuotedArgumentWithSpaces() {
    Assertions.assertEquals("echo hello world", ShellUtils.resolveQuotes("echo \"hello world\""));
  }

  @Test
  void handlesMixedQuotes() {
    Assertions.assertEquals(
        "echo hello world again", ShellUtils.resolveQuotes("echo 'hello world' again"));
  }

  @Test
  void handlesEmptyDoubleQuotes() {
    Assertions.assertEquals("", ShellUtils.resolveQuotes("\"\""));
  }

  @Test
  void handlesEmptySingleQuotes() {
    Assertions.assertEquals("", ShellUtils.resolveQuotes("''"));
  }

  @Test
  void handlesEmptyInput() {
    Assertions.assertEquals("", ShellUtils.resolveQuotes(""));
  }

  @Test
  void adjacentSingleAndDoubleQuotes() {
    Assertions.assertEquals("foobar", ShellUtils.resolveQuotes("'foo'\"bar\""));
  }

  @Test
  void quotedArgumentFollowedImmediatelyByText() {
    Assertions.assertEquals("abc123def", ShellUtils.resolveQuotes("\"abc\"123\"def\""));
  }

  @Test
  void escapedSpaceAfterQuotedText() {
    Assertions.assertEquals("abc def", ShellUtils.resolveQuotes("\"abc\"\\ def"));
  }

  @Test
  void escapedBackslashOutsideQuotes() {
    Assertions.assertEquals("a\\b", ShellUtils.resolveQuotes("a\\\\b"));
  }

  //  @Test
  //  void handleFilePath() {
  //    Assertions.assertEquals("foobar",
  //            ShellUtils.resolveQuotes("/tmp/rat/backslash \\ 43"));
  //  }

  @Test
  void resolvesSimpleSingleQuotes() {
    Assertions.assertEquals("hello world", ShellUtils.resolveQuotes("'hello world'"));
  }

  @Test
  void concatenatesSingleQuotedAndUnquotedText() {
    Assertions.assertEquals("foobar", ShellUtils.resolveQuotes("'foo'bar"));
  }

  @Test
  void concatenatesUnquotedAndSingleQuotedText() {
    Assertions.assertEquals("foobar", ShellUtils.resolveQuotes("foo'bar'"));
  }

  @Test
  void concatenatesAdjacentSingleQuotedStrings() {
    Assertions.assertEquals("foobar", ShellUtils.resolveQuotes("'foo''bar'"));
  }

  @Test
  void example1() {
    Assertions.assertEquals(
        "inside\"literal_quote.outside\"",
        ShellUtils.resolveQuotes("\"inside\\\"literal_quote.\"outside\\\""));
  }

  @Test
  void example2() {
    Assertions.assertEquals(
        "just'one'\\n'backslash", ShellUtils.resolveQuotes("\"just'one'\\\\n'backslash\""));
  }

  @Test
  void example3() {
    Assertions.assertEquals("testnshell", ShellUtils.resolveQuotes("test\\nshell"));
  }

  @Test
  void example4() {
    Assertions.assertEquals("ignore_backslash", ShellUtils.resolveQuotes("ignore\\_backslash"));
    Assertions.assertEquals("shell\\\\\\nscript", ShellUtils.resolveQuotes("'shell\\\\\\nscript'"));
    Assertions.assertEquals("example\\\"test", ShellUtils.resolveQuotes("'example\\\"test'"));
    Assertions.assertEquals(
        "just'one'\\n'backslash", ShellUtils.resolveQuotes("\"just'one'\\\\n'backslash\""));
    Assertions.assertEquals(
        "multiple\\\\slashes", ShellUtils.resolveQuotes("'multiple\\\\slashes'"));
    Assertions.assertEquals(
        "every\\\"thing_is\\\"literal",
            ShellUtils.resolveQuotes("'every\\\"thing_is\\\"literal'"));
  }

  @Test
  void concatenatesSingleAndDoubleQuotedStrings() {
    Assertions.assertEquals("foobar", ShellUtils.resolveQuotes("'foo'\"bar\""));
  }

  @Test
  void preservesDoubleQuotesInsideSingleQuotes() {
    Assertions.assertEquals(
        "just'one'\\n'backslash", ShellUtils.resolveQuotes("\"just'one'\\\\n'backslash\""));
  }

  @Test
  void preservesBackslashesInsideSingleQuotes() {
    Assertions.assertEquals("a\\b", ShellUtils.resolveQuotes("'a\\b'"));
  }

  @Test
  void preservesEscapedQuoteInsideSingleQuotes() {
    // POSIX shells treat '\' literally inside single quotes.
    Assertions.assertEquals(
        "multiple\\\\slashes", ShellUtils.resolveQuotes("'multiple\\\\slashes'"));
  }

  @Test
  void handlesSingleQuotedArgumentWithSpaces() {
    Assertions.assertEquals("echo hello world", ShellUtils.resolveQuotes("echo 'hello world'"));
  }

  @Test
  void handleSingleQuotes() {
    Assertions.assertEquals("shell\\\\\\nscript", ShellUtils.resolveQuotes("'shell\\\\\\nscript'"));
    Assertions.assertEquals(
        "multiple\\\\slashes", ShellUtils.resolveQuotes("'multiple\\\\slashes'"));
    Assertions.assertEquals(
        "every\\\"thing_is\\\"literal", ShellUtils.resolveQuotes("'every\\\"thing_is\\\"literal'"));
  }

  @Test
  void handlefileNames() {

    String str = "/tmp/\"doublequote \\\" 2\" ";
    String escaped = str.translateEscapes();
    str = str.replace("\"", "\\\"");
    System.out.println(escaped);
    System.out.println(str);
  }
}
