package org.neo4j.shell.log;

import org.neo4j.driver.Logger;

public class NullLogger implements Logger
{
    public static final Logger NULL_LOGGER = new NullLogger();

    public NullLogger()
    {
    }

    @Override
    public void error( String message, Throwable cause )
    {
    }

    @Override
    public void info( String message, Object... params )
    {
    }

    @Override
    public void warn( String message, Object... params )
    {
    }

    @Override
    public void warn( String message, Throwable cause )
    {
    }

    @Override
    public void debug( String message, Object... params )
    {
    }

    @Override
    public void trace( String message, Object... params )
    {
    }

    @Override
    public boolean isTraceEnabled()
    {
        return false;
    }

    @Override
    public boolean isDebugEnabled()
    {
        return false;
    }
}
