package org.neo4j.shell.commands;


import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.shell.Command;
import org.neo4j.shell.TestShell;
import org.neo4j.shell.exception.CommandException;

import java.util.ArrayList;
import java.util.Arrays;

import static junit.framework.TestCase.*;

@Ignore
public class BeginIntegrationTest {

    private TestShell shell;
    private Command cmd;

    @Before
    public void setup() {
        this.shell = new TestShell();
        this.cmd = new Begin(shell);
    }

    @Test
    public void openTransaction() throws CommandException {
        connnectShell();
        assertFalse("Did not expect an open transaction here", shell.getCurrentTransaction().isPresent());

        cmd.execute(new ArrayList<>());

        assertTrue("Expected an open transaction", shell.getCurrentTransaction().isPresent());
    }

    @Test
    public void nestedTransactionsAreNotSupported() throws CommandException {
        connnectShell();
        assertFalse("Did not expect an open transaction here", shell.getCurrentTransaction().isPresent());
        cmd.execute(new ArrayList<>());
        assertTrue("Expected an open transaction", shell.getCurrentTransaction().isPresent());

        try {
            cmd.execute(new ArrayList<>());
            fail("Should throw");
        } catch (CommandException e) {
            assertTrue("unexpected error", e.getMessage().contains("already an open transaction"));
        }
    }

    private void connnectShell() throws CommandException {
        shell.connect("bla", 99, "bob", "pass");
    }
}
