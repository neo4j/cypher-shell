package org.neo4j.shell.log;

import org.neo4j.driver.Logger;
import org.neo4j.driver.Logging;

public class NullLogging implements Logging
{
    public static final Logging NULL_LOGGING = new NullLogging();

    public NullLogging()
    {
    }

    @Override
    public Logger getLog( String name )
    {
        return NullLogger.NULL_LOGGER;
    }
}
