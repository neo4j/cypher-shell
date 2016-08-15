package org.neo4j.shell.log;

import org.neo4j.shell.cli.Format;

import javax.annotation.Nonnull;
import java.io.PrintStream;

public interface Logger {
    /**
     *
     * @return the output stream
     */
    @Nonnull
    PrintStream getOutputStream();

    /**
     *
     * @return the error stream
     */
    @Nonnull
    PrintStream getErrorStream();

    /**
     * Print the designated text to configured error stream.
     * @param text to print to the error stream
     */
    void printError(@Nonnull String text);

    /**
     * Print the designated text to configured output stream.
     * @param text to print to the output stream
     */
    void printOut(@Nonnull String text);

    /**
     * Set the output format on the logger
     * @param format to set
     */
    void setFormat(@Nonnull Format format);

    /**
     *
     * @return the current format of the logger
     */
    @Nonnull
    Format getFormat();

    /**
     * Convenience method which only prints the given text to the output stream if the format set
     * is {@link Format#VERBOSE}.
     * @param text to print to the output stream
     */
    default void printIfVerbose(@Nonnull String text) {
        if (Format.VERBOSE.equals(getFormat())) {
            printOut(text);
        }
    }

    /**
     * Convenience method which only prints the given text to the output stream if the format set
     * is {@link Format#PLAIN}.
     * @param text to print to the output stream
     */
    default void printIfPlain(@Nonnull String text) {
        if (Format.PLAIN.equals(getFormat())) {
            printOut(text);
        }
    }
}
