package org.neo4j.shell;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.io.PrintStream;

public interface Shell extends CommandExecuter {

    @Nonnull
    InputStream getInputStream();

    @Nonnull
    PrintStream getOutputStream();
}
