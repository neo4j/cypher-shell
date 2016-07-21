package org.neo4j.shell;

import org.junit.Test;

import static org.junit.Assert.assertTrue;


public class HistorianTest {
    @Test
    public void getHistory() throws Exception {
        assertTrue(Historian.empty.getHistory().isEmpty());
    }

}
