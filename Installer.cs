using System;
using System.IO;
using System.Reflection;
using System.Windows.Forms;
using System.Drawing;
using System.IO.Compression;

public class Program {
    [STAThread]
    public static void Main() {
        Application.EnableVisualStyles();
        Application.SetCompatibleTextRenderingDefault(false);
        Application.Run(new InstallerForm());
    }
}

public class InstallerForm : Form {
    private Label titleLabel;
    private Label pathLabel;
    private TextBox pathTextBox;
    private Button browseButton;
    private Button installButton;
    private ProgressBar progressBar;
    private Label statusLabel;
    private CheckBox shortcutCheckBox;

    // Modern Light theme palette matching YouvakendraSM styling
    private static readonly Color COLOR_BG_APP = Color.FromArgb(0xF8, 0xFA, 0xFC);
    private static readonly Color COLOR_BG_CARD = Color.FromArgb(0xFF, 0xFF, 0xFF);
    private static readonly Color COLOR_PRIMARY = Color.FromArgb(0x25, 0x63, 0xEB);
    private static readonly Color COLOR_TEXT_MAIN = Color.FromArgb(0x0F, 0x17, 0x2A);
    private static readonly Color COLOR_TEXT_MUTED = Color.FromArgb(0x64, 0x74, 0x8B);

    public InstallerForm() {
        this.Text = "YouvakendraSM Installer";
        this.Size = new Size(500, 320);
        this.StartPosition = FormStartPosition.CenterScreen;
        this.FormBorderStyle = FormBorderStyle.FixedDialog;
        this.MaximizeBox = false;
        this.BackColor = COLOR_BG_APP;
        this.ForeColor = COLOR_TEXT_MAIN;

        // Custom Form Icon
        try {
            if (File.Exists("assets\\logo.ico")) {
                this.Icon = new Icon("assets\\logo.ico");
            }
        } catch {}

        titleLabel = new Label() {
            Text = "YouvakendraSM Setup",
            Location = new Point(24, 24),
            Size = new Size(440, 32),
            Font = new Font("Segoe UI", 16, FontStyle.Bold),
            ForeColor = COLOR_TEXT_MAIN
        };

        pathLabel = new Label() {
            Text = "Select Installation Folder:",
            Location = new Point(24, 76),
            Size = new Size(440, 20),
            Font = new Font("Segoe UI", 10, FontStyle.Regular),
            ForeColor = COLOR_TEXT_MUTED
        };

        string defaultPath = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "YouvakendraSM");
        pathTextBox = new TextBox() {
            Text = defaultPath,
            Location = new Point(24, 102),
            Size = new Size(346, 25),
            Font = new Font("Segoe UI", 10, FontStyle.Regular),
            BackColor = COLOR_BG_CARD,
            ForeColor = COLOR_TEXT_MAIN,
            BorderStyle = BorderStyle.FixedSingle
        };

        browseButton = new Button() {
            Text = "Browse...",
            Location = new Point(382, 100),
            Size = new Size(82, 27),
            Font = new Font("Segoe UI", 9, FontStyle.Regular),
            FlatStyle = FlatStyle.Flat,
            BackColor = Color.FromArgb(0xE2, 0xE8, 0xF0),
            ForeColor = COLOR_TEXT_MAIN
        };
        browseButton.FlatAppearance.BorderSize = 0;
        browseButton.Click += BrowseClick;

        shortcutCheckBox = new CheckBox() {
            Text = "Create Desktop Shortcut",
            Checked = true,
            Location = new Point(24, 142),
            Size = new Size(440, 20),
            Font = new Font("Segoe UI", 9, FontStyle.Regular),
            ForeColor = COLOR_TEXT_MUTED
        };

        progressBar = new ProgressBar() {
            Location = new Point(24, 180),
            Size = new Size(440, 16),
            Style = ProgressBarStyle.Blocks,
            Visible = false
        };

        statusLabel = new Label() {
            Text = "",
            Location = new Point(24, 202),
            Size = new Size(440, 20),
            Font = new Font("Segoe UI", 9, FontStyle.Italic),
            ForeColor = COLOR_TEXT_MUTED,
            Visible = false
        };

        installButton = new Button() {
            Text = "Install",
            Location = new Point(364, 230),
            Size = new Size(100, 36),
            Font = new Font("Segoe UI", 10, FontStyle.Bold),
            FlatStyle = FlatStyle.Flat,
            BackColor = COLOR_PRIMARY,
            ForeColor = Color.White
        };
        installButton.FlatAppearance.BorderSize = 0;
        installButton.Click += InstallClick;

