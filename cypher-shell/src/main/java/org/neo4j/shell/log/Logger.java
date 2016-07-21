package org.neo4j.shell.log;

import javax.annotation.Nonnull;
import java.io.PrintStream;

public interface Logger {
    @Nonnull
    PrintStream getOutputStream();

    void printError(@Nonnull String s);

    void printOut(@Nonnull String s);

    @Nonnull
    PrintStream getErrorStream();
}
