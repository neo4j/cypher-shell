package org.neo4j.shell.log;

import org.junit.Before;
import org.junit.Test;

import java.io.PrintStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;


public class AnsiLoggerTest {

    private PrintStream out;
    private PrintStream err;
    private AnsiLogger logger;
    private StringBuilder sb;

    @Before
    public void setup() {
        out = mock(PrintStream.class);
        err = mock(PrintStream.class);
        sb = new StringBuilder();
        logger = new AnsiLogger(out, err, sb);
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
        assertEquals("bob", sb.toString());
    }

    @Test
    public void printOut() throws Exception {
        logger.printOut("sob");
        assertEquals("sob", sb.toString());
    }
}
