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
import edu.kit.kastel.mcse.ardoco.tlr.execution.ArDoCoForSadSamTraceabilityLinkRecovery;
import edu.kit.kastel.mcse.ardoco.cli.plugin.core.TaskPlugin;

/**
 * Plugin for SAD-SAM traceability link recovery.
 */
public class SadSamTaskPlugin extends TaskPlugin {
    private static final String PREFIX = "SadSam";
    private static final String TASK_NAME = "sad-sam";

    private static final String CMD_SAD = PREFIX + "-d";
    private static final String CMD_MODEL = PREFIX + "-m";

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

        Option opt = new Option(CMD_SAD, "documentation", true, "Path to the documentation (SAD)");
        opt.setType(String.class);
        opt.setRequired(true);
        options.add(opt);

        opt = new Option(CMD_MODEL, "model", true, "Path to the model (SAM)");
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
        logger.info("Starting SAD-SAM traceability link recovery task.");

        String name = cmd.getOptionValue("n");
        File sad = null;
        File sam = null;

        try {
            sad = ensureFile(cmd.getOptionValue(CMD_SAD));
            sam = ensureFile(cmd.getOptionValue(CMD_MODEL));
        } catch (IOException e) {
            logger.error(ERROR_READING_FILES, e);
            return;
        }

        var runner = new ArDoCoForSadSamTraceabilityLinkRecovery(name);
        runner.setUp(sad, sam, ArchitectureModelType.PCM, new TreeMap<>(), outputDir);
        runner.run();

        logger.info("SAD-SAM task completed.");
    }

    @Override
    public Map<String, String> getOptionPrefixDescriptions() {
        Map<String, String> descriptions = new HashMap<>();
        descriptions.put(CMD_SAD, "Path to the documentation (SAD)");
        descriptions.put(CMD_MODEL, "Path to the model (SAM)");
        return descriptions;
    }
}