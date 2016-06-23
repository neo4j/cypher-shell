package org.neo4j.shell;

import jline.console.ConsoleReader;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.shell.commands.Exit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.function.Supplier;

/**
 * An interactive shell
 */
public class InteractiveShellRunner {
    private final ConsoleReader reader;
    private final CypherShell shell;
    private final Supplier<String> prompt;

    public InteractiveShellRunner(@Nonnull CypherShell shell, @Nonnull Supplier<String> prompt) throws IOException {
        this.shell = shell;
        this.prompt = prompt;
        this.reader = new ConsoleReader();
    }

    public void run() {
        boolean running = true;
        while (running) {
            try {
                running = work();
            } catch (Exit.ExitException e) {
                throw e;
            } catch (ClientException e) {
                shell.printError(BoltHelper.getSensibleMsg(e));
            }
            catch (CommandException e) {
                shell.printError(e.getMessage());
            }
            catch (Throwable t) {
                // TODO: 6/21/16 Unknown errors maybe should be handled differently
                shell.printError(t.getMessage());
            }
        }
    }

    private boolean work() throws IOException, Exit.ExitException, CommandException {
        String line = readLine();

        if (null == line) {
            return false;
        }

        if (!line.trim().isEmpty()) {
            shell.execute(line);
        }

        return true;
    }

    @Nullable
    private String readLine() throws IOException {
        return reader.readLine(prompt.get());

    }
}
