package edu.kit.kastel.mcse.ardoco.cli.plugin.core;

/* Licensed under MIT 2023. */

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager for task plugins.
 */
public class PluginManager {
    private static final Logger logger = LoggerFactory.getLogger(PluginManager.class);

    private final List<TaskPlugin> plugins;
    private final Options options;
    private final Map<String, TaskPlugin> taskNameToPlugin;

    /**
     * Constructor for the plugin manager.
     */
    public PluginManager() {
        this.plugins = new ArrayList<>();
        this.options = new Options();
        this.taskNameToPlugin = new HashMap<>();

        // Discover and load plugins
        discoverPlugins();

        // Add common options
        addCommonOptions();
    }

    /**
     * Discovers plugins using Java's ServiceLoader.
     */
    private void discoverPlugins() {
        ServiceLoader<TaskPlugin> serviceLoader = ServiceLoader.load(TaskPlugin.class);

        // For each discovered plugin
        for (TaskPlugin plugin : serviceLoader) {
            plugins.add(plugin);
            taskNameToPlugin.put(plugin.getTaskName().toLowerCase(), plugin);

            // Add plugin's options to global options
            for (Option option : plugin.getAllOptions()) {
                options.addOption(option);
            }

            logger.info("Loaded plugin: {}", plugin.getTaskName());
        }
    }

    /**
     * Manually adds a plugin.
     * @param plugin the plugin to add
     */
    public void addPlugin(TaskPlugin plugin) {
        plugins.add(plugin);
        taskNameToPlugin.put(plugin.getTaskName().toLowerCase(), plugin);

        // Add plugin's options to global options
        for (Option option : plugin.getAllOptions()) {
            options.addOption(option);
        }
    }

    /**
     * Adds common command line options.
     */
    private void addCommonOptions() {
        Option opt;

        // Help option
        opt = new Option("h", "help", false, "Show this message");
        opt.setRequired(false);
        options.addOption(opt);

        // Output directory
        opt = new Option("o", "output", true, "Path to the output directory");
        opt.setType(String.class);
        opt.setRequired(false);
        options.addOption(opt);

        // Task selection
        opt = new Option("t", "task", true, "Specify the TLR-task to perform. Valid options are: "
                + String.join(", ", taskNameToPlugin.keySet()) + ", ALL");
        opt.setType(String.class);
        opt.setRequired(false);
        options.addOption(opt);

        // Project name
        opt = new Option("n", "name", true, "Name of the project that should be analyzed");
        opt.setType(String.class);
        opt.setRequired(false);
        options.addOption(opt);
    }

    /**
     * Executes the appropriate plugins based on command line.
     * @param args command line arguments
     */
    public void executePlugins(String[] args) {
        CommandLine cmd;
        try {
            cmd = parseCommandLine(args);
        } catch (IllegalArgumentException | ParseException e) {
            logger.error(e.getMessage());
            printUsage();
            return;
        }

        // Show help and exit if requested
        if (cmd.hasOption("h")) {
            printUsage();
            return;
        }

        // Check if output directory is specified
        if (!cmd.hasOption("o")) {
            logger.error("No output directory specified.");
            return;
        }

        File outputDir = new File(cmd.getOptionValue("o"));
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // Execute based on task
        if (cmd.hasOption("t")) {
            String task = cmd.getOptionValue("t").toLowerCase();

            if ("all".equals(task)) {
                // Run all plugins
                for (TaskPlugin plugin : plugins) {
                    if (plugin.validateParameters(cmd)) {
                        plugin.execute(cmd, outputDir);
                    } else {
                        logger.error("Cannot execute plugin {} due to missing parameters", plugin.getTaskName());
                    }
                }
            } else if (taskNameToPlugin.containsKey(task)) {
                // Run specific plugin
                TaskPlugin plugin = taskNameToPlugin.get(task);
                if (plugin.validateParameters(cmd)) {
                    plugin.execute(cmd, outputDir);
                } else {
                    logger.error("Cannot execute plugin {} due to missing parameters", plugin.getTaskName());
                }
            } else {
                logger.error("Invalid task provided: {}", task);
                printUsage();
            }
        } else {
            logger.error("No task specified. Use the task parameter to specify which task to perform.");
            printUsage();
        }

        // Cleanup after execution
        cleanup(outputDir);
    }

    /**
     * Parses the command line arguments.
     * @param args the arguments
     * @return the parsed command line
     * @throws ParseException if parsing fails
     */
    private CommandLine parseCommandLine(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        return parser.parse(options, args);
    }

    /**
     * Prints usage information.
     */
    private void printUsage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar ardoco-cli.jar", options);

        // Print plugin-specific help
        System.out.println("\nPlugin-specific parameters:");
        for (TaskPlugin plugin : plugins) {
            System.out.println("\n" + plugin.getTaskName() + " plugin:");
            for (Map.Entry<String, String> entry : plugin.getOptionPrefixDescriptions().entrySet()) {
                System.out.printf("  %-10s %s%n", entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Cleans up temporary files.
     * @param outputDir the output directory
     */
    private void cleanup(File outputDir) {
        // Delete temporary files
        final String[] patternsToDelete = {
            "inconsistencyDetection_.*\\.txt",
            "traceLinks_.*\\.txt"
        };

        for (String pattern : patternsToDelete) {
            final File[] files = outputDir.listFiles((dir, name) -> name.matches(pattern));
            if (files != null) {
                for (File file : files) {
                    try {
                        file.delete();
                    } catch (Exception e) {
                        logger.warn("Error deleting temporary file: {}", file.getName(), e);
                    }
                }
            }
        }
    }
}