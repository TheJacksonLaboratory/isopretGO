package org.jax.isopret.gui;

/*-
 * #%L
 * PhenoteFX
 * %%
 * Copyright (C) 2017 - 2021 Peter Robinson
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;


public class IsopretFxApplication extends Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(IsopretFxApplication.class);
    private ConfigurableApplicationContext applicationContext;
    static public final String ISOPRETFX_NAME_KEY = "isopretfx.name";
    static private final String ISOPRETFX_VERSION_PROP_KEY = "isopretfx.version";


    @Override
    public void start(Stage stage) {

        applicationContext.publishEvent(new StageReadyEvent(stage));
        // (Simulation of heavy background work)
        int numberOfUpdates = 10;
        for (int i = 0; i < numberOfUpdates; i++) {
            // Gradually update the loading bar
            try {
                notifyPreloader(new Preloader.ProgressNotification((double) i / numberOfUpdates));
                Thread.sleep(3000L / numberOfUpdates);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void init() {
        ApplicationContextInitializer<GenericApplicationContext> initializer = genericApplicationContext -> {
            genericApplicationContext.registerBean(Application.class, () -> IsopretFxApplication.this);
            genericApplicationContext.registerBean(Parameters.class, this::getParameters);
            genericApplicationContext.registerBean(HostServices.class, this::getHostServices);
        };
        applicationContext = new SpringApplicationBuilder(StockUiApplication.class)
                .sources(IsopretFxApplication.class)
                .initializers(initializer).run();

        ClassPathResource applicationProps =  new ClassPathResource("application.properties");
        Objects.requireNonNull(applicationProps);
        // export app's version into System properties
       try (Reader is = new InputStreamReader(applicationProps.getInputStream())) {
            Properties properties = new Properties();
            properties.load(is);
            String version = "1.2";//properties.getProperty(FENOMINAL_VERSION_PROP_KEY, "unknown version");
            System.setProperty(ISOPRETFX_VERSION_PROP_KEY, version);
            String name = properties.getProperty(ISOPRETFX_NAME_KEY, "PhenoteFX");
            System.setProperty(ISOPRETFX_NAME_KEY, name);
        } catch (IOException e) {
           LOGGER.error("Could not load application properties: {}", e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() throws Exception {
        super.stop();
        final Properties pgProperties = applicationContext.getBean("pgProperties", Properties.class);
        final File configFile = applicationContext.getBean("isopretSettingsFile", File.class);
        final Path configFilePath = configFile.toPath();
        try (OutputStream os = Files.newOutputStream(configFilePath)) {
            pgProperties.store(os, "IsopretFX properties");
        }
        Platform.exit();
        applicationContext.close();
    }


    static class StageReadyEvent extends ApplicationEvent {
        public StageReadyEvent(Stage stage) {
            super(stage);
        }
        public Stage getStage() {
            return ((Stage) getSource());
        }
    }



}

/*private ConfigurableApplicationContext context;

    @Override
    public void init() throws Exception {
        ApplicationContextInitializer<GenericApplicationContext> initializer = new ApplicationContextInitializer<GenericApplicationContext>() {
            @Override
            public void initialize(GenericApplicationContext genericApplicationContext) {
                genericApplicationContext.registerBean(Application.class, () -> JavafxApplication.this);
                genericApplicationContext.registerBean(Parameters.class, () -> getParameters());
                genericApplicationContext.registerBean(HostServices.class, () -> getHostServices());
            }
        };

        this.context = new SpringApplicationBuilder().sources(BootifulFxApplication.class)
                .initializers(initializer)
                .build().run(getParameters().getRaw().toArray(new String[0]));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.context.publishEvent(new StageReadyEvent(primaryStage));
    }

    @Override
    public void stop() throws Exception {
        this.context.close();
        Platform.exit();
    }

    class StageReadyEvent extends ApplicationEvent {

        public Stage getStage() {
            return Stage.class.cast(getSource());
        }

        public StageReadyEvent(Object source) {
            super(source);
        }
    }

 */
