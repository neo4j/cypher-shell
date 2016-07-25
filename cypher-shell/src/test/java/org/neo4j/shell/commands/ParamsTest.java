package org.neo4j.shell.commands;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.shell.VariableHolder;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.log.Logger;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ParamsTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private HashMap<String, Object> vars;
    private Logger logger;
    private Params cmd;

    @Before
    public void setup() throws CommandException {
        vars = new HashMap<>();
        logger = mock(Logger.class);
        VariableHolder shell = mock(VariableHolder.class);
        when(shell.getAll()).thenReturn(vars);
        cmd = new Params(logger, shell);
    }

    @Test
    public void descriptionNotNull() {
        assertNotNull(cmd.getDescription());
    }

    @Test
    public void usageNotNull() {
        assertNotNull(cmd.getUsage());
    }

    @Test
    public void helpNotNull() {
        assertNotNull(cmd.getHelp());
    }

    @Test
    public void runCommand() throws CommandException {
        // given
        vars.put("var", 9);
        // when
        cmd.execute("");
        // then
        verify(logger).printOut("var: 9");
        verifyNoMoreInteractions(logger);
    }

    @Test
    public void runCommandAlignment() throws CommandException {
        // given
        vars.put("var", 9);
        vars.put("param", 99999);
        // when
        cmd.execute("");
        // then
        verify(logger).printOut("param: 99999");
        verify(logger).printOut("var  : 9");
        verifyNoMoreInteractions(logger);
    }

    @Test
    public void runCommandWithArg() throws CommandException {
        // given
        vars.put("var", 9);
        vars.put("param", 9999);
        // when
        cmd.execute("var");
        // then
        verify(logger).printOut("var: 9");
        verifyNoMoreInteractions(logger);
    }

    @Test
    public void runCommandWithUnknownArg() throws CommandException {
        // then
        thrown.expect(CommandException.class);
        thrown.expectMessage(containsString("Unknown parameter: bob"));
        // given
        vars.put("var", 9);
        // when
        cmd.execute("bob");
    }

    @Test
    public void shouldNotAcceptMoreThanOneArgs() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage(containsString("Incorrect number of arguments"));

        cmd.execute("bob sob");
    }
}
