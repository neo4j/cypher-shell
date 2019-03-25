package org.neo4j.shell.commands;


import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.neo4j.shell.ConnectionConfig;
import org.neo4j.shell.CypherShell;
import org.neo4j.shell.cli.Format;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.log.Logger;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CypherShellVerboseIntegrationTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private Logger logger = mock(Logger.class);
    private Command rollbackCommand;
    private Command commitCommand;
    private Command beginCommand;
    private CypherShell shell;

    @Before
    public void setUp() throws Exception {
        doReturn(Format.VERBOSE).when(logger).getFormat();

        shell = new CypherShell(logger);
        rollbackCommand = new Rollback(shell);
        commitCommand = new Commit(shell);
        beginCommand = new Begin(shell);

        shell.connect(new ConnectionConfig("bolt://", "localhost", 7687, "neo4j", "neo", true));
    }

    @After
    public void tearDown() throws Exception {
        shell.execute("MATCH (n) DETACH DELETE (n)");
    }

    @Test
    public void cypherWithNoReturnStatements() throws CommandException {
        //when
        shell.execute("CREATE (:TestPerson {name: \"Jane Smith\"})");

        //then
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(logger, times(1)).printOut(captor.capture());

        List<String> result = captor.getAllValues();
        assertThat(result.get(0), containsString("Added 1 nodes, Set 1 properties, Added 1 labels"));
    }

    @Test
    public void cypherWithReturnStatements() throws CommandException {
        //when
        shell.execute("CREATE (jane :TestPerson {name: \"Jane Smith\"}) RETURN jane");

        //then
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(logger, times(1)).printOut(captor.capture());

        List<String> result = captor.getAllValues();
        assertThat(result.get(0), containsString("| jane "));
        assertThat(result.get(0), containsString("| (:TestPerson {name: \"Jane Smith\"}) |" ));
        assertThat(result.get(0), containsString("Added 1 nodes, Set 1 properties, Added 1 labels"));
    }

    @Test
    public void connectTwiceThrows() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage("Already connected");

        ConnectionConfig config = new ConnectionConfig("bolt://", "localhost", 7687, "neo4j", "neo", true);
        assertTrue("Shell should already be connected", shell.isConnected());
        shell.connect(config);
    }

    @Test
    public void rollbackScenario() throws CommandException {
        //given
        shell.execute("CREATE (:TestPerson {name: \"Jane Smith\"})");

        //when
        beginCommand.execute("");
        shell.execute("CREATE (:Random)");
        rollbackCommand.execute("");

        //then
        shell.execute("MATCH (n:TestPerson) RETURN n ORDER BY n.name");

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(logger, times(2)).printOut(captor.capture());

        List<String> result = captor.getAllValues();
        assertThat(result.get(1), containsString("| n "));
        assertThat(result.get(1), containsString("| (:TestPerson {name: \"Jane Smith\"}) |"));
    }

    @Test
    public void resetOutOfTxScenario() throws CommandException {
        //when
        shell.execute("CREATE (:TestPerson {name: \"Jane Smith\"})");
        shell.reset();

        //then
        shell.execute("CREATE (:TestPerson {name: \"Jane Smith\"})");
        shell.execute("MATCH (n:TestPerson) RETURN n ORDER BY n.name");

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(logger, times(3)).printOut(captor.capture());

        List<String> result = captor.getAllValues();
        assertThat(result.get(2), containsString("| (:TestPerson {name: \"Jane Smith\"}) |" +
                "\n| (:TestPerson {name: \"Jane Smith\"}) |"));
    }

    @Test
    public void resetInTxScenario() throws CommandException {
        //when
        beginCommand.execute("");
        shell.execute("CREATE (:Random)");
        shell.reset();

        //then
        shell.execute("CREATE (:TestPerson {name: \"Jane Smith\"})");
        shell.execute("MATCH (n:TestPerson) RETURN n ORDER BY n.name");

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(logger, times(2)).printOut(captor.capture());

        List<String> result = captor.getAllValues();
        assertThat(result.get(1), containsString("| (:TestPerson {name: \"Jane Smith\"}) |"));
    }

    @Test
    public void commitScenario() throws CommandException {
        beginCommand.execute("");
        shell.execute("CREATE (:TestPerson {name: \"Joe Smith\"})");
        shell.execute("CREATE (:TestPerson {name: \"Jane Smith\"})");
        shell.execute("MATCH (n:TestPerson) RETURN n ORDER BY n.name");
        commitCommand.execute("");

        //then

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(logger, times(3)).printOut(captor.capture());

        List<String> result = captor.getAllValues();
        assertThat(result.get(2),
                containsString("\n| (:TestPerson {name: \"Jane Smith\"}) |\n| (:TestPerson {name: \"Joe Smith\"})  |\n"));
    }

    @Test
    public void paramsAndListVariables() throws CommandException {
        assertTrue(shell.allParameterValues().isEmpty());

        long randomLong = System.currentTimeMillis();
        String stringInput = "\"randomString\"";
        shell.setParameter("string", stringInput);
        Optional result = shell.setParameter("bob", String.valueOf(randomLong));
        assertTrue(result.isPresent());
        assertEquals(randomLong, result.get());

        shell.execute("RETURN { bob }, $string");

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(logger).printOut(captor.capture());

        List<String> queryResult = captor.getAllValues();
        assertThat(queryResult.get(0), containsString("| { bob }"));
        assertThat(queryResult.get(0), containsString("| " + randomLong + " | " + stringInput + " |"));
        assertEquals(randomLong, shell.allParameterValues().get("bob"));
        assertEquals("randomString", shell.allParameterValues().get("string"));
    }

    @Test
    public void paramsAndListVariablesWithSpecialCharacters() throws CommandException {
        assertTrue(shell.allParameterValues().isEmpty());

        long randomLong = System.currentTimeMillis();
        Optional result = shell.setParameter("`bob`", String.valueOf(randomLong));
        assertTrue(result.isPresent());
        assertEquals(randomLong, result.get());

        shell.execute("RETURN { `bob` }");

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(logger).printOut(captor.capture());

        List<String> queryResult = captor.getAllValues();
        assertThat(queryResult.get(0), containsString("| { `bob` }"));
        assertThat(queryResult.get(0), containsString("\n| " + randomLong+ " |\n"));
        assertEquals(randomLong, shell.allParameterValues().get("bob"));
    }
}
