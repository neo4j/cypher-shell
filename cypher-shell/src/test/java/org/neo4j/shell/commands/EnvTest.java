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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class EnvTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private HashMap<String, Object> vars;
    private Logger logger;
    private Env cmd;

    @Before
    public void setup() throws CommandException {
        vars = new HashMap<>();
        logger = mock(Logger.class);
        VariableHolder shell = mock(VariableHolder.class);
        when(shell.getAll()).thenReturn(vars);
        cmd = new Env(logger, shell);
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
    public void shouldNotAcceptArgs() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage(containsString("Incorrect number of arguments"));

        cmd.execute("bob");
        fail("Should not accept args");
    }
}
