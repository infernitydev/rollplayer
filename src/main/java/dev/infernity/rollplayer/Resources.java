package dev.infernity.rollplayer;

import dev.infernity.rollplayer.files.JarPather;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// A immutable list of Singletons.
public enum Resources {
    INSTANCE;

    private final Logger logger;
    private final FileBasedConfiguration config;

    private Resources() {
        this.logger = LoggerFactory.getLogger("Rollplayer");

        var pather = new JarPather<Resources>();
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class).configure(params.properties()
                        .setBasePath(pather.getFolderWithJarFile(Resources.class))
                        .setFileName("rollplayer.properties"));
        try {
            this.config = builder.getConfiguration();
        } catch (ConfigurationException e) {
            throw new ExceptionInInitializerError("The configuration file (rollplayer.properties) was not found.");
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public FileBasedConfiguration getConfig() {
        return config;
    }
}
