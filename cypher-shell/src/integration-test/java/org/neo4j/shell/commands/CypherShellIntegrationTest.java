package org.neo4j.shell.commands;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.shell.Command;
import org.neo4j.shell.CypherShell;
import org.neo4j.shell.exception.CommandException;

import java.util.ArrayList;

import static junit.framework.TestCase.assertTrue;

public class CypherShellIntegrationTest {

    private CypherShell shell = new CypherShell("localhost", 7687, "neo4j", "neo");
    private Command rollbackCommand = new Rollback(shell);
    private Command commitCommand = new Commit(shell);
    private Command beginCommand = new Begin(shell);

    @Before
    public void setUp() throws Exception {
        shell.connect("localhost", 7687, "", "");
    }

    @After
    public void tearDown() throws Exception {
        shell.disconnect();
    }

    @Test
    public void rollbackScenario() throws CommandException {
        try {
            beginCommand.execute(new ArrayList<>());
            shell.executeLine("CREATE (:Random)");
            rollbackCommand.execute(new ArrayList<>());
            shell.executeLine("MATCH (n) RETURN n");
        } catch (CommandException e) {
            assertTrue("unexepcted error", e.getMessage().contains("Not connected"));
        }
    }

    @Test
    public void commitScenario() throws CommandException {
        try {
            beginCommand.execute(new ArrayList<>());
            shell.executeLine("CREATE (:Person {name: \"John Smith\"})");
            commitCommand.execute(new ArrayList<>());
        } catch (CommandException e) {
            assertTrue("unexepcted error", e.getMessage().contains("Not connected"));
        }
    }
}
