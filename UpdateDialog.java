import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

public class UpdateDialog extends JDialog {

    private final String currentVersion;
    private final String latestVersion;

    private JPanel mainPanel;
    private JPanel infoPanel;
    private JPanel buttonPanel;
    private JPanel progressPanel;
    
    private JLabel statusLabel;
    private JProgressBar progressBar;
    private ModernButton btnUpdate;
    private ModernButton btnLater;

    // Palette colors matching styles.css
    private static final Color COLOR_BG_APP = new Color(0x12, 0x12, 0x12);
    private static final Color COLOR_BG_CARD = new Color(0x1C, 0x1C, 0x1E);
    private static final Color COLOR_TEXT_MAIN = new Color(0xF8, 0xFA, 0xFC);
    private static final Color COLOR_TEXT_MUTED = new Color(0x94, 0xA3, 0xB8);
    private static final Color COLOR_PRIMARY = new Color(0x3B, 0x82, 0xF6);
    private static final Color COLOR_PRIMARY_HOVER = new Color(0x60, 0xA5, 0xFA);
    private static final Color COLOR_PRIMARY_ACTIVE = new Color(0x1D, 0x4E, 0xD8);
    private static final Color COLOR_SECONDARY = new Color(0x2A, 0x2A, 0x2C);
    private static final Color COLOR_SECONDARY_HOVER = new Color(0x3E, 0x3E, 0x40);
    private static final Color COLOR_SECONDARY_ACTIVE = new Color(0x1C, 0x1C, 0x1E);
    private static final Color COLOR_SUCCESS = new Color(0x10, 0xB9, 0x81);
    private static final Color COLOR_DANGER = new Color(0xEF, 0x44, 0x44);

    public UpdateDialog(Frame owner, String currentVersion, String latestVersion) {
        super(owner, "Software Update", true);
        this.currentVersion = currentVersion;
        this.latestVersion = latestVersion;

        initializeUI();
    }

    public UpdateDialog(Dialog owner, String currentVersion, String latestVersion) {
        super(owner, "Software Update", true);
        this.currentVersion = currentVersion;
        this.latestVersion = latestVersion;

        initializeUI();
    }

