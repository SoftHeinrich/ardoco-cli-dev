package edu.kit.kastel.mcse.ardoco.cli.plugin.task;


/* Licensed under MIT 2023. */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.tlr.execution.ArDoCoForSamCodeTraceabilityLinkRecovery;
import edu.kit.kastel.mcse.ardoco.cli.plugin.core.TaskPlugin;

/**
 * Plugin for architecture model to code traceability link recovery.
 */
public class SamCodeTaskPlugin extends TaskPlugin {
    private static final String PREFIX = "SamCode";
    private static final String TASK_NAME = "sam-code";

    private static final String CMD_MODEL = PREFIX + "-m";
    private static final String CMD_CODE = PREFIX + "-c";

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public String getTaskName() {
        return TASK_NAME;
    }

    @Override
    public List<Option> getRequiredOptions() {
        List<Option> options = new ArrayList<>();

        Option opt = new Option(CMD_MODEL, "model", true, "Path to the model (SAM)");
        opt.setType(String.class);
        opt.setRequired(true);
        options.add(opt);

        opt = new Option(CMD_CODE, "code", true, "Path to the code");
        opt.setType(String.class);
        opt.setRequired(true);
        options.add(opt);

        opt = new Option("n", "name", true, "Name of the project that should be analyzed");
        opt.setType(String.class);
        opt.setRequired(true);
        options.add(opt);

        return options;
    }


    @Override
    public void execute(CommandLine cmd, File outputDir) {
        logger.info("Starting SAM-CODE traceability link recovery task.");

        String name = cmd.getOptionValue("n");
        File sam = null;
        File code = null;

        try {
            sam = ensureFile(cmd.getOptionValue(CMD_MODEL));
            code = getCodeDirectory(cmd.getOptionValue(CMD_CODE));
        } catch (IOException e) {
            logger.error(ERROR_READING_FILES, e);
            return;
        }

        var runner = new ArDoCoForSamCodeTraceabilityLinkRecovery(name);
        runner.setUp(sam, ArchitectureModelType.PCM, code, new TreeMap<>(), outputDir);
        runner.run();

        logger.info("SAM-CODE task completed.");
    }

    /**
     * Gets the code directory, handling both file and directory inputs.
     * @param path the path to the code
     * @return the code directory
     * @throws IOException if the directory doesn't exist
     */
    private File getCodeDirectory(String path) throws IOException {
        try {
            return ensureFile(path);
        } catch (IOException e) {
            var file = new File(path);
            if (file.isDirectory() && file.exists()) {
                return file;
            } else {
                throw new IOException(ERROR_FILE_NOT_EXISTING + path);
            }
        }
    }

    @Override
    public Map<String, String> getOptionPrefixDescriptions() {
        Map<String, String> descriptions = new HashMap<>();
        descriptions.put(CMD_MODEL, "Path to the model (SAM)");
        descriptions.put(CMD_CODE, "Path to the code");
        return descriptions;
    }
}