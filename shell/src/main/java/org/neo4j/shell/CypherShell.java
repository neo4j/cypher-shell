package org.neo4j.shell;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiRenderer;
import org.neo4j.driver.v1.*;
import org.neo4j.shell.commands.Exit;
import org.neo4j.shell.prettyprint.PrettyPrinter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * An interactive shell for evaluating cypher statements.
 */
public class CypherShell {

    private final CommandHelper commandHelper;
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private Driver driver;
    private Session session;

    CypherShell(@Nonnull String host, int port, @Nonnull String username, @Nonnull String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;

        commandHelper = new CommandHelper(this);
    }

    int run() {
        int exitCode;

        try {
            connect(host, port, username, password);

            InteractiveShellRunner runner = new InteractiveShellRunner(this, this::renderPrompt);
            runner.run();

            exitCode = 0;
        } catch (Exit.ExitException e) {
            exitCode = e.getCode();
        } catch (Throwable t) {
            System.err.println(t.getMessage());
            exitCode = 1;
        }

        return exitCode;
    }

    @Nonnull
    private String renderPrompt() {
        return AnsiRenderer.render(buildPrompt());
    }

    @Nonnull
    private String buildPrompt() {
        // TODO: 6/21/16 Line number

        return "@|bold cypher:|@1@|bold >|@ ";
    }

    void execute(@Nonnull final String line) throws Exit.ExitException {
        // TODO: 6/21/16 handle command

        // See if it's a shell command
        CommandExecutable cmd = getCommandExecutable(line);
        if (cmd != null) {
            executeCmd(cmd);
            return;
        }

        // Else it will be parsed as Cypher, but for that we need to be connected
        if (!isConnected()) {
            System.err.println(ansi().a(Ansi.Attribute.INTENSITY_BOLD).fgRed().a("Not connected to Neo4j").reset());
            return;
        }

        executeCypher(line);
    }

    private void executeCypher(@Nonnull final String line) {
        // TODO: 6/22/16 Lots...
        // TODO: 6/22/16 Expose transaction handling
        StatementResult result = session.run(line);

        PrettyPrinter.print(result);
    }

    private boolean isConnected() {
        return driver != null;
    }

    @Nullable
    private CommandExecutable getCommandExecutable(@Nonnull final String line) {
        String[] parts = line.trim().split("\\s");

        if (parts.length < 1) {
            return null;
        }

        String name = parts[0];

        Command cmd = commandHelper.getCommand(name);

        if (cmd != null) {
            List<String> args = new ArrayList<>();
            for (int i = 1; i < parts.length; i++) {
                args.add(parts[i]);
            }
            return () -> cmd.execute(args);
        }

        return null;
    }

    private void executeCmd(@Nonnull final CommandExecutable cmdExe) throws Exit.ExitException {
        try {
            cmdExe.execute();
        } catch (CommandException e) {
            System.err.println(ansi().a(Ansi.Attribute.INTENSITY_BOLD).fgRed().a(e.getMessage()).reset());
        }
    }

    /**
     * Open a session to Neo4j
     */
    public void connect(@Nonnull final String host, final int port, @Nonnull final String username,
                        @Nonnull final String password) throws CommandException {
        if (isConnected()) {
            // TODO: 6/22/16 Highlight disconnect here
            throw new CommandException("Already connected. Call :disconnect first.");
        }

        final AuthToken authToken;
        if (username.isEmpty() && password.isEmpty()) {
            authToken = null;
        } else if (!username.isEmpty() && !password.isEmpty()) {
            authToken = AuthTokens.basic(username, password);
        } else if (username.isEmpty()) {
            throw new CommandException("Specified password but no username");
        } else {
            throw new CommandException("Specified username but no password");
        }

        try {
            driver = GraphDatabase.driver(String.format("bolt://%s:%d", host, port),
                    authToken);
            session = driver.session();
            // Bug in Java driver forces us to run a statement to make it actually connect
            session.run("RETURN 1").consume();
        } catch (Throwable t) {
            try {
                silentDisconnect();
            } catch (Throwable ignore) {}
            throw t;
        }
    }

    /**
     * Disconnect from Neo4j, clearing up any session resources, but don't give any output.
     */
    private void silentDisconnect() {
        try {
            if (session != null) {
                session.close();
            }
            if (driver != null) {
                driver.close();
            }
        } finally {
            session = null;
            driver = null;
        }
    }

    public void disconnect() throws CommandException {
        if (!isConnected()) {
            throw new CommandException("Not connected, nothing to disconnect from.");
        }
        silentDisconnect();
    }
}