        this.Controls.Add(titleLabel);
        this.Controls.Add(pathLabel);
        this.Controls.Add(pathTextBox);
        this.Controls.Add(browseButton);
        this.Controls.Add(shortcutCheckBox);
        this.Controls.Add(progressBar);
        this.Controls.Add(statusLabel);
        this.Controls.Add(installButton);
    }

    private void BrowseClick(object sender, EventArgs e) {
        using (FolderBrowserDialog fbd = new FolderBrowserDialog()) {
            fbd.SelectedPath = pathTextBox.Text;
            if (fbd.ShowDialog() == DialogResult.OK) {
                pathTextBox.Text = fbd.SelectedPath;
            }
        }
    }

    private void InstallClick(object sender, EventArgs e) {
        string targetDir = pathTextBox.Text;
        if (string.IsNullOrEmpty(targetDir)) {
            MessageBox.Show("Please select a valid folder.", "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
            return;
        }

        installButton.Enabled = false;
        browseButton.Enabled = false;
        pathTextBox.Enabled = false;
        shortcutCheckBox.Enabled = false;

        progressBar.Visible = true;
        statusLabel.Visible = true;
        progressBar.Value = 10;
        statusLabel.Text = "Extracting files...";

        System.Threading.ThreadPool.QueueUserWorkItem((state) => {
            try {
                if (!Directory.Exists(targetDir)) {
                    Directory.CreateDirectory(targetDir);
                }

                // Extract embedded zip resource
                Assembly assembly = Assembly.GetExecutingAssembly();
                string tempZipPath = Path.Combine(Path.GetTempPath(), "YouvakendraSM_setup.zip");
                
                progressBar.Invoke((Action)(() => progressBar.Value = 30));

                using (Stream resourceStream = assembly.GetManifestResourceStream("YouvakendraSM.zip")) {
                    if (resourceStream == null) {
                        throw new Exception("Embedded installer payload not found.");
                    }
                    using (FileStream fileStream = new FileStream(tempZipPath, FileMode.Create)) {
                        resourceStream.CopyTo(fileStream);
                    }
                }

                progressBar.Invoke((Action)(() => progressBar.Value = 50));
                statusLabel.Invoke((Action)(() => statusLabel.Text = "Installing to folder..."));

                if (File.Exists(tempZipPath)) {
                    // Extracting ZIP contents
                    ZipFile.ExtractToDirectory(tempZipPath, targetDir);
                    File.Delete(tempZipPath);
                }

                progressBar.Invoke((Action)(() => progressBar.Value = 80));
                statusLabel.Invoke((Action)(() => statusLabel.Text = "Creating shortcuts..."));

                if (shortcutCheckBox.Checked) {
                    CreateShortcut(targetDir);
                }

                progressBar.Invoke((Action)(() => progressBar.Value = 100));
                statusLabel.Invoke((Action)(() => statusLabel.Text = "Installation completed successfully!"));

                MessageBox.Show("YouvakendraSM has been installed successfully!", "Success", MessageBoxButtons.OK, MessageBoxIcon.Information);

                // Launch the app
                string exePath = Path.Combine(targetDir, "YouvakendraSM.exe");
                if (File.Exists(exePath)) {
                    System.Diagnostics.Process.Start(new System.Diagnostics.ProcessStartInfo() {
                        FileName = exePath,
                        WorkingDirectory = targetDir
                    });
                }

                this.Invoke((Action)(() => this.Close()));

            } catch (Exception ex) {
                MessageBox.Show("Installation failed:\n" + ex.Message, "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                this.Invoke((Action)(() => {
                    installButton.Enabled = true;
                    browseButton.Enabled = true;
                    pathTextBox.Enabled = true;
                    shortcutCheckBox.Enabled = true;
                    progressBar.Visible = false;
                    statusLabel.Visible = false;
                }));
            }
        });
    }

    private void CreateShortcut(string targetDir) {
        try {
            string desktopPath = Environment.GetFolderPath(Environment.SpecialFolder.DesktopDirectory);
            string shortcutPath = Path.Combine(desktopPath, "YouvakendraSM.lnk");
            string targetExe = Path.Combine(targetDir, "YouvakendraSM.exe");

            Type shellType = Type.GetTypeFromProgID("WScript.Shell");
            object shell = Activator.CreateInstance(shellType);
            object shortcut = shellType.InvokeMember("CreateShortcut", BindingFlags.InvokeMethod, null, shell, new object[] { shortcutPath });
            
            Type shortcutType = shortcut.GetType();
            shortcutType.InvokeMember("TargetPath", BindingFlags.SetProperty, null, shortcut, new object[] { targetExe });
            shortcutType.InvokeMember("WorkingDirectory", BindingFlags.SetProperty, null, shortcut, new object[] { targetDir });
            shortcutType.InvokeMember("Description", BindingFlags.SetProperty, null, shortcut, new object[] { "YouvakendraSM Student Attendance Management System" });
            shortcutType.InvokeMember("Save", BindingFlags.InvokeMethod, null, shortcut, null);
        } catch (Exception ex) {
            Console.WriteLine("Shortcut creation error: " + ex.Message);
        }
    }
}