    private void initializeUI() {
        setSize(450, 260);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(COLOR_BG_APP);
        setLayout(new BorderLayout());

        // Main padding panel
        mainPanel = new JPanel();
        mainPanel.setBackground(COLOR_BG_APP);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        mainPanel.setLayout(new BorderLayout(0, 16));
        add(mainPanel, BorderLayout.CENTER);

        // Header Panel (Title)
        JLabel titleLabel = new JLabel("New Update Available");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(COLOR_TEXT_MAIN);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Info Panel (Body text & Versions)
        infoPanel = new JPanel();
        infoPanel.setBackground(COLOR_BG_APP);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        JLabel descLabel = new JLabel("A new version of YouvakendraSM is available.");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(COLOR_TEXT_MUTED);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(descLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 14)));

        // Version card (Grid of versions)
        JPanel versionCard = new JPanel(new GridLayout(2, 2, 8, 8));
        versionCard.setBackground(COLOR_BG_CARD);
        versionCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xFF, 0xFF, 0xFF, 12), 1, true),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        versionCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblCurrTag = new JLabel("Current Version:");
        lblCurrTag.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCurrTag.setForeground(COLOR_TEXT_MUTED);
        versionCard.add(lblCurrTag);

        JLabel lblCurrVal = new JLabel(currentVersion);
        lblCurrVal.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblCurrVal.setForeground(COLOR_TEXT_MAIN);
        versionCard.add(lblCurrVal);

        JLabel lblNewTag = new JLabel("Latest Version:");
        lblNewTag.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNewTag.setForeground(COLOR_TEXT_MUTED);
        versionCard.add(lblNewTag);

        JLabel lblNewVal = new JLabel(latestVersion);
        lblNewVal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNewVal.setForeground(COLOR_SUCCESS); // Present Green
        versionCard.add(lblNewVal);

        infoPanel.add(versionCard);
        mainPanel.add(infoPanel, BorderLayout.CENTER);

        // Button Panel
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(COLOR_BG_APP);

        btnLater = new ModernButton("Later", COLOR_SECONDARY, COLOR_SECONDARY_HOVER, COLOR_SECONDARY_ACTIVE);
        btnLater.setPreferredSize(new Dimension(100, 36));
        btnLater.addActionListener(e -> dispose());
        buttonPanel.add(btnLater);

        btnUpdate = new ModernButton("Update Now", COLOR_PRIMARY, COLOR_PRIMARY_HOVER, COLOR_PRIMARY_ACTIVE);
        btnUpdate.setPreferredSize(new Dimension(120, 36));
        btnUpdate.addActionListener(e -> startUpdateProcess());
        buttonPanel.add(btnUpdate);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Progress Panel (Hidden initially)
        progressPanel = new JPanel();
        progressPanel.setBackground(COLOR_BG_APP);
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
        progressPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        statusLabel = new JLabel("Preparing download...");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(COLOR_TEXT_MUTED);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressPanel.add(statusLabel);
        progressPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        progressBar = new JProgressBar(0, 100);
        progressBar.setForeground(COLOR_PRIMARY);
        progressBar.setBackground(COLOR_BG_CARD);
        progressBar.setBorder(BorderFactory.createEmptyBorder());
        progressBar.setPreferredSize(new Dimension(380, 6));
        progressBar.setMaximumSize(new Dimension(400, 6));
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressPanel.add(progressBar);
    }

    private void startUpdateProcess() {
        // Switch from buttons to progress bar
        mainPanel.remove(buttonPanel);
        mainPanel.add(progressPanel, BorderLayout.SOUTH);
        mainPanel.revalidate();
        mainPanel.repaint();

        // Start async download
        AutoUpdater.downloadUpdateAsync(latestVersion, new AutoUpdater.ProgressCallback() {
            @Override
            public void onProgress(int percent, long bytesDownloaded, long totalBytes) {
                SwingUtilities.invokeLater(() -> {
                    if (percent >= 0) {
                        progressBar.setValue(percent);
                        statusLabel.setText(String.format("Downloading update: %d%% (%.2f MB / %.2f MB)", 
                                percent, bytesDownloaded / (1024.0 * 1024.0), totalBytes / (1024.0 * 1024.0)));
                    } else {
                        // Indeterminate size
                        progressBar.setIndeterminate(true);
                        statusLabel.setText(String.format("Downloading update: %.2f MB", bytesDownloaded / (1024.0 * 1024.0)));
                    }
                });
            }

            @Override
            public void onComplete() {
                SwingUtilities.invokeLater(() -> {
                    progressBar.setIndeterminate(true);
                    statusLabel.setText("Extraction and preparing installation...");
                });

                // Extract files in a background thread to keep UI smooth
                new Thread(() -> {
                    boolean success = AutoUpdater.extractUpdateZip(latestVersion);
                    
                    SwingUtilities.invokeLater(() -> {
                        if (success) {
                            statusLabel.setForeground(COLOR_SUCCESS);
                            statusLabel.setText("Installation ready. Restarting application...");
                            
                            // Let the user see the completion message for a brief moment
                            Timer timer = new Timer(1000, e -> {
                                dispose();
                                AutoUpdater.installAndRestart();
                            });
                            timer.setRepeats(false);
                            timer.start();
                        } else {
                            showError("Extraction failed. Please try again later.");
                        }
                    });
                }).start();
            }

            @Override
            public void onError(String message) {
                SwingUtilities.invokeLater(() -> showError(message));
            }
        });
    }

    private void showError(String message) {
        statusLabel.setForeground(COLOR_DANGER);
        statusLabel.setText("Error: " + message);
        progressBar.setIndeterminate(false);
        progressBar.setValue(0);
        progressBar.setForeground(COLOR_DANGER);

        // Put a Close button back in progress panel
        JPanel errorActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        errorActionPanel.setBackground(COLOR_BG_APP);
        errorActionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        ModernButton btnClose = new ModernButton("Close", COLOR_SECONDARY, COLOR_SECONDARY_HOVER, COLOR_SECONDARY_ACTIVE);
        btnClose.setPreferredSize(new Dimension(100, 32));
        btnClose.addActionListener(e -> dispose());
        errorActionPanel.add(btnClose);

        progressPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        progressPanel.add(errorActionPanel);
        progressPanel.revalidate();
        progressPanel.repaint();
    }

    /**
     * Modern UI Button with Custom Flat Styling and Smooth Colors
     */
    private static class ModernButton extends JButton {
        private final Color normalColor;
        private final Color hoverColor;
        private final Color activeColor;

        public ModernButton(String text, Color normal, Color hover, Color active) {
            super(text);
            this.normalColor = normal;
            this.hoverColor = hover;
            this.activeColor = active;
            
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(COLOR_TEXT_MAIN);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Set padding
            setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

            // Hover effects
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (getModel().isPressed()) {
                g2.setColor(activeColor);
            } else if (getModel().isRollover()) {
                g2.setColor(hoverColor);
            } else {
                g2.setColor(normalColor);
            }
            
            // Draw a rounded rectangle background
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.dispose();
            
            super.paintComponent(g);
        }
    }
}
