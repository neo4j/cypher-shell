package org.neo4j.shell.commands;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.shell.Command;
import org.neo4j.shell.CommandException;
import org.neo4j.shell.CypherShell;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command to connect to an instance of Neo4j.
 */
public class Connect implements Command {
    static final Pattern argPattern =
            Pattern.compile("\\s*((?<username>\\w+):(?<password>[^\\s]+)@)?(?<host>[\\d\\.\\w]+)?(:(?<port>\\d+))?\\s*");
    private static final String COMMAND_NAME = ":connect";

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
        return "Connect to a running instance of neo4j";
    }

    @Override
    public String getUsage() {
        return "[[username:password@]host][:port]";
    }

    @Nonnull
    @Override
    public String getHelp() {
        return getDescription();
    }

    @Nonnull
    @Override
    public List<String> getAliases() {
        return new ArrayList<>();
    }

    private String errorExplanation() {
        return COMMAND_NAME + " takes a single optional argument of the form: " +
                getUsage() + "\nFor example 'localhost:7687' or 'username:password@localhost:7687'";
    }

    @Override
    public void execute(@Nonnull List<String> args) throws Exit.ExitException, CommandException {
        // Default arguments
        String host = "localhost";
        int port = 7687;
        String username = "";
        String password = "";

        if (args.size() > 1) {
            throw new CommandException("Too many arguments.\n" + errorExplanation());
        } else if (args.size() == 1) {
            Matcher m = argPattern.matcher(args.get(0));
            if (!m.matches()) {
                // TODO: 6/22/16 Highlighting here
                 throw new CommandException("Could not parse " + args.get(0) + "\n" + errorExplanation());
            }

            if (null != m.group("host")) {
                host = m.group("host");
            }
            if (null != m.group("port")) {
                // Safe, regex only matches integers
                port = Integer.parseInt(m.group("port"));
            }
            if (null != m.group("username")) {
                username = m.group("username");
            }
            if (null != m.group("password")) {
                password = m.group("password");
            }
        }

        shell.connect(host, port, username, password);

        System.out.println("Connected to neo4j at " + host + ":" + port);
    }
}
