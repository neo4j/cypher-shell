package org.neo4j.shell.commands;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.shell.Command;
import org.neo4j.shell.CommandException;
import org.neo4j.shell.CypherShell;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Command to connect to an instance of Neo4j.
 */
public class Connect implements Command {
    public static final String COMMAND_NAME = ":connect";

    private final CypherShell shell;

    public Connect(@Nonnull final CypherShell shell) {
        this.shell = shell;
    }

    @Nonnull
    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getUsage() {
        return null;
    }

    @Nonnull
    @Override
    public String getHelp() {
        return null;
    }

    @Nonnull
    @Override
    public List<String> getAliases() {
        return new ArrayList<>();
    }

    @Override
    public void execute(@Nonnull List<String> args) throws Exit.ExitException, CommandException {
        // TODO: 6/22/16 Arguments should specify address, port, and auth

        String host = "localhost";
        int port = 7687;
        String username = "neo4j";
        String password = "neo";
        shell.connect(host, port, username, password);
        System.out.println("Connected to neo4j at " + host + ":" + port);
    }
}
