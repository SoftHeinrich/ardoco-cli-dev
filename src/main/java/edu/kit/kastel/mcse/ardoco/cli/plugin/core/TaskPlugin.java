package edu.kit.kastel.mcse.ardoco.cli.plugin.core;

/* Licensed under MIT 2023. */

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for task plugins.
 */
public abstract class TaskPlugin {
    protected static final Logger logger = LoggerFactory.getLogger(TaskPlugin.class);
    protected static final String ERROR_FILE_NOT_EXISTING = "The specified file does not exist and/or could not be created: ";
    protected static final String ERROR_READING_FILES = "Error in reading files and/or directories!";

    /**
     * Gets the prefix for this plugin's options.
     * @return the prefix string
     */
    public abstract String getPrefix();

    /**
     * Gets the task name this plugin handles.
     * @return the task name
     */
    public abstract String getTaskName();

    /**
     * Gets the required options for this plugin.
     * @return list of required options
     */
    public abstract List<Option> getRequiredOptions();


    /**
     * Executes the plugin task.
     * @param cmd the command line
     * @param outputDir the output directory
     */
    public abstract void execute(CommandLine cmd, File outputDir);

    /**
     * Checks if this plugin handles the given task.
     * @param task the task name
     * @return true if this plugin handles the task
     */
    public boolean canHandle(String task) {
        return getTaskName().equalsIgnoreCase(task);
    }

    /**
     * Validates that all required parameters are present.
     * @param cmd the command line
     * @return true if all required parameters are present
     */
    public boolean validateParameters(CommandLine cmd) {
        for (Option option : getRequiredOptions()) {
            if (!cmd.hasOption(option.getOpt())) {
                logger.error("Missing required parameter: {}", option.getLongOpt());
                return false;
            }
        }
        return true;
    }

    /**
     * Gets all options (required and optional) for this plugin.
     * @return list of all options
     */
    public List<Option> getAllOptions() {
        List<Option> allOptions = getRequiredOptions();
        return allOptions;
    }

    /**
     * Ensure that a file exists.
     * @param path the path to the file
     * @return the file
     * @throws IOException if something went wrong
     */
    protected File ensureFile(String path) throws IOException {
        if (path == null || path.isBlank()) {
            throw new IOException(ERROR_FILE_NOT_EXISTING + path);
        }
        var file = new File(path);
        if (file.exists()) {
            return file;
        }
        // File not available
        throw new IOException(ERROR_FILE_NOT_EXISTING + path);
    }

    /**
     * Ensure that a directory exists (or create).
     * @param path the path to the file
     * @return the file
     */
    protected File ensureDir(String path) {
        var file = new File(path);
        if (file.isDirectory() && file.exists()) {
            return file;
        }
        file.mkdirs();
        return file;
    }

    /**
     * Gets a map of option prefixes and descriptions for help text.
     * @return map of prefixes to descriptions
     */
    public abstract Map<String, String> getOptionPrefixDescriptions();
}