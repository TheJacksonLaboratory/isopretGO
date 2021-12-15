package org.jax.isopret.gui;


import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.jax.isopret.gui.configuration.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;

@Component
public class StageInitializer implements ApplicationListener<IsopretFxApplication.StageReadyEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StageInitializer.class);

    private final ApplicationContext applicationContext;
    private final ApplicationProperties applicationProperties;

    public StageInitializer(ApplicationProperties props, ApplicationContext context) {
        this.applicationContext = context;
        this.applicationProperties = props;
    }


    @Override
    public void onApplicationEvent(IsopretFxApplication.StageReadyEvent event) {
        try {
            ClassPathResource gopherResource = new ClassPathResource("fxml/isopretmain.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(gopherResource.getURL());
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            Parent parent = fxmlLoader.load();
            Stage stage = event.getStage();
            stage.setScene(new Scene(parent, 1200, 900));
            stage.setResizable(true);
            stage.setTitle(applicationProperties.getApplicationUiTitle());
            readAppIcon().ifPresent(stage.getIcons()::add);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Optional<Image> readAppIcon() {
        if (Platform.isMacintosh()) {
            try {
                URL iconURL = StageInitializer.class.getResource("/img/phenomenon.png");

                java.awt.Image macimage = new ImageIcon(iconURL).getImage();
                // not working
               // com.apple.eawt.Application.getApplication().setDockIconImage(macimage);
            } catch (Exception e) {
                // Won't work on Windows or Linux. Just skip it!
            }
        }
        try (InputStream is = StageInitializer.class.getResourceAsStream("/img/phenomenon.png")) {
            if (is != null) {
                return Optional.of(new Image(is));
            }
        } catch (IOException e) {
            LOGGER.warn("Error reading app icon {}", e.getMessage());
        }
        return Optional.empty();
    }
}
