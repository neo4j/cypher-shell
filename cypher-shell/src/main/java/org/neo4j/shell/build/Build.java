package org.neo4j.shell.build;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This class provides access to build time variables
 */
public class Build {

    private static Properties props = null;

    /**
     * Reads the build generated properties file the first time it is called.
     *
     * @return build properties
     */
    @Nonnull
    private static Properties getProperties() {
        if (props == null) {
            props = new Properties();
            try (InputStream stream = Build.class.getClassLoader().getResourceAsStream("build.properties")) {
                if (stream == null) {
                    throw new IllegalStateException("Cannot read build.properties");
                } else {
                    props.load(stream);
                }
            } catch (IOException e) {
                System.err.println("Could not read build properties: " + e.getMessage());
            }
        }

        return props;
    }

    /**
     * @return the revision of the source code, or "dev" if no properties file could be read.
     */
    @Nonnull
    public static String version() {
        return getProperties().getProperty("version", "dev");
    }

    /**
     * @return the revision of the Neo4j Driver, or "dev" if no properties file could be read.
     */
    @Nonnull
    public static String driverVersion() {
        return getProperties().getProperty("driverVersion", "dev");
    }
}
