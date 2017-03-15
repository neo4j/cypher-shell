package org.neo4j.shell.system;

/**
 * Utility functions
 */
public class Utils {
    /**
     * @return true if running on Windows, false if not or if os could not be determined.
     */
    public static boolean isWindows() {
        String osName = System.getProperty("os.name");
        return osName != null && osName.toLowerCase().contains("windows");
    }
}
