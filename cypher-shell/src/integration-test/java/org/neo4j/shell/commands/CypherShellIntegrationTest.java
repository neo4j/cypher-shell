package org.neo4j.shell.commands;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.neo4j.shell.ConnectionConfig;
import org.neo4j.shell.CypherShell;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.log.Logger;

import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

public class CypherShellIntegrationTest {

    private Logger logger = mock(Logger.class);
    private CypherShell shell = new CypherShell(logger);
    private Command rollbackCommand = new Rollback(shell);
    private Command commitCommand = new Commit(shell);
    private Command beginCommand = new Begin(shell);

    @Before
    public void setUp() throws Exception {
        shell.connect(new ConnectionConfig("localhost", 7687, "neo4j", "neo"));
    }

    @After
    public void tearDown() throws Exception {
        shell.execute("MATCH (n:TestPerson) DETACH DELETE (n)");
        shell.disconnect();
    }

    @Test
    public void rollbackScenario() throws CommandException {
        try {
            //given
            shell.execute("CREATE (:TestPerson {name: \"Jane Smith\"})");

            //when
            beginCommand.execute("");
            shell.execute("CREATE (:Random)");
            rollbackCommand.execute("");

            //then
            shell.execute("MATCH (n:TestPerson) RETURN n");

            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(logger, times(3)).printOut(captor.capture());

            List<String> result = captor.getAllValues();
            assertThat(result.get(2), is("n\n{name: \"Jane Smith\"}"));

        } catch (CommandException e) {
            assertTrue("unexepcted error", e.getMessage().contains("Not connected"));
        }
    }

    @Test
    public void commitScenario() throws CommandException {
        try {
            beginCommand.execute("");
            shell.execute("CREATE (:TestPerson {name: \"Joe Smith\"})");
            commitCommand.execute("");

            //then
            shell.execute("MATCH (n:TestPerson) RETURN n");

            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(logger, times(2)).printOut(captor.capture());

            List<String> result = captor.getAllValues();
            assertThat(result.get(1), is("n\n{name: \"Joe Smith\"}"));
        } catch (CommandException e) {
            assertTrue("unexepcted error", e.getMessage().contains("Not connected"));
        }
    }
}
