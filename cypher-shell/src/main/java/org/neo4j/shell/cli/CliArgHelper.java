package org.neo4j.shell.cli;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.action.StoreConstArgumentAction;
import net.sourceforge.argparse4j.impl.choice.CollectionArgumentChoice;
import net.sourceforge.argparse4j.inf.*;

import javax.annotation.Nonnull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.neo4j.shell.cli.FailBehavior.FAIL_AT_END;
import static org.neo4j.shell.cli.FailBehavior.FAIL_FAST;

/**
 * Command line argument parsing and related stuff
 */
public class CliArgHelper {


    public static final Pattern ADDRESS_ARG_PATTERN =
            Pattern.compile("\\s*(?<protocol>[a-zA-Z]+://)?((?<username>\\w+):(?<password>[^\\s]+)@)?(?<host>[\\d\\.\\w]+)?(:(?<port>\\d+))?\\s*");

    @Nonnull
    public static CliArgs parse(@Nonnull String... args) {
        ArgumentParser parser = setupParser();

        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        CliArgs cliArgs = new CliArgs();

        // Parse address string
        Matcher addressMatcher = parseAddressMatcher(parser, ns.getString("address"));

        cliArgs.setHost(addressMatcher.group("host"), "localhost");
        // Safe, regex only matches integers
        String portString = addressMatcher.group("port");
        cliArgs.setPort(portString == null ? 7687 : Integer.parseInt(portString));
        // Also parse username and password from address if available
        cliArgs.setUsername(addressMatcher.group("username"), "");
        cliArgs.setPassword(addressMatcher.group("password"), "");

        // Only overwrite user/pass from address string if the arguments were specified
        String user = ns.getString("username");
        if (!user.isEmpty()) {
            cliArgs.setUsername(user, cliArgs.getUsername());
        }
        String pass = ns.getString("password");
        if (!pass.isEmpty()) {
            cliArgs.setPassword(pass, cliArgs.getPassword());
        }

        // Other arguments
        // cypher string might not be given, represented by null
        cliArgs.setCypher(ns.getString("cypher"));
        // Fail behavior as sensible default and returns a proper type
        cliArgs.setFailBehavior(ns.get("fail-behavior"));

        //Set Output format
        cliArgs.setFormat(Format.parse(ns.get("format")));

        return cliArgs;
    }

    private static Matcher parseAddressMatcher(ArgumentParser parser, String address) {
        Matcher matcher = ADDRESS_ARG_PATTERN.matcher(address);
        if (!matcher.matches()) {
            parser.printUsage();
            System.err.println("cypher-shell: error: Failed to parse address: '" + address + "'");
            System.err.println("\n  Address should be of the form: [username:password@][host][:port]");
            System.exit(1);
        }
        return matcher;
    }

    private static ArgumentParser setupParser() {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("cypher-shell")
                .defaultHelp(true)
                .description("A command line shell where you can execute Cypher against an instance of Neo4j");

        ArgumentGroup connGroup = parser.addArgumentGroup("connection arguments");
        connGroup.addArgument("-a", "--address")
                .help("address and port to connect to")
                .setDefault("localhost:7687");
        connGroup.addArgument("-u", "--username")
                .setDefault("")
                .help("username to connect as");
        connGroup.addArgument("-p", "--password")
                .setDefault("")
                .help("password to connect with");

        MutuallyExclusiveGroup failGroup = parser.addMutuallyExclusiveGroup();
        failGroup.addArgument("--fail-fast")
                .help("exit and report failure on first error when reading from file (this is the default behavior)")
                .dest("fail-behavior")
                .setConst(FAIL_FAST)
                .action(new StoreConstArgumentAction());
        failGroup.addArgument("--fail-at-end")
                .help("exit and report failures at end of input when reading from file")
                .dest("fail-behavior")
                .setConst(FAIL_AT_END)
                .action(new StoreConstArgumentAction());
        parser.setDefault("fail-behavior", FAIL_FAST);

        parser.addArgument("--format")
                .help("desired output format, verbose(default) displays statistics, plain only displays data")
                .choices(new CollectionArgumentChoice<>(
                        Format.VERBOSE.name().toLowerCase(), Format.PLAIN.name().toLowerCase()))
                .setDefault(Format.VERBOSE.name().toLowerCase());

        parser.addArgument("cypher")
                .nargs("?")
                .help("an optional string of cypher to execute and then exit");
        return parser;
    }


}
