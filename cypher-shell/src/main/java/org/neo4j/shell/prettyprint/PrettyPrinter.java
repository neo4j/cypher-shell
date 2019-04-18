package org.neo4j.shell.prettyprint;

import org.neo4j.shell.cli.Format;
import org.neo4j.shell.state.BoltResult;

import javax.annotation.Nonnull;
import java.util.Set;

import static org.neo4j.shell.prettyprint.OutputFormatter.Capabilities.*;

/**
 * Print the result from neo4j in a intelligible fashion.
 */
public class PrettyPrinter {
    private final StatisticsCollector statisticsCollector;
    private final OutputFormatter outputFormatter;

    public PrettyPrinter(@Nonnull PrettyConfig prettyConfig) {
        this.statisticsCollector = new StatisticsCollector(prettyConfig.format);
        this.outputFormatter = selectFormatter(prettyConfig);
    }

    public void format(@Nonnull final BoltResult result, LinePrinter linePrinter) {
        Set<OutputFormatter.Capabilities> capabilities = outputFormatter.capabilities();

        int numberOfRows = 0;
        if (capabilities.contains(RESULT)) {
            numberOfRows = outputFormatter.formatAndCount(result, linePrinter);
        }

        if (capabilities.contains(INFO)) printIfNotEmpty(outputFormatter.formatInfo(result.getSummary()), linePrinter);
        if (capabilities.contains(PLAN)) printIfNotEmpty(outputFormatter.formatPlan(result.getSummary()), linePrinter);
        if (capabilities.contains(FOOTER)) printIfNotEmpty(outputFormatter.formatFooter(result, numberOfRows), linePrinter);
        if (capabilities.contains(STATISTICS)) printIfNotEmpty(statisticsCollector.collect(result.getSummary()), linePrinter);
    }

    // Helper for testing
    String format(@Nonnull final BoltResult result) {
        StringBuilder sb = new StringBuilder();
        format(result, line -> {if (line!=null && !line.trim().isEmpty()) sb.append(line).append(OutputFormatter.NEWLINE);});
        return sb.toString();
    }

    private void printIfNotEmpty( String s, LinePrinter linePrinter ) {
        if (!s.isEmpty()) {
            linePrinter.printOut( s );
        }
    }

    private OutputFormatter selectFormatter(PrettyConfig prettyConfig) {
        if (prettyConfig.format == Format.VERBOSE) {
            return new TableOutputFormatter(prettyConfig.wrap, prettyConfig.numSampleRows);
        } else {
            return new SimpleOutputFormatter();
        }
    }
}
