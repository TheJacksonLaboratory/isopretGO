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
import javafx.application.Platform;
import javafx.stage.Stage;
import org.jax.isopret.gui.service.HostServicesWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;


public class IsopretFxApplication extends Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(IsopretFxApplication.class);
    private ConfigurableApplicationContext applicationContext;
    static public final String ISOPRETFX_NAME_KEY = "isopretfx.name";
    static private final String ISOPRETFX_VERSION_PROP_KEY = "isopretfx.version";


    @Override
    public void start(Stage stage) {
        applicationContext.publishEvent(new StageReadyEvent(stage));
    }

    /**
     * Note that we set headless mode to be false because SpringBoot seems to set it to true on Mac,
     * which in turn stops getHostServices from working.
     */
    @Override
    public void init() {
        ApplicationContextInitializer<GenericApplicationContext> initializer = genericApplicationContext -> {
            genericApplicationContext.registerBean(Application.class, () -> IsopretFxApplication.this);
            genericApplicationContext.registerBean(Parameters.class, this::getParameters);
            genericApplicationContext.registerBean(HostServicesWrapper.class, this::getHostServicesWrapper);
        };
        applicationContext = new SpringApplicationBuilder(StockUiApplication.class)
                .sources(IsopretFxApplication.class)
                .headless(false)
                .initializers(initializer).run();

    }

    private HostServicesWrapper getHostServicesWrapper() {
        return HostServicesWrapper.wrap(getHostServices());
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
        applicationContext.close();
        Platform.exit();
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
