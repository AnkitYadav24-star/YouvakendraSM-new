import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;

public class AutoUpdater {

    public interface ProgressCallback {
        void onProgress(int percent, long bytesDownloaded, long totalBytes);
        void onComplete();
        void onError(String message);
    }

    // Static variables to track extraction error details for UI consumption
    public static String lastExceptionMessage = "";
    public static String lastFailedFilePath = "";

    private static final DateTimeFormatter LOG_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Appends a detailed log message to updater.log and standard out/err streams.
     */
    private static synchronized void log(String level, String message, Throwable t) {
        String timestamp = LocalDateTime.now().format(LOG_DATE_FORMAT);
        String logLine = String.format("[%s] [%s] %s", timestamp, level, message);
        if (t != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            logLine += "\n" + sw.toString();
        }

        if ("ERROR".equals(level)) {
            System.err.println(logLine);
        } else {
            System.out.println(logLine);
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter("updater.log", true))) {
            pw.println(logLine);
        } catch (IOException e) {
            System.err.println("[Updater] Failed to write to updater.log: " + e.getMessage());
        }
    }

    public static void logInfo(String message) {
        log("INFO", message, null);
    }

    public static void logError(String message, Throwable t) {
        log("ERROR", message, t);
    }

    public static void logDebug(String message) {
        log("DEBUG", message, null);
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
                lastExceptionMessage = "";
                lastFailedFilePath = "";

                String urlString = "https://github.com/" + Constants.GITHUB_OWNER + "/" + Constants.GITHUB_REPO + "/releases/download/v" + version + "/YouvakendraSM.zip";
                logInfo("Download URL: " + urlString);
                logInfo("Download start for version: " + version);

                URL url = new URL(urlString);
                connection = openConnectionWithRedirects(url);
                int responseCode = connection.getResponseCode();

                // Redirect fallbacks for v-prefix structures
                if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    logInfo("Release with 'v' prefix returned 404, trying without 'v' prefix...");
                    urlString = "https://github.com/" + Constants.GITHUB_OWNER + "/" + Constants.GITHUB_REPO + "/releases/download/" + version + "/YouvakendraSM.zip";
                    logInfo("Download URL: " + urlString);
                    url = new URL(urlString);
                    connection = openConnectionWithRedirects(url);
                    responseCode = connection.getResponseCode();
                }

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    String err = "Server returned HTTP response code: " + responseCode + " for update ZIP.";
                    logError(err, null);
                    callback.onError(err);
                    return;
                }

                long totalBytes = connection.getContentLengthLong();
                inputStream = connection.getInputStream();

                File tempZip = new File(Constants.UPDATE_ZIP_FILE);
                logInfo("Saving update to downloaded file path: " + tempZip.getAbsolutePath());
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
                        callback.onProgress(-1, bytesDownloaded, -1);
                    }
                }

                outputStream.close();
                inputStream.close();

                logInfo("Download complete. File size: " + tempZip.length() + " bytes. ZIP saved to: " + tempZip.getAbsolutePath());
                callback.onComplete();

            } catch (Exception e) {
                logError("Download error: " + e.getMessage(), e);
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
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);
        conn.setRequestProperty("User-Agent", "YouvakendraSM-Updater");
        conn.setInstanceFollowRedirects(true);

        int status = conn.getResponseCode();
        int redirectCount = 0;

        while ((status == HttpURLConnection.HTTP_MOVED_TEMP || 
                status == HttpURLConnection.HTTP_MOVED_PERM || 
                status == 307 || status == 308) && redirectCount < 5) {

            String newUrl = conn.getHeaderField("Location");
            conn.disconnect();

            logInfo("Redirected to: " + newUrl);
            url = new URL(newUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setRequestProperty("User-Agent", "YouvakendraSM-Updater");
            conn.setInstanceFollowRedirects(true);

            status = conn.getResponseCode();
            redirectCount++;
        }
        return conn;
    }

    /**
     * Extracts the downloaded update ZIP file, validating its integrity first.
     */
    public static boolean extractUpdateZip(String latestVersion) {
        lastExceptionMessage = "";
        lastFailedFilePath = "";

        File zipFile = new File(Constants.UPDATE_ZIP_FILE);
        logInfo("Extraction start from: " + zipFile.getAbsolutePath());

        // 1. Verify file existence
        if (!zipFile.exists() || !zipFile.isFile()) {
            String err = "Downloaded file does not exist or is not a file.";
            lastExceptionMessage = err;
            lastFailedFilePath = zipFile.getAbsolutePath();
            logError(err, null);
            return false;
        }

        // 2. Validate structural integrity & expected contents
        logInfo("ZIP validation: checking structural integrity of download...");
        try (ZipFile testZip = new ZipFile(zipFile)) {
            boolean containsJar = false;
            Enumeration<? extends ZipEntry> entries = testZip.entries();
            int entryCount = 0;
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                entryCount++;
                if (entry.getName().endsWith("YouvakendraSM.jar")) {
                    containsJar = true;
                }
            }
            if (entryCount == 0) {
                throw new IOException("The ZIP archive is empty (0 entries).");
            }
            if (!containsJar) {
                throw new IOException("Expected payload file 'YouvakendraSM.jar' was not found inside the update package.");
            }
            logInfo("ZIP validation passed. Contains " + entryCount + " entries.");
        } catch (Exception e) {
            String err = "ZIP validation failed: " + e.getMessage();
            lastExceptionMessage = err;
            lastFailedFilePath = zipFile.getAbsolutePath();
            logError(err, e);
            return false;
        }

        // 3. Display diagnostic metadata details
        logInfo("==================================================");
        logInfo("Downloaded File Path: " + zipFile.getAbsolutePath());
        logInfo("File Size: " + zipFile.length() + " bytes");
        logInfo("ZIP Contents Preview:");
        try (ZipFile inspectZip = new ZipFile(zipFile)) {
            Enumeration<? extends ZipEntry> entries = inspectZip.entries();
            int previewCount = 0;
            while (entries.hasMoreElements() && previewCount < 50) {
                ZipEntry entry = entries.nextElement();
                logInfo("  [ZIP ENTRY] " + entry.getName() + " (" + entry.getSize() + " bytes)");
                previewCount++;
            }
            if (entries.hasMoreElements()) {
                logInfo("  [ZIP ENTRY] ... and additional entries");
            }
        } catch (IOException e) {
            logError("Failed to list ZIP contents: " + e.getMessage(), e);
        }
        logInfo("==================================================");

        // 4. Perform extraction with Java ZipInputStream
        File destDir = new File("update_temp");
        logInfo("Verify extraction target folder: " + destDir.getAbsolutePath());
        if (destDir.exists()) {
            logInfo("Clearing existing update_temp directory...");
            deleteDirectory(destDir);
        }
        if (!destDir.mkdirs()) {
            String err = "Failed to create target extraction directory 'update_temp'.";
            lastExceptionMessage = err;
            lastFailedFilePath = destDir.getAbsolutePath();
            logError(err, null);
            return false;
        }
        if (!destDir.canWrite()) {
            String err = "Target extraction folder 'update_temp' is not writable (Permission Denied).";
            lastExceptionMessage = err;
            lastFailedFilePath = destDir.getAbsolutePath();
            logError(err, null);
            return false;
        }

        byte[] buffer = new byte[8192];
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            String commonPrefix = null;

            while ((entry = zipIn.getNextEntry()) != null) {
                String entryName = entry.getName();

                // Establish the prefix on the first entry (strip top-level directories if present)
                if (commonPrefix == null) {
                    int firstSlashIndex = entryName.indexOf('/');
                    if (firstSlashIndex != -1) {
                        String firstDir = entryName.substring(0, firstSlashIndex);
                        if (firstDir.equalsIgnoreCase(Constants.GITHUB_REPO + "-" + latestVersion) || 
                            firstDir.equalsIgnoreCase(Constants.GITHUB_REPO + "-v" + latestVersion) || 
                            firstDir.contains("-")) {
                            commonPrefix = firstDir + "/";
                            logInfo("Detected top-level folder wrapper prefix: " + commonPrefix);
                        } else {
                            commonPrefix = "";
                        }
                    } else {
                        commonPrefix = "";
                    }
                }

                String relativePath = entryName;
                if (!commonPrefix.isEmpty() && entryName.startsWith(commonPrefix)) {
                    relativePath = entryName.substring(commonPrefix.length());
                }

                if (relativePath.isEmpty()) {
                    zipIn.closeEntry();
                    continue;
                }

                File entryFile = new File(destDir, relativePath);
                lastFailedFilePath = entryFile.getAbsolutePath();

                if (entry.isDirectory()) {
                    if (!entryFile.exists() && !entryFile.mkdirs()) {
                        throw new IOException("Failed to create folder: " + entryFile.getAbsolutePath());
                    }
                } else {
                    File parent = entryFile.getParentFile();
                    if (parent != null && !parent.exists()) {
                        if (!parent.mkdirs()) {
                            throw new IOException("Failed to create parent folder path: " + parent.getAbsolutePath());
                        }
                    }

                    logInfo("Extracting file entry: " + relativePath + " (" + entry.getSize() + " bytes)");
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(entryFile))) {
                        int read;
                        while ((read = zipIn.read(buffer)) != -1) {
                            bos.write(buffer, 0, read);
                        }
                    } catch (FileNotFoundException fnfe) {
                        throw new IOException("Permission Denied or File Locked: " + entryFile.getName(), fnfe);
                    }
                }
                zipIn.closeEntry();
            }

            logInfo("Extraction complete. Successfully extracted ZIP into: " + destDir.getAbsolutePath());
            return true;
        } catch (Exception e) {
            String err = "Extraction failed: " + e.getMessage();
            lastExceptionMessage = e.getMessage();
            logError(err, e);
            return false;
        }
    }

    /**
     * Launches the batch script to finalize the update and exits the application.
     */
    public static void installAndRestart() {
        logInfo("File replacement start: creating update_installer.bat");
        File batchFile = new File(Constants.UPDATE_INSTALLER_BAT);

        try (PrintWriter writer = new PrintWriter(new FileWriter(batchFile))) {
            writer.println("@echo off");
            writer.println("echo ========================================================");
            writer.println("echo   YouvakendraSM - Installing Updates");
            writer.println("echo ========================================================");
            writer.println("echo Waiting for the main application to close...");
            writer.println("timeout /t 1 /nobreak > nul");

            writer.println("set retryCount=0");
            writer.println(":retry_copy");
            writer.println("xcopy /e /y /q \"update_temp\\*\" \".\" > nul 2>nul");
            writer.println("if %errorlevel% neq 0 (");
            writer.println("    set /a retryCount=%retryCount%+1");
            writer.println("    if %retryCount% geq 20 (");
            writer.println("        echo [ERROR] Failed to replace files after 20 retries.");
            writer.println("        echo Please close any instances of YouvakendraSM and try again.");
            writer.println("        pause");
            writer.println("        exit");
            writer.println("    )");
            writer.println("    echo Files are locked by running application. Retrying (%retryCount%/20) in 1 second...");
            writer.println("    timeout /t 1 /nobreak > nul");
            writer.println("    goto retry_copy");
            writer.println(")");

            writer.println("echo Cleaning up update temporary files...");
            writer.println("rmdir /s /q \"update_temp\" > nul");
            writer.println("if exist \"" + Constants.UPDATE_ZIP_FILE + "\" del /f /q \"" + Constants.UPDATE_ZIP_FILE + "\" > nul");

            writer.println("echo Restarting the application...");
            writer.println("if exist \"YouvakendraSM.exe\" (");
            writer.println("    start \"\" \"YouvakendraSM.exe\"");
            writer.println(") else (");
            writer.println("    start \"\" \"" + Constants.RUN_BAT + "\"");
            writer.println(")");

            writer.println("echo Update complete!");
            writer.println("del /f /q \"%~f0\" & exit");

        } catch (IOException e) {
            logError("Failed to create installer batch script: " + e.getMessage(), e);
            return;
        }

        // Execute batch file
        try {
            logInfo("Restart process: Launching update_installer.bat and exiting JVM...");
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", batchFile.getAbsolutePath());
            pb.directory(new File("."));
            pb.start();

            // Terminate current process to release all file locks
            System.exit(0);
        } catch (IOException e) {
            logError("Failed to launch installer batch script: " + e.getMessage(), e);
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
