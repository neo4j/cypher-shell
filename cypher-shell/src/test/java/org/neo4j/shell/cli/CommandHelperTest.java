package org.neo4j.shell.cli;

import org.junit.Test;
import org.neo4j.shell.exception.CommandException;

import static org.junit.Assert.*;
import static org.neo4j.shell.commands.CommandHelper.simpleArgParse;

public class CommandHelperTest {

    @Test
    public void emptyStringIsNoArgs() throws CommandException {
        assertEquals(0, simpleArgParse("", 0, "", "").length);
    }

    @Test
    public void whitespaceStringIsNoArgs() throws CommandException {
        assertEquals(0, simpleArgParse("    \t  ", 0, "", "").length);
    }

    @Test
    public void oneArg() throws CommandException {
        try {
            assertEquals(0, simpleArgParse("bob", 0, "", ""));
            fail();
        } catch (CommandException e) {
            assertTrue(e.getMessage().contains("Incorrect number of arguments"));
        }
    }
}
