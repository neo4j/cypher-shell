package org.neo4j.shell.build;

import org.junit.Test;

import static org.junit.Assert.assertTrue;


public class BuildTest {
    @Test
    public void versionIsNumeric() throws Exception {
        assertTrue(Build.version().matches("\\d+\\.\\d+\\.\\d+.*"));
    }

    @Test
    public void neo4jDriverVersionIsNumeric() throws Exception {
        assertTrue(Build.driverVersion().matches("\\d+\\.\\d+\\.\\d+.*"));
    }
}
