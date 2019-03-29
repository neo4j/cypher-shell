package org.neo4j.shell.log;

import org.neo4j.shell.cli.Format;

import javax.annotation.Nonnull;
import java.io.PrintStream;

public interface Logger {
    /**
     * @return the output stream
     */
    @Nonnull
    PrintStream getOutputStream();

    /**
     * @return the error stream
     */
    @Nonnull
    PrintStream getErrorStream();

    /**
     * Print a sanitized cause of the specified error.
     * If debug mode is enabled, a full stacktrace should be printed as well.
     *
     * @param throwable to print to the error stream
     */
    void printError(@Nonnull Throwable throwable);

    /**
     * Print the designated text to configured error stream.
     *
     * @param text to print to the error stream
     */
    void printError(@Nonnull String text);

    /**
     * Print the designated text to configured output stream.
     *
     * @param text to print to the output stream
     */
    void printOut(@Nonnull String text);

    /**
     * @return the current format of the logger
     */
    @Nonnull
    Format getFormat();

    /**
     * Set the output format on the logger
     *
     * @param format to set
     */
    void setFormat(@Nonnull Format format);

    /**
     * @return true if debug mode is enabled, false otherwise
     */
    boolean isDebugEnabled();

    /**
     * Convenience method which only prints the given text to the output stream if debug mode is enabled
     *
     * @param text to print to the output stream
     */
    default void printIfDebug(@Nonnull String text) {
        if (isDebugEnabled()) {
            printOut(text);
        }
    }

    /**
     * Convenience method which only prints the given text to the output stream if the format set
     * is {@link Format#VERBOSE}.
     *
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
     *
     * @param text to print to the output stream
     */
    default void printIfPlain(@Nonnull String text) {
        if (Format.PLAIN.equals(getFormat())) {
            printOut(text);
        }
    }

    boolean getWrap();

    void setWrap(boolean wrap);

    int getNumSampleRows();

    void setNumSampleRows(int numSampleRows);
}
