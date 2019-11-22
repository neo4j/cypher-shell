package org.neo4j.shell.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VersionsTest
{
    @Test
    public void shouldWorkForEmptyString() throws Exception {
        assertEquals(0, Versions.version("").compareTo(Versions.version("0.0.0")));
        assertEquals(0, Versions.majorVersion(""));
        assertEquals(0, Versions.minorVersion(""));
        assertEquals(0, Versions.patch(""));
    }

    @Test
    public void shouldWorkForReleaseVersion() throws Exception {
        String versionString = "3.4.5";
        assertEquals(0, Versions.version(versionString).compareTo(Versions.version("3.4.5")));
        assertEquals(3, Versions.majorVersion(versionString));
        assertEquals(4, Versions.minorVersion(versionString));
        assertEquals(5, Versions.patch(versionString));
    }

    @Test
    public void shouldWorkForPreReleaseVersion() throws Exception {
        String versionString = "3.4.55-beta99";
        assertEquals(0, Versions.version(versionString).compareTo(Versions.version("3.4.55")));
        assertEquals(3, Versions.majorVersion(versionString));
        assertEquals(4, Versions.minorVersion(versionString));
        assertEquals(55, Versions.patch(versionString));
    }
}
