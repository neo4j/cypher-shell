package org.neo4j.shell.commands;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.neo4j.driver.exceptions.AuthenticationException;
import org.neo4j.shell.ConnectionConfig;
import org.neo4j.shell.CypherShell;
import org.neo4j.shell.StringLinePrinter;
import org.neo4j.shell.cli.Format;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.prettyprint.PrettyConfig;

import static org.neo4j.driver.internal.messaging.request.MultiDatabaseUtil.ABSENT_DB_NAME;

public class CypherShellFailureIntegrationTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private StringLinePrinter linePrinter = new StringLinePrinter();
    private CypherShell shell;

    @Before
    public void setUp() throws Exception {
        linePrinter.clear();
        shell = new CypherShell(linePrinter, new PrettyConfig(Format.VERBOSE, true, 1000));
    }

    @Test
    public void cypherWithNoPasswordShouldReturnValidError() throws CommandException {
        thrown.expect(AuthenticationException.class);
        thrown.expectMessage("The client is unauthorized due to authentication failure.");

        shell.connect(new ConnectionConfig("bolt://", "localhost", 7687, "neo4j", "", true, ABSENT_DB_NAME));
    }

    @Test
    public void switchingDatabaseInOpenTransactionShouldFail() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage("There is an open transaction.");

        shell.connect(new ConnectionConfig("bolt://", "localhost", 7687, "neo4j", "", true, ABSENT_DB_NAME));

        shell.beginTransaction();
        shell.setActiveDatabase("another_database");
    }
}
