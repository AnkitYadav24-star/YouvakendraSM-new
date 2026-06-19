import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Constants {
    // Current application version fallback
    public static final String DEFAULT_CURRENT_VERSION = "1.0.0";
    
    // GitHub repository details
    public static final String GITHUB_OWNER = "AnkitYadav24-star";
    public static final String GITHUB_REPO = "YouvakendraSM-new";
    
    // URLs for updates
    public static final String VERSION_URL = "https://raw.githubusercontent.com/" + GITHUB_OWNER + "/" + GITHUB_REPO + "/main/version.txt";
    
    // File names
    public static final String VERSION_FILE = "version.txt";
    public static final String RUN_BAT = "run.bat";
    public static final String UPDATE_INSTALLER_BAT = "update_installer.bat";
    public static final String UPDATE_ZIP_FILE = "YouvakendraSM_update.zip";
    
    /**
     * Gets the local version from version.txt. Falls back to DEFAULT_CURRENT_VERSION if not found or unreadable.
     */
    public static String getLocalVersion() {
        File file = new File(VERSION_FILE);
        if (file.exists() && file.isFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line = reader.readLine();
                if (line != null) {
                    return line.trim();
                }
            } catch (IOException e) {
                System.err.println("[Updater] Failed to read version.txt: " + e.getMessage());
            }
        }
        return DEFAULT_CURRENT_VERSION;
    }
}
