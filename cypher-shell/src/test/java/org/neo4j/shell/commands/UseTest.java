package org.neo4j.shell.commands;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.neo4j.shell.DatabaseManager;
import org.neo4j.shell.exception.CommandException;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class UseTest
{
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private DatabaseManager mockShell = mock(DatabaseManager.class);
    private Command cmd;

    @Before
    public void setup() {
        this.cmd = new Use(mockShell);
    }

    @Test
    public void setAbsentDatabaseOnNoArgument() throws CommandException {
        cmd.execute("");

        verify(mockShell).setActiveDatabase(DatabaseManager.ABSENT_DB_NAME);
    }

    @Test
    public void shouldFailIfMoreThanOneArg() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage(containsString("Incorrect number of arguments"));

        cmd.execute("db1 db2");
        fail("Expected error");
    }

    @Test
    public void setActiveDatabase() throws CommandException {
        cmd.execute("db1");

        verify(mockShell).setActiveDatabase("db1");
    }

    @Test
    public void printUsage() throws CommandException {
        String usage = cmd.getUsage();
        assertEquals(usage, "database");
    }
}
