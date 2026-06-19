import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AutoUpdater {

    public interface ProgressCallback {
        void onProgress(int percent, long bytesDownloaded, long totalBytes);
        void onComplete();
        void onError(String message);
    }

    /**
     * Downloads the update ZIP asynchronously.
     */
    public static void downloadUpdateAsync(String version, ProgressCallback callback) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            InputStream inputStream = null;
            FileOutputStream outputStream = null;
            try {
                // Try downloading from the standard GitHub Release source tag
                String urlString = "https://github.com/" + Constants.GITHUB_OWNER + "/" + Constants.GITHUB_REPO + "/archive/refs/tags/v" + version + ".zip";
                URL url = new URL(urlString);
                
                System.out.println("[Updater] Connecting to: " + urlString);
                connection = openConnectionWithRedirects(url);

                int responseCode = connection.getResponseCode();
                
                // If tag with 'v' prefix returns 404, try without 'v'
                if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    System.out.println("[Updater] Tag with 'v' returned 404, trying without 'v' prefix...");
                    urlString = "https://github.com/" + Constants.GITHUB_OWNER + "/" + Constants.GITHUB_REPO + "/archive/refs/tags/" + version + ".zip";
                    url = new URL(urlString);
                    connection = openConnectionWithRedirects(url);
                    responseCode = connection.getResponseCode();
                }

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    callback.onError("Server returned HTTP response code: " + responseCode + " for update ZIP.");
                    return;
                }

                long totalBytes = connection.getContentLengthLong();
                inputStream = connection.getInputStream();

                File tempZip = new File(Constants.UPDATE_ZIP_FILE);
                outputStream = new FileOutputStream(tempZip);

                byte[] buffer = new byte[8192];
                int bytesRead;
                long bytesDownloaded = 0;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    bytesDownloaded += bytesRead;

                    if (totalBytes > 0) {
                        int percent = (int) ((bytesDownloaded * 100) / totalBytes);
                        callback.onProgress(percent, bytesDownloaded, totalBytes);
                    } else {
                        // Unknown size
                        callback.onProgress(-1, bytesDownloaded, -1);
                    }
                }

                outputStream.close();
                inputStream.close();
                
                System.out.println("[Updater] Download complete. ZIP saved to: " + tempZip.getAbsolutePath());
                callback.onComplete();

            } catch (Exception e) {
                callback.onError("Download error: " + e.getMessage());
            } finally {
                try {
                    if (outputStream != null) outputStream.close();
                    if (inputStream != null) inputStream.close();
                } catch (IOException ignored) {}
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    /**
     * Follows HTTP redirects manually to obtain a connection.
     */
    private static HttpURLConnection openConnectionWithRedirects(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setRequestProperty("User-Agent", "YouvakendraSM-Updater");
        conn.setInstanceFollowRedirects(true);

        int status = conn.getResponseCode();
        int redirectCount = 0;
        
        while ((status == HttpURLConnection.HTTP_MOVED_TEMP || 
                status == HttpURLConnection.HTTP_MOVED_PERM || 
                status == 307 || status == 308) && redirectCount < 5) {
            
            String newUrl = conn.getHeaderField("Location");
            conn.disconnect();
            
            System.out.println("[Updater] Redirected to: " + newUrl);
            url = new URL(newUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("User-Agent", "YouvakendraSM-Updater");
            conn.setInstanceFollowRedirects(true);
            
            status = conn.getResponseCode();
            redirectCount++;
        }
        return conn;
    }

    /**
     * Extracts the downloaded update ZIP file, stripping the top-level directory wrapper.
     */
    public static boolean extractUpdateZip() {
        File zipFile = new File(Constants.UPDATE_ZIP_FILE);
        File destDir = new File("update_temp");
        
        // Clear previous update temp dir if exists
        if (destDir.exists()) {
            deleteDirectory(destDir);
        }
        destDir.mkdirs();

        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            String commonPrefix = null;

            // First pass or inspect first entry to identify the root folder prefix
            // GitHub ZIPs wrap everything in a folder like "YouvakendraSM-1.0.1/"
            // We need to strip this prefix so files go to the root of update_temp.
            byte[] buffer = new byte[8192];
            
            while ((entry = zipIn.getNextEntry()) != null) {
                String entryName = entry.getName();
                
                // Establish the prefix on the first entry
                if (commonPrefix == null) {
                    int firstSlashIndex = entryName.indexOf('/');
                    if (firstSlashIndex != -1) {
                        commonPrefix = entryName.substring(0, firstSlashIndex + 1);
                    } else {
                        commonPrefix = ""; // No prefix
                    }
                }

                // Strip prefix
                String relativePath = entryName;
                if (!commonPrefix.isEmpty() && entryName.startsWith(commonPrefix)) {
                    relativePath = entryName.substring(commonPrefix.length());
                }

                if (relativePath.isEmpty()) {
                    zipIn.closeEntry();
                    continue; // Skip root folder entry itself
                }

                File entryFile = new File(destDir, relativePath);

                if (entry.isDirectory()) {
                    entryFile.mkdirs();
                } else {
                    // Create parent directories if they don't exist
                    File parent = entryFile.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }

                    // Write file
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(entryFile))) {
                        int read;
                        while ((read = zipIn.read(buffer)) != -1) {
                            bos.write(buffer, 0, read);
                        }
                    }
                }
                zipIn.closeEntry();
            }
            
            System.out.println("[Updater] Successfully extracted ZIP into: " + destDir.getAbsolutePath());
            return true;
        } catch (IOException e) {
            System.err.println("[Updater] Failed to extract update ZIP: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Launches the batch script to finalize the update and exits the application.
     */
    public static void installAndRestart() {
        File batchFile = new File(Constants.UPDATE_INSTALLER_BAT);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(batchFile))) {
            writer.println("@echo off");
            writer.println("echo ========================================================");
            writer.println("echo   YouvakendraSM - Installing Updates");
            writer.println("echo ========================================================");
            writer.println("echo Waiting for the main application to close...");
            writer.println("timeout /t 2 /nobreak > nul");
            
            writer.println("echo Copying new application files...");
            // Use xcopy to copy recursively and overwrite all files silently
            writer.println("xcopy /e /y /q \"update_temp\\*\" \".\" > nul");
            
            writer.println("echo Cleaning up update temporary files...");
            writer.println("rmdir /s /q \"update_temp\" > nul");
            
            // Delete the downloaded ZIP file as well
            writer.println("if exist \"" + Constants.UPDATE_ZIP_FILE + "\" del /f /q \"" + Constants.UPDATE_ZIP_FILE + "\" > nul");
            
            writer.println("echo Restarting the application...");
            // Start the application back up using run.bat
            writer.println("start \"\" \"" + Constants.RUN_BAT + "\"");
            
            writer.println("echo Update complete!");
            // Batch file deletes itself
            writer.println("del /f /q \"%~f0\" & exit");
            
        } catch (IOException e) {
            System.err.println("[Updater] Failed to create installer batch script: " + e.getMessage());
            return;
        }

        // Execute batch file
        try {
            System.out.println("[Updater] Launching update_installer.bat and exiting JVM...");
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", batchFile.getAbsolutePath());
            pb.directory(new File("."));
            pb.start();
            
            // Terminate current process to release all file locks
            System.exit(0);
        } catch (IOException e) {
            System.err.println("[Updater] Failed to launch installer batch script: " + e.getMessage());
        }
    }

    /**
     * Utility method to recursively delete a directory.
     */
    public static void deleteDirectory(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDirectory(f);
            }
        }
        file.delete();
    }
}
