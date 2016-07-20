package org.neo4j.shell.commands;

import org.neo4j.shell.Command;
import org.neo4j.shell.ConnectionConfig;
import org.neo4j.shell.Connector;
import org.neo4j.shell.cli.CliArgHelper;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.ExitException;
import org.neo4j.shell.log.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import static org.neo4j.shell.CommandHelper.simpleArgParse;

/**
 * Command to connect to an instance of Neo4j.
 */
public class Connect implements Command {
    private static final String COMMAND_NAME = ":connect";

    private final Logger logger;
    private final Connector connector;

    public Connect(@Nonnull final Logger logger, @Nonnull final Connector connector) {
        this.logger = logger;
        this.connector = connector;
    }

    @Nonnull
    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Nonnull
    @Override
    public String getDescription() {
        return "Connect to a running instance of neo4j";
    }

    @Nonnull
    @Override
    public String getUsage() {
        return "[username:password@][host][:port]";
    }

    @Nonnull
    @Override
    public String getHelp() {
        return "Connect to a running instance of neo4j. Must be in disconnected state.";
    }

    @Nonnull
    @Override
    public List<String> getAliases() {
        return new ArrayList<>();
    }

    @Override
    public void execute(@Nonnull final String argString) throws ExitException, CommandException {
        // Default arguments
        String host = "localhost";
        int port = 7687;
        String username = "";
        String password = "";

        String[] args = simpleArgParse(argString, 0, 1, COMMAND_NAME, getUsage());

        if (args.length == 1) {
            Matcher m = CliArgHelper.addressArgPattern.matcher(args[0]);
            if (!m.matches()) {
                throw new CommandException(String.format("Could not parse @|bold %s|@\nusage: @|bold %s|@ %s",
                        args[0], COMMAND_NAME, getUsage()));
            }

            if (null != m.group("protocol") && !"bolt://".equalsIgnoreCase(m.group("protocol"))) {
                throw new CommandException(String.format("Unsupported protocol: '%s'\nOnly 'bolt://' is supported.",
                        m.group("protocol")));
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

        connector.connect(new ConnectionConfig(host, port, username, password));
        logger.printOut("Connected to neo4j at @|bold " + host + ":" + port + "|@");
    }
}
