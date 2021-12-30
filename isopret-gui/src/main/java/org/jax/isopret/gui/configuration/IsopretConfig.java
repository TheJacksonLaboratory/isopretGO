package org.jax.isopret.gui.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class IsopretConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(IsopretConfig.class);

    public static final String ISOPRET_SETTINGS_FILE_BASENAME = "isopretfx.properties";


    @Bean
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    /**
     * Properties meant to store user configuration within the app's directory
     *
     * @param configFilePath path where the properties file is supposed to be present (it's ok if the file itself doesn't exist).
     * @return {@link Properties} with user configuration
     */
    @Bean(value="pgProperties")
    @Primary
    public Properties pgProperties(@Qualifier("isopretSettingsFile") File configFilePath) {
        Properties properties = new Properties();
        if (configFilePath.isFile()) {
            try (InputStream is = Files.newInputStream(configFilePath.toPath())) {
                properties.load(is);
            } catch (IOException e) {
                LOGGER.warn("Error during reading `{}`", configFilePath, e);
            }
        }
        return properties;
    }


    @Bean("isopretSettingsFile")
    public File isopretSettingsFile(File isopretDirectory){
        return new File(isopretDirectory + File.separator + ISOPRET_SETTINGS_FILE_BASENAME);
    }

    @Bean("isopretDirectory")
    public File isopretDirectory() throws IOException {
        String osName = System.getProperty("os.name").toLowerCase();
        File appHomeDir;
        if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) { // Unix
            appHomeDir = new File(System.getProperty("user.home") + File.separator + ".isopretfx");
        } else if (osName.contains("win")) { // Windows
            appHomeDir = new File(System.getProperty("user.home") + File.separator + "isopretfx");
        } else if (osName.contains("mac")) { // OsX
            appHomeDir = new File(System.getProperty("user.home") + File.separator + ".isopretfx");
        } else { // unknown platform
            appHomeDir = new File(System.getProperty("user.home") + File.separator + "isopretfx");
        }

        if (!appHomeDir.exists()) {
            LOGGER.debug("App home directory does not exist at {}", appHomeDir.getAbsolutePath());
            if (!appHomeDir.getParentFile().exists() && !appHomeDir.getParentFile().mkdirs()) {
                LOGGER.warn("Unable to create parent directory for app home at {}",
                        appHomeDir.getParentFile().getAbsolutePath());
                throw new IOException("Unable to create parent directory for app home at " +
                        appHomeDir.getParentFile().getAbsolutePath());
            } else {
                if (!appHomeDir.mkdir()) {
                    LOGGER.warn("Unable to create app home directory at {}", appHomeDir.getAbsolutePath());
                    throw new IOException("Unable to create app home directory at " + appHomeDir.getAbsolutePath());
                } else {
                    LOGGER.info("Created app home directory at {}", appHomeDir.getAbsolutePath());
                }
            }
        }
        return appHomeDir;
    }
}
