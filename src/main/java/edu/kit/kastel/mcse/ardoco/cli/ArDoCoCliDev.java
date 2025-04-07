package edu.kit.kastel.mcse.ardoco.cli;


/* Licensed under MIT 2023. */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.cli.plugin.core.PluginManager;
import edu.kit.kastel.mcse.ardoco.cli.plugin.task.SadCodeTaskPlugin;
import edu.kit.kastel.mcse.ardoco.cli.plugin.task.SadSamTaskPlugin;
import edu.kit.kastel.mcse.ardoco.cli.plugin.task.SamCodeTaskPlugin;

/**
 * Main class for the ArDoCo command line interface using a plugin architecture.
 */
public class ArDoCoCliDev {
    private static final Logger logger = LoggerFactory.getLogger(ArDoCoCli.class);

    /**
     * Private constructor to prevent instantiation.
     */
    private ArDoCoCliDev() {
        throw new IllegalAccessError();
    }

    /**
     * Main method for the ArDoCo CLI.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // Create plugin manager
        PluginManager pluginManager = new PluginManager();

        // Register plugins manually (in a real implementation, these would be discovered via ServiceLoader)
        pluginManager.addPlugin(new SadSamTaskPlugin());
        pluginManager.addPlugin(new SamCodeTaskPlugin());
        pluginManager.addPlugin(new SadCodeTaskPlugin());


        // Execute plugins based on command line arguments
        pluginManager.executePlugins(args);
    }
}
