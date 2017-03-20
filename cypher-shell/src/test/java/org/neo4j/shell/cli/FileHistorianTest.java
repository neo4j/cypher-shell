package org.neo4j.shell.cli;


import jline.console.ConsoleReader;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.neo4j.shell.Historian;
import org.neo4j.shell.log.Logger;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.System.getProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class FileHistorianTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private Logger logger = mock(Logger.class);
    private InputStream mockedInput = mock(InputStream.class);
    private ConsoleReader reader = mock(ConsoleReader.class);

    @Before
    public void setup() {
        doReturn(System.out).when(logger).getOutputStream();
    }

    @Test
    public void defaultHistoryFile() throws Exception {
        Path expectedPath = Paths.get(getProperty("user.home"), ".neo4j", ".neo4j_history");

        File history = FileHistorian.getDefaultHistoryFile();
        assertEquals(expectedPath.toString(), history.getPath());
    }

    @Test
    public void noHistoryFileGivesMemoryHistory() throws Exception {
        File historyFile = Paths.get(temp.newFolder().getAbsolutePath(), "asfasd", "zxvses", "fanjtaacf").toFile();
        assertFalse(historyFile.getParentFile().isDirectory());
        assertFalse(historyFile.getParentFile().getParentFile().isDirectory());
        Historian historian = FileHistorian.setupHistory(reader, logger, historyFile);

        assertNotNull(historian);

        verify(logger).printError(contains("Could not load history file. Falling back to session-based history.\n" +
                "Failed to create directory for history"));
    }
}
