package dev.infernity.rollplayer;

import dev.infernity.rollplayer.files.JarPather;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/// A immutable list of Singletons.
public enum Resources {
    INSTANCE;

    private final Logger logger;
    private final FileBasedConfiguration config;
    private final String version;
    private final String name;
    private final String timestamp;

    Resources() {
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
        String _version, _name, _timestamp;
        try (InputStream stream = getClass().getResourceAsStream("/application-details.properties")) {
            Objects.requireNonNull(stream);
            Properties props = new Properties();
            props.load(stream);
            _version = initializeVersion(props);
            _name = initializeName(props);
            _timestamp = initializeTimestamp(props);
        } catch (IOException | NullPointerException e) {
            _version = "(unknown version)";
            _name = "(unknown name)";
            _timestamp = "(unknown timestamp)";
        }
        version = _version;
        name = _name;
        timestamp = _timestamp;
    }

    private String initializeVersion(Properties properties) throws IOException {
        var version = properties.get("application.version");
        if (version == null) {
            return "(unknown version)";
        }
        return (String) version;
    }

    private String initializeName(Properties properties) throws IOException {
        var name = properties.get("application.name");
        if (name == null) {
            return "(unknown name)";
        }
        return (String) name;
    }

    private String initializeTimestamp(Properties properties) throws IOException {
        var timestamp = properties.get("application.buildtime");
        if (timestamp == null) {
            return "(unknown timestamp)";
        }
        return (String) timestamp;
    }

    public Logger getLogger() {
        return logger;
    }

    public FileBasedConfiguration getConfig() {
        return config;
    }

    public String getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
