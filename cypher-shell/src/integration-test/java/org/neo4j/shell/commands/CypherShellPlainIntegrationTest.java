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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CypherShellPlainIntegrationTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private Logger logger = mock(Logger.class);
    private CypherShell shell;

    @Before
    public void setUp() throws Exception {
        doReturn(Format.PLAIN).when(logger).getFormat();
        shell = new CypherShell(logger);
        shell.connect(new ConnectionConfig("bolt://", "localhost", 7687, "neo4j", "neo", true));
    }

    @After
    public void tearDown() throws Exception {
        shell.execute("MATCH (n) DETACH DELETE (n)");
    }

    @Test
    public void periodicCommitWorks() throws CommandException {
        shell.execute("USING PERIODIC COMMIT\n" +
                "LOAD CSV FROM 'https://neo4j.com/docs/cypher-refcard/3.2/csv/artists.csv' AS line\n" +
                "CREATE (:Artist {name: line[1], year: toInt(line[2])});");

        shell.execute("MATCH (a:Artist) WHERE a.name = 'Europe' RETURN a.name");

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(logger, times(2)).printOut(captor.capture());

        List<String> queryResult = captor.getAllValues();
        assertThat(queryResult.get(1), containsString("Europe"));
    }

    @Test
    public void cypherWithProfileStatements() throws CommandException {
        //when
        shell.execute("CYPHER RUNTIME=INTERPRETED PROFILE RETURN null");

        //then
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(logger, times(1)).printOut(captor.capture());

        List<String> result = captor.getAllValues();
        String actual = result.get(0);
        //      This assertion checks everything except for time and cypher
        assertThat(actual, containsString("Plan: \"PROFILE\""));
        assertThat(actual, containsString("Statement: \"READ_ONLY\""));
        assertThat(actual, containsString("Planner: \"COST\""));
        assertThat(actual, containsString("Runtime: \"INTERPRETED\""));
        assertThat(actual, containsString("DbHits: 0"));
        assertThat(actual, containsString("Rows: 1"));
        assertThat(actual, containsString("null"));
        assertThat(actual, containsString("NULL"));
    }

    @Test
    public void cypherWithExplainStatements() throws CommandException {
        //when
        shell.execute("CYPHER RUNTIME=INTERPRETED EXPLAIN RETURN null");

        //then
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(logger, times(1)).printOut(captor.capture());

        List<String> result = captor.getAllValues();
        String actual = result.get(0);
        //      This assertion checks everything except for time and cypher
        assertThat(actual, containsString("Plan: \"EXPLAIN\""));
        assertThat(actual, containsString("Statement: \"READ_ONLY\""));
        assertThat(actual, containsString("Planner: \"COST\""));
        assertThat(actual, containsString("Runtime: \"INTERPRETED\""));
    }
}
