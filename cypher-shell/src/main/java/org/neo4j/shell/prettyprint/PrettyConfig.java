package org.neo4j.shell.prettyprint;

import org.neo4j.shell.cli.CliArgs;
import org.neo4j.shell.cli.Format;

/**
 * Configuration of pretty printer.
 */
public class PrettyConfig {

    public static final PrettyConfig DEFAULT = new PrettyConfig(new CliArgs());

    public final Format format;
    public final boolean wrap;
    public final int numSampleRows;

    public PrettyConfig(CliArgs cliArgs) {
        this(selectFormat(cliArgs), cliArgs.getWrap(), cliArgs.getNumSampleRows());
    }

    private static Format selectFormat(CliArgs cliArgs) {
        if (cliArgs.isStringShell() && Format.AUTO.equals(cliArgs.getFormat())) {
            return Format.PLAIN;
        }
        return cliArgs.getFormat();
    }

    public PrettyConfig(Format format, boolean wrap, int numSampleRows) {
        this.format = format;
        this.wrap = wrap;
        this.numSampleRows = numSampleRows;
    }
}
