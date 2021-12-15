package org.jax.isopret.gui.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApplicationProperties {

    private final String applicationUiTitle;

    private final String applicationVersion;

    @Autowired
    public ApplicationProperties(@Value("${application.title}") String uiTitle,
                                 @Value("${application.version") String version) {
        this.applicationUiTitle = uiTitle;
        this.applicationVersion = version;
    }



    public String getApplicationUiTitle() {
        return applicationUiTitle;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }
}
