package org.neo4j.shell.log;

import org.junit.Before;
import org.junit.Test;

import java.io.PrintStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class AnsiLoggerTest {

    private PrintStream out;
    private PrintStream err;
    private AnsiLogger logger;

    @Before
    public void setup() {
        out = mock(PrintStream.class);
        err = mock(PrintStream.class);
        logger = new AnsiLogger(out, err);
    }

    @Test
    public void defaultStreams() throws Exception {
        Logger logger = new AnsiLogger();

        assertEquals(System.out, logger.getOutputStream());
        assertEquals(System.err, logger.getErrorStream());
    }

    @Test
    public void customStreams() throws Exception {
        assertEquals(out, logger.getOutputStream());
        assertEquals(err, logger.getErrorStream());
    }

    @Test
    public void printError() throws Exception {
        logger.printError("bob");
        verify(err).println("bob");
    }

    @Test
    public void printOut() throws Exception {
        logger.printOut("sob");
        verify(out).println("sob");
    }

    @Test
    public void printOutManyShouldNotBuildState() throws Exception {
        logger.printOut("bob");
        logger.printOut("nob");
        logger.printOut("cod");

        verify(out).println("bob");
        verify(out).println("nob");
        verify(out).println("cod");
    }

    @Test
    public void printErrManyShouldNotBuildState() throws Exception {
        logger.printError("bob");
        logger.printError("nob");
        logger.printError("cod");

        verify(err).println("bob");
        verify(err).println("nob");
        verify(err).println("cod");
    }
}
