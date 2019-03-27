package org.neo4j.shell.commands;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.shell.ParameterMap;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.log.Logger;
import org.neo4j.shell.state.ParamValue;

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

    private HashMap<String, ParamValue> vars;
    private Logger logger;
    private Params cmd;

    @Before
    public void setup() throws CommandException {
        vars = new HashMap<>();
        logger = mock(Logger.class);
        ParameterMap shell = mock( ParameterMap.class);
        when(shell.getAllAsUserInput()).thenReturn(vars);
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
        String var = "var";
        int value = 9;
        vars.put(var, new ParamValue(String.valueOf(value), value));
        // when
        cmd.execute("");
        // then
        verify(logger).printOut(":param var => 9");
        verifyNoMoreInteractions(logger);
    }

    @Test
    public void runCommandAlignment() throws CommandException {
        // given
        vars.put("var", new ParamValue(String.valueOf(9), 9));
        vars.put("param", new ParamValue(String.valueOf(99999), 99999));
        // when
        cmd.execute("");
        // then
        verify(logger).printOut(":param param => 99999");
        verify(logger).printOut(":param var   => 9");
        verifyNoMoreInteractions(logger);
    }

    @Test
    public void runCommandWithArg() throws CommandException {
        // given
        vars.put("var", new ParamValue(String.valueOf(9), 9));
        vars.put("param", new ParamValue(String.valueOf(9999), 9999));
        // when
        cmd.execute("var");
        // then
        verify(logger).printOut(":param var => 9");
        verifyNoMoreInteractions(logger);
    }

    @Test
    public void runCommandWithArgWithExtraSpace() throws CommandException {
        // given
        vars.put("var", new ParamValue(String.valueOf(9), 9));
        vars.put("param", new ParamValue(String.valueOf(9999), 9999));
        // when
        cmd.execute(" var");
        // then
        verify(logger).printOut(":param var => 9");
        verifyNoMoreInteractions(logger);
    }

    @Test
    public void runCommandWithArgWithBackticks() throws CommandException {
        // given
        vars.put("var", new ParamValue(String.valueOf(9), 9));
        vars.put("param", new ParamValue(String.valueOf(9999), 9999));
        // when
        cmd.execute("`var`");
        // then
        verify(logger).printOut(":param `var` => 9");
        verifyNoMoreInteractions(logger);
    }

    @Test
    public void runCommandWithSpecialCharacters() throws CommandException {
        // given
        vars.put("var `", new ParamValue(String.valueOf(9), 9));
        vars.put("param", new ParamValue(String.valueOf(9999), 9999));
        // when
        cmd.execute("`var ```");
        // then
        verify(logger).printOut(":param `var ``` => 9");
        verifyNoMoreInteractions(logger);
    }

    @Test
    public void runCommandWithUnknownArg() throws CommandException {
        // then
        thrown.expect(CommandException.class);
        thrown.expectMessage(containsString("Unknown parameter: bob"));
        // given
        vars.put("var", new ParamValue(String.valueOf(9), 9));
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
