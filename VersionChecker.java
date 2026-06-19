import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.awt.Frame;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class VersionChecker {

    /**
     * Fetches the latest version string from the remote GitHub repository.
     * Returns null if the version could not be fetched.
     */
    public static String fetchLatestVersion() {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(Constants.VERSION_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // 5 seconds timeout
            connection.setReadTimeout(5000);    // 5 seconds timeout
            
            // Set User-Agent as standard practice for GitHub API / raw requests
            connection.setRequestProperty("User-Agent", "YouvakendraSM-Updater");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String inputLine = in.readLine();
                    if (inputLine != null) {
                        return inputLine.trim();
                    }
                }
            } else {
                System.err.println("[Updater] Failed to fetch version. HTTP response code: " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("[Updater] Error fetching latest version: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    /**
     * Compares two semantic version strings.
     * Returns true if remoteVersion is newer than localVersion.
     */
    public static boolean isNewerVersion(String localVersion, String remoteVersion) {
        if (localVersion == null || remoteVersion == null) {
            return false;
        }

        // Normalize versions by removing 'v' or 'V' prefixes
        String cleanLocal = localVersion.replaceAll("(?i)^v", "").trim();
        String cleanRemote = remoteVersion.replaceAll("(?i)^v", "").trim();

        // Split by dots or dashes (to handle prereleases like 1.0.0-beta)
        String[] localParts = cleanLocal.split("[.-]");
        String[] remoteParts = cleanRemote.split("[.-]");

        int length = Math.max(localParts.length, remoteParts.length);
        for (int i = 0; i < length; i++) {
            int localVal = 0;
            if (i < localParts.length) {
                try {
                    localVal = Integer.parseInt(localParts[i]);
                } catch (NumberFormatException e) {
                    // Fallback to 0 if not a number
                }
            }

            int remoteVal = 0;
            if (i < remoteParts.length) {
                try {
                    remoteVal = Integer.parseInt(remoteParts[i]);
                } catch (NumberFormatException e) {
                    // Fallback to 0 if not a number
                }
            }

            if (remoteVal > localVal) {
                return true;
            } else if (localVal > remoteVal) {
                return false;
            }
        }

        return false; // Equal versions
    }

    /**
     * Checks for updates in a background thread.
     * If silent is true, shows dialog only when an update is available.
     * If silent is false (manual check), shows feedback on success/failure.
     */
    public static void checkForUpdatesAsync(boolean silent) {
        new Thread(() -> {
            try {
                // Short sleep on startup to let GUI load
                if (silent) {
                    Thread.sleep(1500);
                }

                String localVersion = Constants.getLocalVersion();
                System.out.println("[Updater] Local Version: " + localVersion);
                
                String latestVersion = fetchLatestVersion();
                System.out.println("[Updater] Remote Version: " + latestVersion);

                if (latestVersion != null) {
                    if (isNewerVersion(localVersion, latestVersion)) {
                        SwingUtilities.invokeLater(() -> {
                            try {
                                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                            } catch (Exception ignored) {}
                            
                            UpdateDialog dialog = new UpdateDialog((Frame) null, localVersion, latestVersion);
                            dialog.setVisible(true);
                        });
                    } else {
                        if (!silent) {
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(null, 
                                    "You are running the latest version (" + localVersion + ").", 
                                    "Up to Date", JOptionPane.INFORMATION_MESSAGE);
                            });
                        }
                    }
                } else {
                    if (!silent) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(null, 
                                "Could not check for updates. Please check your connection and try again.", 
                                "Update Check Failed", JOptionPane.ERROR_MESSAGE);
                        });
                    }
                }
            } catch (Exception e) {
                System.err.println("[Updater] Exception during update check: " + e.getMessage());
            }
        }).start();
    }
}
