package commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidCommandTest {

    @Test
    void returnsTrueWhenInputContainsEcho() {
        assertTrue(ValidCommand.containsShellBuiltIn("echo hello"));
    }

    @Test
    void returnsTrueWhenInputContainsPwd() {
        assertTrue(ValidCommand.containsShellBuiltIn("pwd"));
    }

    @Test
    void returnsTrueWhenPipelineContainsBuiltin() {
        assertTrue(ValidCommand.containsShellBuiltIn("ls | echo hello"));
    }

    @Test
    void returnsFalseWhenNoBuiltinExists() {
        assertFalse(ValidCommand.containsShellBuiltIn("ls | grep txt"));
    }


    @Test
    void returnsFalseForEmptyInput() {
        assertFalse(ValidCommand.containsShellBuiltIn(""));
    }

    @Test
    void returnsFalseForWhitespaceInput() {
        assertFalse(ValidCommand.containsShellBuiltIn("   "));
    }
}
