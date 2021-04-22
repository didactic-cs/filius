package filius.rahmenprogramm;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class FiliusArgs {
    public String argsString = "";

    public String currWD = Information.initArbeitsbereichPfad;
    public String projectFile;
    public boolean log = false;
    public String newWD = null;
    public boolean nativeLookAndFeel = false;
    public boolean verbose = false;
    public boolean help = false;
    public int rtt = 1;
    public boolean lowResolution;

    private Options options = new Options();

    public FiliusArgs() {
        options.addOption("h", false, "Show usage info and exit");
        options.addOption("v", false, "Activate verbose log");
        options.addOption("w", "wd", true, "The working directory that is used during program execution");
        options.addOption("l", false, "Activate logging to log file");
        options.addOption("r", true, "Factor to adapt round-trip-time in Filius network: 1 (default) to 5");
        options.addOption("n", false, "Activate native look and feel graphical user interface");
        options.addOption("s", false, "Adapt Filius ui for small screen resolution (less than 1024x768)");
    }

    public void parseCommandLine(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption("l")) {
            log = true;
        }
        if (cmd.hasOption("h")) {
            help = true;
        }
        if (cmd.hasOption("v")) {
            verbose = true;
        }
        if (cmd.hasOption("w")) {
            currWD = cmd.getOptionValue("w");
        }
        if (cmd.hasOption("n")) {
            nativeLookAndFeel = true;
        }
        if (cmd.hasOption("s")) {
            lowResolution = true;
        }
        if (cmd.hasOption("r")) {
            try {
                rtt = Integer.parseInt(cmd.getOptionValue("r"));
            } catch (NumberFormatException e) {}
        }

        if (cmd.getArgs().length == 1) {
            try {
                File projectfile = new File(cmd.getArgs()[0]);
                if (projectfile.exists()) {
                    projectFile = projectfile.getAbsolutePath();
                }
            } catch (Exception e) {}
        }
    }

    public void showUsageInformation() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("filius [OPTIONS] [FILE]", options);
    }
}
