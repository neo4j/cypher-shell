package org.neo4j.shell.commands;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.neo4j.driver.exceptions.AuthenticationException;
import org.neo4j.shell.CypherShell;
import org.neo4j.shell.ShellParameterMap;
import org.neo4j.shell.StringLinePrinter;
import org.neo4j.shell.cli.Format;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.prettyprint.PrettyConfig;

public class CypherShellFailureIntegrationTest extends CypherShellIntegrationTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private StringLinePrinter linePrinter = new StringLinePrinter();

    @Before
    public void setUp() {
        linePrinter.clear();
        shell = new CypherShell(linePrinter, new PrettyConfig(Format.VERBOSE, true, 1000), false, new ShellParameterMap());
    }

    @Test
    public void cypherWithNoPasswordShouldReturnValidError() throws CommandException {
        thrown.expect(AuthenticationException.class);
        thrown.expectMessage("The client is unauthorized due to authentication failure.");

        connect("");
    }
}
