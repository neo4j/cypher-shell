package org.neo4j.shell.commands;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.shell.StreamShell;
import org.neo4j.shell.exception.CommandException;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class EnvTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private StreamShell shell;
    private Env cmd;

    @Before
    public void setup() throws CommandException {
        shell = new StreamShell();
        shell.connect();
        cmd = new Env(shell);
    }

    @Test
    public void runCommand() throws CommandException {
        // given
        Set set = new Set(shell);
        set.execute("var 9");
        // when
        cmd.execute("");
        // then
        assertEquals("var: 9\n", shell.getOutLog());
    }

    @Test
    public void runCommandAlignment() throws CommandException {
        // given
        Set set = new Set(shell);
        set.execute("var 9");
        set.execute("param 99999");
        // when
        cmd.execute("");
        // then
        assertEquals("param: 99999\nvar  : 9\n", shell.getOutLog());
    }

    @Test
    public void shouldNotAcceptArgs() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage(containsString("Incorrect number of arguments"));

        cmd.execute("bob");
        fail("Should not accept args");
    }
}
