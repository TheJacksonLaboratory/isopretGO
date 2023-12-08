package org.jax.isopret.gui.service.model;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.io.File;

public class Platform {

    /**
     * Get path to directory where HRMD-gui stores global settings.
     * The path depends on underlying operating system. Linux, Windows & OSX
     * currently supported.
     * @return File to directory
     */
    public static File getIsopretFxDir() {
        CurrentPlatform platform = figureOutPlatform();
        File linuxPath = new File(System.getProperty("user.home") + File.separator + ".isopretfx");
        File windowsPath = new File(System.getProperty("user.home") + File.separator + "isopretfx");
        File osxPath = new File(System.getProperty("user.home") + File.separator + ".isopretfx");

        switch (platform) {
            case LINUX -> {
                return linuxPath;
            }
            case WINDOWS -> {
                return windowsPath;
            }
            case OSX -> {
                return osxPath;
            }
            case UNKNOWN -> {
                return null;
            }
            default -> {
                Alert a = new Alert(AlertType.ERROR);
                a.setTitle("Find GUI config dir");
                a.setHeaderText(null);
                a.setContentText(String.format("Unrecognized platform: \"%s\"", platform));
                a.showAndWait();
                return null;
            }
        }
    }

    /**
     * Get the absolute path to the log file.
     * @return the absolute path,e.g., /home/user/.vpvgui/vpvgui.log
     */
    public static String getAbsoluteLogPath() {
        File dir = getIsopretFxDir();
        return dir + File.separator +  "isopretfx.log";
    }


    /* Based on this post: http://www.mkyong.com/java/how-to-detect-os-in-java-systemgetpropertyosname/ */
    private static CurrentPlatform figureOutPlatform() {
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            return CurrentPlatform.LINUX;
        } else if (osName.contains("win")) {
            return CurrentPlatform.WINDOWS;
        } else if (osName.contains("mac")) {
            return CurrentPlatform.OSX;
        } else {
            return CurrentPlatform.UNKNOWN;
        }
    }


    private enum CurrentPlatform {
        LINUX("Linux"),
        WINDOWS("Windows"),
        OSX("Mac OSX"),
        UNKNOWN("Unknown");
        private final String name;

        CurrentPlatform(String n) {this.name = n; }

        @Override
        public String toString() { return this.name; }
    }



}
