package org.neo4j.shell.cli;

import org.junit.Test;

import org.neo4j.shell.CypherShell;
import org.neo4j.shell.Historian;
import org.neo4j.shell.commands.Begin;
import org.neo4j.shell.commands.Command;
import org.neo4j.shell.commands.CommandHelper;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.log.AnsiLogger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
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
    public void oneArg() {
        try {
            assertEquals(0, simpleArgParse("bob", 0, "", ""));
            fail();
        } catch (CommandException e) {
            assertTrue(e.getMessage().contains("Incorrect number of arguments"));
        }
    }

    @Test
    public void shouldIgnoreCaseForCommands()
    {
        // Given
        AnsiLogger logger = new AnsiLogger( false );
        CommandHelper commandHelper = new CommandHelper( logger, Historian.empty, new CypherShell( logger ) );

        // When
        Command begin = commandHelper.getCommand( ":BEGIN" );

        // Then
        assertTrue( begin instanceof Begin );
    }
}
