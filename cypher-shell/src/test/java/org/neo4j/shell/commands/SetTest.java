package org.neo4j.shell.commands;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.shell.Command;
import org.neo4j.shell.Shell;
import org.neo4j.shell.exception.CommandException;

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SetTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private Shell mockShell = mock(Shell.class);
    private Command cmd;

    @Before
    public void setup() {
        this.cmd = new Set(mockShell);
    }

    @Test
    public void shouldFailIfNoArgs() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage(containsString("Incorrect number of arguments"));

        cmd.execute("");
        fail("Expected error");
    }

    @Test
    public void shouldFailIfOneArg() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage(containsString("Incorrect number of arguments"));

        cmd.execute("bob");
        fail("Expected error");
    }

    @Test
    public void setValue() throws CommandException {
        when(mockShell.isConnected()).thenReturn(true);

        cmd.execute("bob   9");

        verify(mockShell).set("bob", "9");
    }

    @Test
    public void shouldNotSplitOnSpace() throws CommandException {
        when(mockShell.isConnected()).thenReturn(true);

        cmd.execute("bob 'one two'");
        verify(mockShell).set("bob", "'one two'");
    }

    @Test
    public void shouldNotExecuteEscapedCypher() throws CommandException {
        when(mockShell.isConnected()).thenReturn(true);

        cmd.execute("bob \"RETURN 5 as bob\"");
        verify(mockShell).set("bob", "\"RETURN 5 as bob\"");
    }
}
