package org.neo4j.shell.commands;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.shell.ConnectionConfig;
import org.neo4j.shell.CypherShell;
import org.neo4j.shell.cli.Format;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.log.Logger;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class CypherShellFailureIntegrationTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private Logger logger = mock(Logger.class);
    private CypherShell shell;

    @Before
    public void setUp() throws Exception {
        doReturn(Format.VERBOSE).when(logger).getFormat();

        shell = new CypherShell(logger);
    }

    @Test
    public void cypherWithNoPasswordShouldReturnValidError() throws CommandException {
        thrown.expectMessage("The client is unauthorized due to authentication failure.");

        shell.connect(new ConnectionConfig(logger, "bolt://", "localhost", 7687, "neo4j", "", true));
    }
}
