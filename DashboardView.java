import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;
import javafx.concurrent.Task;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DashboardView extends BorderPane {

    // DateTime Formatter for the dates (e.g., 01-Jun-2026)
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);

    // List of students in memory
    private final ObservableList<Student> studentsList = FXCollections.observableArrayList();
    private final FilteredList<Student> filteredStudents = new FilteredList<>(this.studentsList, p -> true);

    // Keep track of the next student ID number
    private int nextStudentIdNum = 2;

    // Active role in system
    private String currentRole = "Admin";
    private LoggedInUser loggedInUser;

    // Attendance state: present student IDs
    private final List<String> presentStudentIds = new ArrayList<>();

    // Sidebar fields
    private VBox sidebar;
    private Label lblLogo;
    private Button btnToggleSidebar;
    private final List<Button> menuButtons = new ArrayList<>();

    // Header fields
    private TextField txtSearch;
    private ComboBox<String> cbRoleSelector;
    private Label lblHeaderRoleBadge;
    private Label lblHeaderUserName;
    private Text txtAvatarChar;

    // Content container
    private StackPane mainContentArea;

    // Panels
    private ScrollPane dashboardPanel;
    private VBox studentsPanel;
    private ScrollPane attendancePanel;
    private ScrollPane coursesPanel;
    private ScrollPane batchesPanel;
    private ScrollPane companiesPanel;
    private ScrollPane placementsPanel;
    private ScrollPane reportsPanel;
    private ScrollPane usersPanel;
    private ScrollPane settingsPanel;

    // Courses dynamic table data
    private TableView<Course> coursesTable;
    private final ObservableList<Course> coursesList = FXCollections.observableArrayList();
    private VBox loadingOverlayCourses;

    // Batches dynamic table data
    private TableView<Batch> batchesTable;
    private final ObservableList<Batch> batchesList = FXCollections.observableArrayList();
    private VBox loadingOverlayBatches;

    // Companies dynamic table data
    private TableView<Company> companiesTable;
    private final ObservableList<Company> companiesList = FXCollections.observableArrayList();
    private VBox loadingOverlayCompanies;

    // Placements dynamic table data
    private TableView<StudentPlacement> placementsTable;
    private final ObservableList<StudentPlacement> placementsList = FXCollections.observableArrayList();
    private VBox loadingOverlayPlacements;

    // Metrics labels
    private Label lblTotalStudentsVal;
    private Label lblPresentVal;
    private Label lblAbsentVal;
    private Label lblReadyVal;

    // Student Form Fields
    private TextField txtId;
    private TextField txtName;
    private TextField txtFatherName;
    private TextField txtMotherName;
    private TextField txtMobile;
    private TextField txtAltMobile;
    private TextField txtEmail;
    private TextField txtAddress;
    private ComboBox<String> cbCourse;
    private ComboBox<String> cbBatch;
    private DatePicker dpAdmissionDate;
    private Label lblCompletionDate;
    private ComboBox<String> cbStatus;

    // Form buttons
    private Button btnAdd;
    private Button btnUpdate;
    private Button btnDelete;
    private Button btnClear;
    private Label lblFormMessage;
    private ComboBox<Student> cbEditorStudentSelect;

    // Student TableView
    private TableView<Student> studentTable;

    // Recent Activity container
    private VBox activityListContainer;

    // Attendance Table & Controls
    private TableView<Attendance> attendanceTable;
    private final ObservableList<Attendance> attendanceList = FXCollections.observableArrayList();
    private VBox loadingOverlayAttendance;

    // Loading overlay and Form container
    private VBox loadingOverlay;
    private VBox formBox;

    public DashboardView() {
        this(new LoggedInUser(LoggedInUser.Role.ADMIN, "AD001", "Admin User", "", "Center 1", "Admin", ""));
    }

    public DashboardView(LoggedInUser user) {
        this.loggedInUser = user;
        if (user.getRole() == LoggedInUser.Role.ADMIN) {
            this.currentRole = "Admin";
        } else if (user.getRole() == LoggedInUser.Role.TRAINER) {
            this.currentRole = "Trainer";
        } else if (user.getRole() == LoggedInUser.Role.STUDENT) {
            this.currentRole = "Student";
        } else {
            this.currentRole = "Viewer";
        }

        // Initialize dummy student data
        initializeDummyData();

        // Build UI structure
        setLeft(buildSidebar());
        setTop(buildHeader());

        // Build content panels
        buildDashboardPanel();
        buildStudentsPanel();
        buildAttendancePanel();
        buildCoursesPanel();
        buildBatchesPanel();
        buildCompaniesPanel();
        buildPlacementsPanel();
        buildReportsPanel();
        buildUsersPanel();
        buildSettingsPanel();

        // Main content stack pane
        mainContentArea = new StackPane();
        mainContentArea.getChildren().addAll(
                dashboardPanel,
                studentsPanel,
                attendancePanel,
                coursesPanel,
                batchesPanel,
                companiesPanel,
                placementsPanel,
                reportsPanel,
                usersPanel,
                settingsPanel);

        setCenter(mainContentArea);

        // Set initial view to Dashboard
        showPanel(dashboardPanel);
        setActiveMenuButton(menuButtons.get(0));

        // Initial update of dashboard numbers
        updateDashboardMetrics();

        // Fetch data on startup to populate dashboard metrics
        fetchDataForDashboard();

        // Apply role permissions initially
        switchUserRole(currentRole);
    }

    private void initializeDummyData() {
        // Empty to prevent generating dummy data as requested
    }

    // --- SIDEBAR BUILDER ---
    private VBox buildSidebar() {
        sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(240);
        sidebar.setMinWidth(240);
        sidebar.setSpacing(5);

        // Sidebar Logo Box
        HBox logoBox = new HBox();
        logoBox.getStyleClass().add("sidebar-title-box");
        logoBox.setAlignment(Pos.CENTER_LEFT);

        lblLogo = new Label("YSM");
        lblLogo.getStyleClass().add("sidebar-logo");
        lblLogo.setWrapText(false);
        HBox.setHgrow(lblLogo, Priority.ALWAYS);

        btnToggleSidebar = new Button("◀");
        btnToggleSidebar.getStyleClass().add("sidebar-toggle");
        btnToggleSidebar.setOnAction(e -> toggleSidebar());

        logoBox.getChildren().addAll(lblLogo, btnToggleSidebar);
        sidebar.getChildren().add(logoBox);

        // Navigation menu buttons
        String[][] menuItems = {
                { "Dashboard",
                        "M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" },
                { "Students", "M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2 M12 11a4 4 0 100-8 4 4 0 000 8z" },
                { "Attendance",
                        "M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" },
                { "Courses",
                        "M4 19.5A2.5 2.5 0 016.5 17H20M4 19.5A2.5 2.5 0 006.5 22H20M4 19.5V5A2.5 2.5 0 016.5 2.5H20v20H6.5" },
                { "Batches",
                        "M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" },
                { "Companies",
                        "M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m-1 4h1" },
                { "Placements",
                        "M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2 2v2m4 6h.01M5 20h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" },
                { "Reports", "M18 20V10m-6 10V4M6 20v-6" },
                { "Users",
                        "M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2 M23 21v-2a4 4 0 00-3-3.87 M16 3.13a4 4 0 010 7.75" },
                { "Settings",
                        "M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z M15 12a3 3 0 11-6 0 3 3 0 016 0z" }
        };

        for (String[] item : menuItems) {
            Button btn = new Button(item[0]);
            btn.getStyleClass().add("sidebar-button");

            // Create a thin-line minimalist SVG Icon
            javafx.scene.shape.SVGPath svgIcon = new javafx.scene.shape.SVGPath();
            svgIcon.setContent(item[1]);
            svgIcon.getStyleClass().add("sidebar-icon");

            btn.setGraphic(svgIcon);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setAlignment(Pos.CENTER_LEFT);
            btn.setGraphicTextGap(15);
            btn.setOnAction(e -> {
                setActiveMenuButton(btn);
                switch (item[0]) {
                    case "Dashboard":
                        showPanel(dashboardPanel);
                        break;
                    case "Students":
                        showPanel(studentsPanel);
                        fetchStudentsFromGoogleSheets();
                        break;
                    case "Attendance":
                        showPanel(attendancePanel);
                        refreshAttendanceData();
                        break;
                    case "Courses":
                        showPanel(coursesPanel);
                        refreshCoursesData();
                        break;
                    case "Batches":
                        showPanel(batchesPanel);
                        refreshBatchesData();
                        break;
                    case "Companies":
                        showPanel(companiesPanel);
                        refreshCompaniesData();
                        break;
                    case "Placements":
                        showPanel(placementsPanel);
                        refreshPlacementsData();
                        break;
                    case "Reports":
                        showPanel(reportsPanel);
                        break;
                    case "Users":
                        showPanel(usersPanel);
                        break;
                    case "Settings":
                        showPanel(settingsPanel);
                        break;
                }
            });
            menuButtons.add(btn);
            sidebar.getChildren().add(btn);
        }

        return sidebar;
    }

    private void toggleSidebar() {
        boolean collapsed = sidebar.getPrefWidth() < 100;
        if (collapsed) {
            sidebar.setPrefWidth(240);
            sidebar.setMinWidth(240);
            lblLogo.setText("YSM");
            btnToggleSidebar.setText("◀");
            for (Button btn : menuButtons) {
                btn.setContentDisplay(ContentDisplay.LEFT);
            }
        } else {
            sidebar.setPrefWidth(70);
            sidebar.setMinWidth(70);
            lblLogo.setText("YSM");
            btnToggleSidebar.setText("▶");
            for (Button btn : menuButtons) {
                btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }
        }
    }

    private void setActiveMenuButton(Button activeBtn) {
        for (Button btn : menuButtons) {
            btn.getStyleClass().remove("sidebar-button-active");
        }
        activeBtn.getStyleClass().add("sidebar-button-active");
    }

    private void showPanel(Parent panel) {
        dashboardPanel.setVisible(false);
        studentsPanel.setVisible(false);
        attendancePanel.setVisible(false);
        coursesPanel.setVisible(false);
        if (batchesPanel != null)
            batchesPanel.setVisible(false);
        if (companiesPanel != null)
            companiesPanel.setVisible(false);
        if (placementsPanel != null)
            placementsPanel.setVisible(false);
        reportsPanel.setVisible(false);
        usersPanel.setVisible(false);
        settingsPanel.setVisible(false);

        panel.setVisible(true);
    }

    // --- HEADER BUILDER ---
    private HBox buildHeader() {
        HBox header = new HBox();
        header.getStyleClass().add("header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);

        // Search box
        txtSearch = new TextField();
        txtSearch.setPromptText("Search by Name, ERP No, Batch ID, Batch Time...");
        txtSearch.getStyleClass().add("search-field");
        txtSearch.setPrefWidth(350);

        Button btnHeaderRefresh = new Button("🔄 Refresh All");
        btnHeaderRefresh.getStyleClass().addAll("btn", "btn-secondary");
        btnHeaderRefresh.setOnAction(e -> refreshAllData());

        // Dynamic search binding
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredStudents.setPredicate(student -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lower = newValue.toLowerCase();
                return (student.getErpNo() != null && student.getErpNo().toLowerCase().contains(lower))
                        || (student.getName() != null && student.getName().toLowerCase().contains(lower))
                        || (student.getBatchId() != null && student.getBatchId().toLowerCase().contains(lower))
                        || (student.getBatchTime() != null && student.getBatchTime().toLowerCase().contains(lower));
            });
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Notification Icon / Button
        Button btnNotification = new Button("🔔");
        btnNotification.getStyleClass().add("header-icon-btn");
        btnNotification.setStyle("-fx-font-size: 16px;");
        btnNotification.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Notifications");
            alert.setHeaderText("Recent Bulletins");
            alert.setContentText("- System started successfully.\n- Initialized backup local storage.\n- Current role: "
                    + currentRole);
            alert.showAndWait();
        });

        // Role Selector Dropdown
        Label lblRoleSelectText = new Label("View Mode:");
        lblRoleSelectText.getStyleClass().add("form-label");

        cbRoleSelector = new ComboBox<>();
        cbRoleSelector.getItems().addAll("Admin", "Trainer", "Viewer");
        cbRoleSelector.setValue("Admin");
        cbRoleSelector.setOnAction(e -> switchUserRole(cbRoleSelector.getValue()));

        // User Avatar Circle
        StackPane avatarPane = new StackPane();
        Circle avatarBg = new Circle(18);
        avatarBg.getStyleClass().add("avatar-circle");

        String initialChar = "A";
        if (loggedInUser != null && loggedInUser.getName() != null && !loggedInUser.getName().isEmpty()) {
            initialChar = loggedInUser.getName().substring(0, 1).toUpperCase();
        }
        txtAvatarChar = new Text(initialChar);
        txtAvatarChar.getStyleClass().add("avatar-text");
        avatarPane.getChildren().addAll(avatarBg, txtAvatarChar);

        if (loggedInUser != null && loggedInUser.getPictureUrl() != null && !loggedInUser.getPictureUrl().isEmpty()) {
            try {
                javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView();
                javafx.scene.image.Image img = new javafx.scene.image.Image(loggedInUser.getPictureUrl(), true);
                imgView.setImage(img);
                imgView.setFitWidth(36);
                imgView.setFitHeight(36);
                imgView.setPreserveRatio(true);
                Circle clip = new Circle(18, 18, 18);
                imgView.setClip(clip);

                img.progressProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal.doubleValue() == 1.0 && !img.isError()) {
                        txtAvatarChar.setVisible(false);
                    }
                });
                avatarPane.getChildren().add(imgView);
            } catch (Exception e) {
                System.out.println("Could not load header avatar: " + e.getMessage());
            }
        }

        // Profile Text
        VBox profileTxtBox = new VBox();
        profileTxtBox.setAlignment(Pos.CENTER_LEFT);
        lblHeaderUserName = new Label(loggedInUser != null ? loggedInUser.getName() : "Admin User");
        lblHeaderUserName.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        lblHeaderRoleBadge = new Label(currentRole);
        lblHeaderRoleBadge.getStyleClass().addAll("role-badge");

        profileTxtBox.getChildren().addAll(lblHeaderUserName, lblHeaderRoleBadge);

        // Clickable profile section
        HBox profileBox = new HBox(10, avatarPane, profileTxtBox);
        profileBox.setAlignment(Pos.CENTER_LEFT);
        profileBox.setCursor(javafx.scene.Cursor.HAND);
        profileBox.setOnMouseClicked(e -> showUserProfilePopup());

        // Hide role selector for non-admins
        boolean isAdminUser = loggedInUser != null && loggedInUser.getRole() == LoggedInUser.Role.ADMIN;
        header.getChildren().addAll(txtSearch, btnHeaderRefresh, spacer, profileBox);
        return header;
    }

    private void switchUserRole(String role) {
        this.currentRole = role;
        lblHeaderRoleBadge.getStyleClass().removeAll("role-badge", "role-badge-trainer", "role-badge-viewer");
        lblHeaderRoleBadge.getStyleClass().add("role-badge");

        String displayRole = role;
        String displayName = "Viewer Account";
        if (loggedInUser != null) {
            String loggedInRoleStr = loggedInUser.getRole() == LoggedInUser.Role.ADMIN ? "Admin"
                    : (loggedInUser.getRole() == LoggedInUser.Role.TRAINER ? "Trainer" 
                    : (loggedInUser.getRole() == LoggedInUser.Role.STUDENT ? "Student" : "Viewer"));
            if (loggedInRoleStr.equals(role)) {
                displayName = loggedInUser.getName();
            } else {
                displayName = role + " View";
            }
        } else {
            if ("Admin".equals(role))
                displayName = "Admin User";
            else if ("Trainer".equals(role))
                displayName = "Trainer Staff";
        }

        lblHeaderUserName.setText(displayName);

        if ("Admin".equals(role)) {
            lblHeaderRoleBadge.setText("Admin");
            txtAvatarChar.setText("A");
        } else if ("Trainer".equals(role)) {
            lblHeaderRoleBadge.getStyleClass().add("role-badge-trainer");
            lblHeaderRoleBadge.setText("Trainer");
            txtAvatarChar.setText("T");
        } else if ("Student".equals(role)) {
            lblHeaderRoleBadge.getStyleClass().add("role-badge-viewer");
            lblHeaderRoleBadge.setText("Student");
            txtAvatarChar.setText("S");
        } else {
            lblHeaderRoleBadge.getStyleClass().add("role-badge-viewer");
            lblHeaderRoleBadge.setText("Viewer");
            txtAvatarChar.setText("V");
        }

        applyRolePermissions(role);
        addActivity("👥", "Switched view mode to: " + role);
    }

    private void applyRolePermissions(String role) {
        boolean isAdmin = "Admin".equals(role);
        boolean isTrainer = "Trainer".equals(role);
        boolean isViewer = "Viewer".equals(role) || "Student".equals(role);

        // Apply to student form fields - Trainer and Admin can edit
        txtName.setDisable(isViewer);
        txtFatherName.setDisable(isViewer);
        txtMotherName.setDisable(isViewer);
        txtMobile.setDisable(isViewer);
        txtAltMobile.setDisable(isViewer);
        txtEmail.setDisable(isViewer);
        txtAddress.setDisable(isViewer);

        // Trainer and Admin can edit course/batch/status and date
        cbCourse.setDisable(isViewer);
        cbBatch.setDisable(isViewer);
        dpAdmissionDate.setDisable(isViewer);
        cbStatus.setDisable(isViewer);

        // Buttons configuration
        btnAdd.setDisable(!isAdmin && !isTrainer); // Admin and Trainer can add
        btnDelete.setDisable(!isAdmin && !isTrainer); // Admin and Trainer can delete
        btnUpdate.setDisable(isViewer); // Admin and Trainer can update
        btnClear.setDisable(isViewer);

        if (isAdmin) {
            lblFormMessage.setText("Full administrative write privileges active.");
            lblFormMessage.setStyle("-fx-text-fill: -success-color;");
        } else if (isTrainer) {
            lblFormMessage.setText("Trainer view: Allowed to update batch/status/attendance.");
            lblFormMessage.setStyle("-fx-text-fill: -primary-color;");
        } else {
            lblFormMessage.setText("Viewer view: Read-only access enabled.");
            lblFormMessage.setStyle("-fx-text-fill: -text-muted;");
        }
    }

    // --- PANEL 1: DASHBOARD ---
    private void buildDashboardPanel() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(24));
        container.getStyleClass().add("root");

        // Title
        Label lblTitle = new Label("System Dashboard");
        lblTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: -text-main;");

        // Summary Cards Container (Responsive Grid / HBox)
        HBox metricsGrid = new HBox(16);
        metricsGrid.setAlignment(Pos.CENTER);

        // Card 1: Total Students
        VBox cardTotal = createMetricCard("TOTAL STUDENTS", "120", "metric-card-primary");
        lblTotalStudentsVal = (Label) cardTotal.getChildren().get(1);

        // Card 2: Present Today
        VBox cardPresent = createMetricCard("PRESENT TODAY", "95", "metric-card-success");
        lblPresentVal = (Label) cardPresent.getChildren().get(1);

        // Card 3: Absent Today
        VBox cardAbsent = createMetricCard("ABSENT TODAY", "25", "metric-card-danger");
        lblAbsentVal = (Label) cardAbsent.getChildren().get(1);

        // Card 4: Ready For Exam
        VBox cardReady = createMetricCard("READY FOR EXAM", "18", "metric-card-warning");
        lblReadyVal = (Label) cardReady.getChildren().get(1);

        HBox.setHgrow(cardTotal, Priority.ALWAYS);
        HBox.setHgrow(cardPresent, Priority.ALWAYS);
        HBox.setHgrow(cardAbsent, Priority.ALWAYS);
        HBox.setHgrow(cardReady, Priority.ALWAYS);
        metricsGrid.getChildren().addAll(cardTotal, cardPresent, cardAbsent, cardReady);

        // Side-by-side grid below cards
        HBox gridBottom = new HBox(20);
        HBox.setHgrow(gridBottom, Priority.ALWAYS);
        VBox.setVgrow(gridBottom, Priority.ALWAYS);

        // Left: Recent Activities
        VBox recentActivitiesBox = new VBox(15);
        recentActivitiesBox.getStyleClass().add("content-card");
        HBox.setHgrow(recentActivitiesBox, Priority.ALWAYS);
        recentActivitiesBox.setPrefWidth(600);

        Label lblActTitle = new Label("Recent System Activity");
        lblActTitle.getStyleClass().add("card-title");

        activityListContainer = new VBox(10);
        activityListContainer.setPadding(new Insets(5, 0, 5, 0));

        // Initial Activities
        addActivityItem("🚀", "System initialized", "Real-time log active");

        recentActivitiesBox.getChildren().addAll(lblActTitle, activityListContainer);

        // Right: Information Panels
        VBox infoPanel = new VBox(20);
        infoPanel.setPrefWidth(400);

        // Info Card 1: Course Rules
        VBox courseRulesCard = new VBox(12);
        courseRulesCard.getStyleClass().add("content-card");
        Label lblRulesTitle = new Label("Course Configurations");
        lblRulesTitle.getStyleClass().add("card-title");

        GridPane gpRules = new GridPane();
        gpRules.setHgap(15);
        gpRules.setVgap(10);
        gpRules.setPadding(new Insets(5, 0, 5, 0));

        Label h1 = new Label("Course");
        Label h2 = new Label("Full Title");
        Label h3 = new Label("Duration");
        Label h4 = new Label("Action");

        gpRules.add(h1, 0, 0);
        gpRules.add(h2, 1, 0);
        gpRules.add(h3, 2, 0);
        gpRules.add(h4, 3, 0);

        h1.setStyle("-fx-font-weight: bold; -fx-text-fill: -text-main;");
        h2.setStyle("-fx-font-weight: bold; -fx-text-fill: -text-main;");
        h3.setStyle("-fx-font-weight: bold; -fx-text-fill: -text-main;");
        h4.setStyle("-fx-font-weight: bold; -fx-text-fill: -text-main;");

        // Row 1
        Label r1_c1 = new Label("DM");
        Label r1_c2 = new Label("Digital Management");
        Label r1_c3 = new Label("3 Months");
        r1_c1.setStyle("-fx-text-fill: -text-muted;");
        r1_c2.setStyle("-fx-text-fill: -text-muted;");
        r1_c3.setStyle("-fx-text-fill: -text-muted;");
        Button btnData1 = new Button("Data");
        btnData1.getStyleClass().addAll("btn", "btn-secondary");
        btnData1.setStyle("-fx-padding: 4 10; -fx-font-size: 11px;");

        gpRules.add(r1_c1, 0, 1);
        gpRules.add(r1_c2, 1, 1);
        gpRules.add(r1_c3, 2, 1);
        gpRules.add(btnData1, 3, 1);

        // Row 2
        Label r2_c1 = new Label("DTP");
        Label r2_c2 = new Label("Desk Top Publishing");
        Label r2_c3 = new Label("4 Months");
        r2_c1.setStyle("-fx-text-fill: -text-muted;");
        r2_c2.setStyle("-fx-text-fill: -text-muted;");
        r2_c3.setStyle("-fx-text-fill: -text-muted;");
        Button btnData2 = new Button("Data");
        btnData2.getStyleClass().addAll("btn", "btn-secondary");
        btnData2.setStyle("-fx-padding: 4 10; -fx-font-size: 11px;");

        gpRules.add(r2_c1, 0, 2);
        gpRules.add(r2_c2, 1, 2);
        gpRules.add(r2_c3, 2, 2);
        gpRules.add(btnData2, 3, 2);

        courseRulesCard.getChildren().addAll(lblRulesTitle, gpRules);

        // Info Card 2: Role Summary
        VBox roleSummaryCard = new VBox(12);
        roleSummaryCard.getStyleClass().add("content-card");
        Label lblRolesTitle = new Label("User Permissions Directory");
        lblRolesTitle.getStyleClass().add("card-title");

        VBox vbRoles = new VBox(10);
        vbRoles.getChildren().addAll(
                createRoleDetailLine("Admin", "Full read, write, update and delete capabilities."),
                createRoleDetailLine("Trainer", "Modify attendance records and update student status."),
                createRoleDetailLine("Viewer", "Read-only access across all directories."));
        roleSummaryCard.getChildren().addAll(lblRolesTitle, vbRoles);

        infoPanel.getChildren().addAll(courseRulesCard, roleSummaryCard);
        gridBottom.getChildren().addAll(recentActivitiesBox, infoPanel);

        container.getChildren().addAll(lblTitle, metricsGrid, gridBottom);

        dashboardPanel = new ScrollPane(container);
        dashboardPanel.setFitToWidth(true);
        dashboardPanel.setFitToHeight(true);
        dashboardPanel.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
    }

    private VBox createMetricCard(String title, String value, String borderStyleClass) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("metric-card", borderStyleClass);

        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("metric-title");

        Label lblVal = new Label(value);
        lblVal.getStyleClass().add("metric-value");

        Label lblSubtext = new Label("Updated in real-time");
        lblSubtext.setStyle("-fx-font-size: 11px; -fx-text-fill: -text-muted;");

        card.getChildren().addAll(lblTitle, lblVal, lblSubtext);
        return card;
    }

    private HBox createRoleDetailLine(String role, String desc) {
        HBox box = new HBox(12);
        Label badge = new Label(role);
        badge.getStyleClass().add("role-badge");
        if ("Trainer".equals(role)) {
            badge.getStyleClass().add("role-badge-trainer");
        } else if ("Viewer".equals(role)) {
            badge.getStyleClass().add("role-badge-viewer");
        }

        Label labelDesc = new Label(desc);
        labelDesc.setWrapText(true);
        labelDesc.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-muted;");

        box.getChildren().addAll(badge, labelDesc);
        return box;
    }

    private void addActivity(String icon, String text) {
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();
        String timeStr = date.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) + " "
                + time.format(DateTimeFormatter.ofPattern("hh:mm a"));

        // Add to dashboard activities feed
        addActivityItem(icon, text, timeStr);
    }

    private void addActivityItem(String icon, String text, String timeStr) {
        HBox item = new HBox(12);
        item.getStyleClass().add("activity-item");
        item.setAlignment(Pos.CENTER_LEFT);

        Label lblIcon = new Label(icon);
        lblIcon.getStyleClass().add("activity-icon");

        VBox vbContent = new VBox(2);
        Label lblText = new Label(text);
        lblText.getStyleClass().add("activity-text");
        lblText.setStyle("-fx-font-weight: 500;");

        Label lblTime = new Label(timeStr);
        lblTime.getStyleClass().add("activity-time");

        vbContent.getChildren().addAll(lblText, lblTime);
        item.getChildren().addAll(lblIcon, vbContent);

        if (activityListContainer != null) {
            activityListContainer.getChildren().add(0, item);
            if (activityListContainer.getChildren().size() > 7) {
                activityListContainer.getChildren().remove(7);
            }
        }
    }

    private void updateDashboardMetrics() {
        // Total students
        int total = studentsList.size();
        if (lblTotalStudentsVal != null) {
            lblTotalStudentsVal.setText(String.valueOf(total));
        }

        // Present & Absent counts from today's attendance data
        String todayStrFormatted = LocalDate.now().format(DATE_FORMATTER); // e.g. "20-Jun-2026"
        String todayStrISO = LocalDate.now().toString(); // e.g. "2026-06-20"

        long presentCount = attendanceList.stream()
                .filter(a -> (todayStrFormatted.equalsIgnoreCase(a.getDate()) || todayStrISO.equals(a.getDate()))
                        && "Present".equalsIgnoreCase(a.getStatus()))
                .count();
        long absentCount = attendanceList.stream()
                .filter(a -> (todayStrFormatted.equalsIgnoreCase(a.getDate()) || todayStrISO.equals(a.getDate()))
                        && "Absent".equalsIgnoreCase(a.getStatus()))
                .count();

        // Fallback to latest date in the attendance sheet if no records for today exist
        if (presentCount == 0 && absentCount == 0 && !attendanceList.isEmpty()) {
            // Find the most recent date in the attendance list
            String latestDate = attendanceList.get(attendanceList.size() - 1).getDate();
            presentCount = attendanceList.stream()
                    .filter(a -> latestDate.equals(a.getDate()) && "Present".equalsIgnoreCase(a.getStatus()))
                    .count();
            absentCount = attendanceList.stream()
                    .filter(a -> latestDate.equals(a.getDate()) && "Absent".equalsIgnoreCase(a.getStatus()))
                    .count();
        }

        if (lblPresentVal != null) {
            lblPresentVal.setText(String.valueOf(presentCount));
        }
        if (lblAbsentVal != null) {
            lblAbsentVal.setText(String.valueOf(absentCount));
        }

        // Ready for Exam counts
        long readyTotal = studentsList.stream().filter(s -> "Ready For Exam".equalsIgnoreCase(s.getStatus())).count();

        if (lblReadyVal != null) {
            lblReadyVal.setText(String.valueOf(readyTotal));
        }
    }

    private void fetchDataForDashboard() {
        javafx.concurrent.Task<Void> loadTask = new javafx.concurrent.Task<>() {
            @Override
            protected Void call() throws Exception {
                GoogleSheetsService service = new GoogleSheetsService();
                List<Course> loadedCourses = service.readCourses();
                List<Student> loadedStudents = service.readStudents();
                List<Attendance> loadedAttendance = service.getAttendanceRecords();

                javafx.application.Platform.runLater(() -> {
                    coursesList.clear();
                    coursesList.addAll(loadedCourses);

                    String trainerCourse = getTrainerCourseId();
                    if (loggedInUser != null && loggedInUser.getRole() == LoggedInUser.Role.STUDENT) {
                        loadedStudents.removeIf(s -> !loggedInUser.getId().equalsIgnoreCase(s.getId()));
                        
                        // Filter courses list so student only sees their own course
                        coursesList.removeIf(c -> {
                            String scId = loggedInUser.getCourseId();
                            if (scId != null && !scId.trim().isEmpty()) {
                                return !scId.equalsIgnoreCase(c.getCourseId());
                            }
                            String des = loggedInUser.getDesignation();
                            if (des != null && des.contains(": ")) {
                                String cName = des.substring(des.indexOf(": ") + 2).trim();
                                return !cName.equalsIgnoreCase(c.getCourseName());
                            }
                            return true;
                        });
                    } else if (trainerCourse != null) {
                        loadedStudents.removeIf(s -> {
                            String scId = s.getCourseId();
                            if (scId != null && !scId.trim().isEmpty()) {
                                return !trainerCourse.equalsIgnoreCase(scId.trim());
                            }
                            return !trainerCourse.equalsIgnoreCase(s.getCourse());
                        });
                        // Filter courses list so trainer only sees their course
                        coursesList.removeIf(c -> !trainerCourse.equalsIgnoreCase(c.getCourseId()));
                    }

                    studentsList.clear();
                    studentsList.addAll(loadedStudents);

                    int maxIdNum = 0;
                    for (Student s : loadedStudents) {
                        String id = s.getId();
                        if (id != null && id.startsWith("ST")) {
                            try {
                                int num = Integer.parseInt(id.substring(2).trim());
                                if (num > maxIdNum) {
                                    maxIdNum = num;
                                }
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }
                    nextStudentIdNum = maxIdNum + 1;

                    if (loggedInUser != null && loggedInUser.getRole() == LoggedInUser.Role.STUDENT) {
                        loadedAttendance.removeIf(a -> !loggedInUser.getId().equalsIgnoreCase(a.getStudentId()));
                    } else if (trainerCourse != null) {
                        loadedAttendance.removeIf(a -> {
                            String acId = a.getCourseId();
                            if (acId != null && !acId.trim().isEmpty()) {
                                return !trainerCourse.equalsIgnoreCase(acId.trim());
                            }
                            for (Student s : studentsList) {
                                if (s.getId().equalsIgnoreCase(a.getStudentId())) {
                                    return false;
                                }
                            }
                            return true;
                        });
                    }

                    attendanceList.clear();
                    attendanceList.addAll(loadedAttendance);

                    presentStudentIds.clear();
                    for (Student s : loadedStudents) {
                        if ("Active".equalsIgnoreCase(s.getStatus())) {
                            presentStudentIds.add(s.getId());
                        }
                    }

                    updateDashboardMetrics();
                });
                return null;
            }
        };

        new Thread(loadTask).start();
    }

    // --- PANEL 2: STUDENT MANAGEMENT ---
    private void buildStudentsPanel() {
        studentsPanel = new VBox(20);
        studentsPanel.setPadding(new Insets(24));
        studentsPanel.getStyleClass().add("root");

        Label lblTitle = new Label("Student Directory & Profiles");
        lblTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: -text-main;");

        // Layout: Table full width
        HBox bodyLayout = new HBox(20);
        HBox.setHgrow(bodyLayout, Priority.ALWAYS);
        VBox.setVgrow(bodyLayout, Priority.ALWAYS);

        // Left Table Box (100% width)
        VBox tableBox = new VBox(15);
        tableBox.getStyleClass().add("content-card");
        HBox.setHgrow(tableBox, Priority.ALWAYS);

        HBox tableHeaderBar = new HBox(15);
        tableHeaderBar.setAlignment(Pos.CENTER_LEFT);

        Label lblTableTitle = new Label("Student Enrollments");
        lblTableTitle.getStyleClass().add("card-title");
        lblTableTitle.setStyle("-fx-padding: 0;");

        Region toolbarSpacer = new Region();
        HBox.setHgrow(toolbarSpacer, Priority.ALWAYS);

        Button btnManageStudents = new Button("Manage Students ⚙️");
        btnManageStudents.getStyleClass().addAll("btn", "btn-primary");
        btnManageStudents.setVisible(loggedInUser != null && (loggedInUser.getRole() == LoggedInUser.Role.ADMIN || loggedInUser.getRole() == LoggedInUser.Role.TRAINER));
        btnManageStudents.setManaged(loggedInUser != null && (loggedInUser.getRole() == LoggedInUser.Role.ADMIN || loggedInUser.getRole() == LoggedInUser.Role.TRAINER));
        btnManageStudents.setOnAction(e -> openStudentEditorModal());

        tableHeaderBar.getChildren().addAll(lblTableTitle, toolbarSpacer, btnManageStudents);

        StackPane tableContainer = new StackPane();
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        studentTable = new TableView<>();
        studentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Progress Overlay
        loadingOverlay = new VBox(15);
        loadingOverlay.setAlignment(Pos.CENTER);
        loadingOverlay.getStyleClass().add("loading-overlay");
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setStyle("-fx-progress-color: -primary-color;");
        Label lblLoading = new Label("Loading students from Google Sheets...");
        lblLoading.getStyleClass().add("loading-text");
        loadingOverlay.getChildren().addAll(progressIndicator, lblLoading);
        loadingOverlay.setVisible(false);

        tableContainer.getChildren().addAll(studentTable, loadingOverlay);

        // Table Columns - 13 columns in order
        TableColumn<Student, String> colId = new TableColumn<>("Student_ID");
        colId.setCellValueFactory(data -> data.getValue().studentIdProperty());
        colId.setPrefWidth(90);

        TableColumn<Student, String> colErpNo = new TableColumn<>("ERP_No");
        colErpNo.setCellValueFactory(data -> data.getValue().erpNoProperty());
        colErpNo.setPrefWidth(90);

        TableColumn<Student, String> colName = new TableColumn<>("Student_Name");
        colName.setCellValueFactory(data -> data.getValue().studentNameProperty());
        colName.setPrefWidth(140);

        TableColumn<Student, String> colFatherName = new TableColumn<>("Father_Name");
        colFatherName.setCellValueFactory(data -> data.getValue().fatherNameProperty());
        colFatherName.setPrefWidth(120);

        TableColumn<Student, String> colDob = new TableColumn<>("DOB");
        colDob.setCellValueFactory(data -> data.getValue().dobProperty());
        colDob.setPrefWidth(100);

        TableColumn<Student, String> colAddress = new TableColumn<>("Address");
        colAddress.setCellValueFactory(data -> data.getValue().addressProperty());
        colAddress.setPrefWidth(140);

        TableColumn<Student, String> colCourse = new TableColumn<>("Course");
        colCourse.setCellValueFactory(data -> data.getValue().courseProperty());
        colCourse.setPrefWidth(80);

        TableColumn<Student, String> colCenter = new TableColumn<>("Center");
        colCenter.setCellValueFactory(data -> data.getValue().centerProperty());
        colCenter.setPrefWidth(90);

        TableColumn<Student, String> colBatchId = new TableColumn<>("Batch_ID");
        colBatchId.setCellValueFactory(data -> data.getValue().batchIdProperty());
        colBatchId.setPrefWidth(95);

        TableColumn<Student, String> colBatchTime = new TableColumn<>("Batch_Time");
        colBatchTime.setCellValueFactory(data -> data.getValue().batchTimeProperty());
        colBatchTime.setPrefWidth(110);

        TableColumn<Student, String> colDoj = new TableColumn<>("DOJ");
        colDoj.setCellValueFactory(data -> data.getValue().dojProperty());
        colDoj.setPrefWidth(100);

        TableColumn<Student, String> colCourseDuration = new TableColumn<>("Course_Duration");
        colCourseDuration.setCellValueFactory(data -> data.getValue().courseDurationProperty());
        colCourseDuration.setPrefWidth(120);

        TableColumn<Student, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());
        colStatus.setPrefWidth(120);

        // Customize Status column cell drawing
        colStatus.setCellFactory(column -> new TableCell<Student, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("status-badge");
                    switch (item.toLowerCase()) {
                        case "active":
                            badge.getStyleClass().add("status-active");
                            break;
                        case "course completed":
                            badge.getStyleClass().add("status-completed");
                            break;
                        case "ready for exam":
                            badge.getStyleClass().add("status-ready");
                            break;
                        case "exam passed":
                            badge.getStyleClass().add("status-passed");
                            break;
                        case "exam failed":
                            badge.getStyleClass().add("status-failed");
                            break;
                        case "inactive":
                            badge.getStyleClass().add("status-inactive");
                            break;
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        studentTable.getColumns().addAll(colId, colErpNo, colName, colFatherName, colDob, colAddress, colCourse,
                colCenter, colBatchId, colBatchTime, colDoj, colCourseDuration, colStatus);
        studentTable.setItems(filteredStudents);

        tableBox.getChildren().addAll(tableHeaderBar, tableContainer);

        // Form Box (For Modal Editor popup)
        formBox = new VBox(15);
        formBox.getStyleClass().add("content-card");
        formBox.setPrefWidth(450);

        Label lblFormTitle = new Label("Student Details Editor");
        lblFormTitle.getStyleClass().add("card-title");

        // Selector toolbar at top
        HBox selectorBar = new HBox(10);
        selectorBar.setAlignment(Pos.CENTER_LEFT);
        selectorBar.setStyle(
                "-fx-padding: 0 0 10 0; -fx-border-color: transparent transparent -border-color transparent; -fx-border-width: 0 0 1 0;");

        TextField txtEditorSearch = new TextField();
        txtEditorSearch.setPromptText("Search Name / ERP / ID");
        txtEditorSearch.getStyleClass().add("text-input");
        txtEditorSearch.setPrefWidth(140);

        cbEditorStudentSelect = new ComboBox<>();
        cbEditorStudentSelect.setPromptText("Select Student");
        cbEditorStudentSelect.setPrefWidth(160);
        cbEditorStudentSelect.getStyleClass().add("combo-box");

        cbEditorStudentSelect.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Student s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) {
                    setText(null);
                } else {
                    setText(s.getName() + " (" + s.getId() + ")");
                }
            }
        });
        cbEditorStudentSelect.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Student s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) {
                    setText("Select Student");
                } else {
                    setText(s.getName() + " (" + s.getId() + ")");
                }
            }
        });

        txtEditorSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                cbEditorStudentSelect.setItems(studentsList);
            } else {
                String search = newVal.toLowerCase();
                ObservableList<Student> matches = FXCollections.observableArrayList();
                for (Student s : studentsList) {
                    if ((s.getName() != null && s.getName().toLowerCase().contains(search))
                            || (s.getId() != null && s.getId().toLowerCase().contains(search))
                            || (s.getErpNo() != null && s.getErpNo().toLowerCase().contains(search))) {
                        matches.add(s);
                    }
                }
                cbEditorStudentSelect.setItems(matches);
            }
            cbEditorStudentSelect.show();
        });

        cbEditorStudentSelect.valueProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                studentTable.getSelectionModel().select(newSel);
                populateForm(newSel);
                btnAdd.setDisable(true);
                if (!"Viewer".equals(currentRole)) {
                    btnUpdate.setDisable(false);
                    btnDelete.setDisable(!"Admin".equals(currentRole) && !"Trainer".equals(currentRole));
                }
            }
        });

        Button btnSelectStudent = new Button("Select");
        btnSelectStudent.getStyleClass().addAll("btn", "btn-primary");
        btnSelectStudent.setOnAction(e -> {
            Student selected = cbEditorStudentSelect.getValue();
            if (selected != null) {
                studentTable.getSelectionModel().select(selected);
                populateForm(selected);
                btnAdd.setDisable(true);
                if (!"Viewer".equals(currentRole)) {
                    btnUpdate.setDisable(false);
                    btnDelete.setDisable(!"Admin".equals(currentRole) && !"Trainer".equals(currentRole));
                }
            }
        });

        selectorBar.getChildren().addAll(new Label("Find student:"), txtEditorSearch, cbEditorStudentSelect,
                btnSelectStudent);

        ScrollPane formScroll = new ScrollPane();
        formScroll.setFitToWidth(true);
        formScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(formScroll, Priority.ALWAYS);

        GridPane gp = new GridPane();
        gp.setHgap(12);
        gp.setVgap(12);
        gp.setPadding(new Insets(5));
        formScroll.setContent(gp);

        // Column Constraints
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(38);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(62);
        gp.getColumnConstraints().addAll(col1, col2);

        // ID
        txtId = new TextField();
        txtId.setEditable(false);
        txtId.getStyleClass().add("text-input");
        gp.add(new Label("Student ID (Auto):"), 0, 0);
        gp.add(txtId, 1, 0);

        // Name
        txtName = new TextField();
        gp.add(new Label("Student Name * :"), 0, 1);
        gp.add(txtName, 1, 1);

        // Father Name
        txtFatherName = new TextField();
        gp.add(new Label("Father's Name:"), 0, 2);
        gp.add(txtFatherName, 1, 2);

        // Mother Name
        txtMotherName = new TextField();
        gp.add(new Label("Mother's Name:"), 0, 3);
        gp.add(txtMotherName, 1, 3);

        // Mobile
        txtMobile = new TextField();
        gp.add(new Label("Mobile Number * :"), 0, 4);
        gp.add(txtMobile, 1, 4);

        // Alt Mobile
        txtAltMobile = new TextField();
        gp.add(new Label("Alt Mobile Number:"), 0, 5);
        gp.add(txtAltMobile, 1, 5);

        // Email
        txtEmail = new TextField();
        gp.add(new Label("Email ID:"), 0, 6);
        gp.add(txtEmail, 1, 6);

        // Course
        cbCourse = new ComboBox<>();
        cbCourse.getItems().addAll("DM", "DTP");
        cbCourse.setMaxWidth(Double.MAX_VALUE);
        gp.add(new Label("Course Select * :"), 0, 7);
        gp.add(cbCourse, 1, 7);

        // Batch
        cbBatch = new ComboBox<>();
        cbBatch.getItems().addAll("A (10AM-1PM)", "B (2PM-5PM)", "C (3PM-6PM)");
        cbBatch.setMaxWidth(Double.MAX_VALUE);
        gp.add(new Label("Batch Time * :"), 0, 8);
        gp.add(cbBatch, 1, 8);

        // Admission Date
        dpAdmissionDate = new DatePicker();
        dpAdmissionDate.setMaxWidth(Double.MAX_VALUE);
        gp.add(new Label("Admission Date * :"), 0, 9);
        gp.add(dpAdmissionDate, 1, 9);

        // Completion Date (Calculated)
        lblCompletionDate = new Label("Select course & date");
        lblCompletionDate.setStyle("-fx-font-weight: bold; -fx-text-fill: -primary-color; -fx-padding: 6 0;");
        gp.add(new Label("Completion Date:"), 0, 10);
        gp.add(lblCompletionDate, 1, 10);

        // Status
        cbStatus = new ComboBox<>();
        cbStatus.getItems().addAll("Active", "Course Completed", "Ready For Exam", "Exam Passed", "Exam Failed",
                "Inactive");
        cbStatus.setValue("Active");
        cbStatus.setMaxWidth(Double.MAX_VALUE);
        gp.add(new Label("Academic Status:"), 0, 11);
        gp.add(cbStatus, 1, 11);

        // Address
        txtAddress = new TextField();
        gp.add(new Label("Home Address:"), 0, 12);
        gp.add(txtAddress, 1, 12);

        // Completion Date calculation listeners
        cbCourse.valueProperty().addListener((obs, oldVal, newVal) -> calculateCompletionDate());
        dpAdmissionDate.valueProperty().addListener((obs, oldVal, newVal) -> calculateCompletionDate());

        // Notification label in form
        lblFormMessage = new Label();
        lblFormMessage.setWrapText(true);
        lblFormMessage.setStyle("-fx-font-weight: bold;");

        // Buttons Box
        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new Insets(10, 0, 0, 0));

        btnAdd = new Button("Add Student");
        btnAdd.getStyleClass().addAll("btn", "btn-primary");
        btnAdd.setOnAction(e -> doAddStudent());

        btnUpdate = new Button("Update");
        btnUpdate.getStyleClass().addAll("btn", "btn-success");
        btnUpdate.setOnAction(e -> doUpdateStudent());
        btnUpdate.setDisable(true); // Disable until row selected

        btnDelete = new Button("Delete");
        btnDelete.getStyleClass().addAll("btn", "btn-danger");
        btnDelete.setOnAction(e -> doDeleteStudent());
        btnDelete.setDisable(true); // Disable until row selected

        btnClear = new Button("Clear");
        btnClear.getStyleClass().addAll("btn", "btn-secondary");
        btnClear.setOnAction(e -> clearFormSelection());

        btnBox.getChildren().addAll(btnAdd, btnUpdate, btnDelete, btnClear);

        formBox.getChildren().addAll(lblFormTitle, selectorBar, formScroll, lblFormMessage, btnBox);

        bodyLayout.getChildren().add(tableBox);
        studentsPanel.getChildren().addAll(lblTitle, bodyLayout);

        // Wire Table row selection listener
        studentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                populateForm(newSel);
                btnAdd.setDisable(true); // Can't add duplicate
                if (!"Viewer".equals(currentRole)) {
                    btnUpdate.setDisable(false);
                    btnDelete.setDisable(!"Admin".equals(currentRole)); // Only admin deletes
                }
            } else {
                resetFormForNewEntry();
            }
        });

        // Set initial ID
        resetFormForNewEntry();
    }

    private void calculateCompletionDate() {
        String course = cbCourse.getValue();
        LocalDate admDate = dpAdmissionDate.getValue();

        if (course != null && admDate != null) {
            int durationMonths = (course.equalsIgnoreCase("DTP") || course.toUpperCase().contains("DTP")) ? 4 : 3;
            LocalDate completion = admDate.plusMonths(durationMonths);
            lblCompletionDate.setText(completion.format(DATE_FORMATTER));
        } else {
            lblCompletionDate.setText("Auto calculated (select course & date)");
        }
    }

    private void populateForm(Student s) {
        txtId.setText(s.getId());
        txtName.setText(s.getName());
        txtFatherName.setText(s.getFatherName());
        txtMotherName.setText(s.getMotherName());
        txtMobile.setText(s.getMobile());
        txtAltMobile.setText(s.getAltMobile());
        txtEmail.setText(s.getEmail());
        String scId = s.getCourseId();
        if (scId != null && !scId.trim().isEmpty()) {
            cbCourse.setValue(scId.trim());
        } else {
            cbCourse.setValue(s.getCourse());
        }
        cbBatch.setValue(s.getBatch());

        try {
            dpAdmissionDate.setValue(LocalDate.parse(s.getAdmissionDate(), DATE_FORMATTER));
        } catch (Exception e) {
            dpAdmissionDate.setValue(null);
        }

        lblCompletionDate.setText(s.getCompletionDate());
        cbStatus.setValue(s.getStatus());
        txtAddress.setText(s.getAddress());
    }

    private void resetFormForNewEntry() {
        txtId.setText(String.format("ST%04d (New)", nextStudentIdNum));
        txtName.clear();
        txtFatherName.clear();
        txtMotherName.clear();
        txtMobile.clear();
        txtAltMobile.clear();
        txtEmail.clear();
        cbCourse.setValue(null);
        cbBatch.setValue(null);
        dpAdmissionDate.setValue(null);
        lblCompletionDate.setText("Auto calculated (select course & date)");
        cbStatus.setValue("Active");
        txtAddress.clear();

        btnAdd.setDisable("Viewer".equals(currentRole));
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
    }

    private void clearFormSelection() {
        studentTable.getSelectionModel().clearSelection();
        resetFormForNewEntry();
    }

    // CRUD IMPLEMENTATION
    private void doAddStudent() {
        if (!validateForm())
            return;

        String id = String.format("ST%04d", nextStudentIdNum);
        String selectedCourseId = cbCourse.getValue();
        String courseName = selectedCourseId;
        if (coursesList != null) {
            for (Course c : coursesList) {
                if (c.getCourseId().equalsIgnoreCase(selectedCourseId)) {
                    courseName = c.getCourseName();
                    break;
                }
            }
        }
        Student s = new Student(
                id,
                txtName.getText().trim(),
                txtFatherName.getText().trim(),
                txtMotherName.getText().trim(),
                txtMobile.getText().trim(),
                txtAltMobile.getText().trim(),
                txtEmail.getText().trim(),
                txtAddress.getText().trim(),
                courseName,
                cbBatch.getValue(),
                dpAdmissionDate.getValue().format(DATE_FORMATTER),
                lblCompletionDate.getText(),
                cbStatus.getValue());
        s.setErpNo("");
        s.setCourseId(selectedCourseId);

        formBox.setDisable(true);
        lblFormMessage.setText("Saving to Google Sheets...");
        lblFormMessage.setStyle("-fx-text-fill: -primary-color;");

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                GoogleSheetsService service = new GoogleSheetsService();
                service.addStudent(s);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            formBox.setDisable(false);
            nextStudentIdNum++;
            studentsList.add(s);
            addActivity("➕", "Added new student: " + s.getName() + " (" + s.getId() + ")");
            clearFormSelection();
            updateDashboardMetrics();
            if (editorModalStage != null) {
                editorModalStage.close();
            }
            showInfoAlert("Success", "Student added successfully to Google Sheets.", "Record " + id + " created.");
            fetchStudentsFromGoogleSheets();
        });

        task.setOnFailed(e -> {
            formBox.setDisable(false);
            Throwable ex = task.getException();
            ex.printStackTrace();
            lblFormMessage.setText("Error saving: " + ex.getMessage());
            lblFormMessage.setStyle("-fx-text-fill: -danger-color;");
            showErrorAlert("Save Error", "Failed to add student to Google Sheets.", ex.getMessage());
        });

        new Thread(task).start();
    }

    private void doUpdateStudent() {
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;
        if (!validateForm())
            return;

        String id = selected.getId();
        String selectedCourseId = cbCourse.getValue();
        String courseName = selectedCourseId;
        if (coursesList != null) {
            for (Course c : coursesList) {
                if (c.getCourseId().equalsIgnoreCase(selectedCourseId)) {
                    courseName = c.getCourseName();
                    break;
                }
            }
        }

        Student s = new Student(
                id,
                txtName.getText().trim(),
                txtFatherName.getText().trim(),
                txtMotherName.getText().trim(),
                txtMobile.getText().trim(),
                txtAltMobile.getText().trim(),
                txtEmail.getText().trim(),
                txtAddress.getText().trim(),
                courseName,
                cbBatch.getValue(),
                dpAdmissionDate.getValue().format(DATE_FORMATTER),
                lblCompletionDate.getText(),
                cbStatus.getValue());
        s.setErpNo(selected.getErpNo());
        s.setCenter(selected.getCenter());
        s.setDob(selected.getDob());
        s.setCourseId(selectedCourseId);

        formBox.setDisable(true);
        lblFormMessage.setText("Updating in Google Sheets...");
        lblFormMessage.setStyle("-fx-text-fill: -primary-color;");

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                GoogleSheetsService service = new GoogleSheetsService();
                service.updateStudent(s);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            formBox.setDisable(false);
            selected.setName(s.getName());
            selected.setFatherName(s.getFatherName());
            selected.setMotherName(s.getMotherName());
            selected.setMobile(s.getMobile());
            selected.setAltMobile(s.getAltMobile());
            selected.setEmail(s.getEmail());
            selected.setAddress(s.getAddress());
            selected.setCourse(s.getCourse());
            selected.setBatch(s.getBatch());
            selected.setAdmissionDate(s.getAdmissionDate());
            selected.setCompletionDate(s.getCompletionDate());
            selected.setStatus(s.getStatus());
            selected.setCourseId(s.getCourseId());

            studentTable.refresh();
            addActivity("📝", "Updated details for: " + selected.getName() + " (" + selected.getId() + ")");
            clearFormSelection();
            updateDashboardMetrics();
            if (editorModalStage != null) {
                editorModalStage.close();
            }
            showInfoAlert("Success", "Student profiles updated in Google Sheets.", "Record updated.");
            fetchStudentsFromGoogleSheets();
        });

        task.setOnFailed(e -> {
            formBox.setDisable(false);
            Throwable ex = task.getException();
            ex.printStackTrace();
            lblFormMessage.setText("Error updating: " + ex.getMessage());
            lblFormMessage.setStyle("-fx-text-fill: -danger-color;");
            showErrorAlert("Update Error", "Failed to update student in Google Sheets.", ex.getMessage());
        });

        new Thread(task).start();
    }

    private void doDeleteStudent() {
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Remove student record from Google Sheets?");
        confirm.setContentText(
                "Are you sure you want to permanently delete " + selected.getName() + " (" + selected.getId() + ")?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            formBox.setDisable(true);
            lblFormMessage.setText("Deleting from Google Sheets...");
            lblFormMessage.setStyle("-fx-text-fill: -danger-color;");

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    GoogleSheetsService service = new GoogleSheetsService();
                    service.deleteStudent(selected.getId());
                    return null;
                }
            };

            task.setOnSucceeded(e -> {
                formBox.setDisable(false);
                studentsList.remove(selected);
                addActivity("❌", "Deleted student profile: " + selected.getName() + " (" + selected.getId() + ")");
                clearFormSelection();
                updateDashboardMetrics();
                if (editorModalStage != null) {
                    editorModalStage.close();
                }
                showInfoAlert("Deleted", "Student profile deleted from Google Sheets.", "Record removed.");
                fetchStudentsFromGoogleSheets();
            });

            task.setOnFailed(e -> {
                formBox.setDisable(false);
                Throwable ex = task.getException();
                ex.printStackTrace();
                lblFormMessage.setText("Error deleting: " + ex.getMessage());
                lblFormMessage.setStyle("-fx-text-fill: -danger-color;");
                showErrorAlert("Delete Error", "Failed to delete student from Google Sheets.", ex.getMessage());
            });

            new Thread(task).start();
        }
    }

    private boolean validateForm() {
        if (txtName.getText().trim().isEmpty()) {
            showErrorAlert("Validation Error", "Missing Required Field", "Please input the student name.");
            return false;
        }
        if (txtMobile.getText().trim().isEmpty()) {
            showErrorAlert("Validation Error", "Missing Required Field", "Please input the student mobile number.");
            return false;
        }
        if (cbCourse.getValue() == null) {
            showErrorAlert("Validation Error", "Missing Course Option", "Please pick a course (DM or DTP).");
            return false;
        }
        if (cbBatch.getValue() == null) {
            showErrorAlert("Validation Error", "Missing Batch Option", "Please select a time slot.");
            return false;
        }
        if (dpAdmissionDate.getValue() == null) {
            showErrorAlert("Validation Error", "Missing Date Info", "Please pick the admission start date.");
            return false;
        }
        return true;
    }

    // --- PANEL 3: ATTENDANCE ---
    private void buildAttendancePanel() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(24));
        container.getStyleClass().add("root");

        Label lblTitle = new Label("Attendance Records");
        lblTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: -text-main;");

        VBox contentBox = new VBox(15);
        contentBox.getStyleClass().add("content-card");
        VBox.setVgrow(contentBox, Priority.ALWAYS);

        // Header controls inside Attendance
        HBox bar = new HBox(15);
        bar.setAlignment(Pos.CENTER_LEFT);

        Label lblSubtitle = new Label("Historical Attendance Log");
        lblSubtitle.setStyle("-fx-font-weight: bold; -fx-text-fill: -text-muted; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnRefresh = new Button("Refresh Data 🔄");
        btnRefresh.getStyleClass().addAll("btn", "btn-primary");
        btnRefresh.setOnAction(e -> refreshAttendanceData());

        bar.getChildren().addAll(lblSubtitle, spacer, btnRefresh);

        StackPane tableContainer = new StackPane();
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        attendanceTable = new TableView<>();
        attendanceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Progress Overlay
        loadingOverlayAttendance = new VBox(15);
        loadingOverlayAttendance.setAlignment(Pos.CENTER);
        loadingOverlayAttendance.getStyleClass().add("loading-overlay");
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setStyle("-fx-progress-color: -primary-color;");
        Label lblLoading = new Label("Loading attendance records from Google Sheets...");
        lblLoading.getStyleClass().add("loading-text");
        loadingOverlayAttendance.getChildren().addAll(progressIndicator, lblLoading);
        loadingOverlayAttendance.setVisible(false);

        tableContainer.getChildren().addAll(attendanceTable, loadingOverlayAttendance);

        // Setup columns in the exact order requested: Attendance_ID, Date, Student_ID,
        // Batch_ID, Status, Marked_By, Marked_Time
        TableColumn<Attendance, String> colAttId = new TableColumn<>("Attendance_ID");
        colAttId.setCellValueFactory(data -> data.getValue().attendanceIdProperty());
        colAttId.setPrefWidth(120);

        TableColumn<Attendance, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(data -> data.getValue().dateProperty());
        colDate.setPrefWidth(120);

        TableColumn<Attendance, String> colStudentId = new TableColumn<>("Student_ID");
        colStudentId.setCellValueFactory(data -> data.getValue().studentIdProperty());
        colStudentId.setPrefWidth(120);

        TableColumn<Attendance, String> colBatchId = new TableColumn<>("Batch_ID");
        colBatchId.setCellValueFactory(data -> data.getValue().batchIdProperty());
        colBatchId.setPrefWidth(100);

        TableColumn<Attendance, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());
        colStatus.setPrefWidth(120);
        colStatus.setCellFactory(column -> new TableCell<Attendance, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("status-badge");
                    switch (item.toLowerCase()) {
                        case "present":
                            badge.getStyleClass().add("status-active");
                            break;
                        case "absent":
                            badge.getStyleClass().add("status-inactive");
                            break;
                        default:
                            badge.getStyleClass().add("status-ready");
                            break;
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        TableColumn<Attendance, String> colMarkedBy = new TableColumn<>("Marked_By");
        colMarkedBy.setCellValueFactory(data -> data.getValue().markedByProperty());
        colMarkedBy.setPrefWidth(150);

        TableColumn<Attendance, String> colMarkedTime = new TableColumn<>("Marked_Time");
        colMarkedTime.setCellValueFactory(data -> data.getValue().markedTimeProperty());
        colMarkedTime.setPrefWidth(180);

        attendanceTable.getColumns().addAll(colAttId, colDate, colStudentId, colBatchId, colStatus, colMarkedBy,
                colMarkedTime);
        attendanceTable.setItems(attendanceList);

        contentBox.getChildren().addAll(bar, tableContainer);
        container.getChildren().addAll(lblTitle, contentBox);

        attendancePanel = new ScrollPane(container);
        attendancePanel.setFitToWidth(true);
        attendancePanel.setFitToHeight(true);
        attendancePanel.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
    }

    // --- PANEL 4: COURSES ---
    private void buildCoursesPanel() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(24));
        container.getStyleClass().add("root");

        Label lblTitle = new Label("Courses Directory");
        lblTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: -text-main;");

        VBox contentBox = new VBox(15);
        contentBox.getStyleClass().add("content-card");
        VBox.setVgrow(contentBox, Priority.ALWAYS);

        HBox bar = new HBox(15);
        bar.setAlignment(Pos.CENTER_LEFT);

        Label lblSubtitle = new Label("Dynamic Course List");
        lblSubtitle.setStyle("-fx-font-weight: bold; -fx-text-fill: -text-muted; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnManageCourses = new Button("Manage Courses ⚙️");
        btnManageCourses.getStyleClass().addAll("btn", "btn-secondary");
        btnManageCourses.setVisible(loggedInUser != null && loggedInUser.getRole() == LoggedInUser.Role.ADMIN);
        btnManageCourses.setManaged(loggedInUser != null && loggedInUser.getRole() == LoggedInUser.Role.ADMIN);
        btnManageCourses.setOnAction(e -> openCoursesEditorModal());

        Button btnRefresh = new Button("Refresh Data 🔄");
        btnRefresh.getStyleClass().addAll("btn", "btn-primary");
        btnRefresh.setOnAction(e -> refreshCoursesData());

        bar.getChildren().addAll(lblSubtitle, spacer, btnManageCourses, btnRefresh);

        StackPane tableContainer = new StackPane();
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        coursesTable = new TableView<>();
        coursesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Progress Overlay
        loadingOverlayCourses = new VBox(15);
        loadingOverlayCourses.setAlignment(Pos.CENTER);
        loadingOverlayCourses.getStyleClass().add("loading-overlay");
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setStyle("-fx-progress-color: -primary-color;");
        Label lblLoading = new Label("Loading courses from Google Sheets...");
        lblLoading.getStyleClass().add("loading-text");
        loadingOverlayCourses.getChildren().addAll(progressIndicator, lblLoading);
        loadingOverlayCourses.setVisible(false);

        tableContainer.getChildren().addAll(coursesTable, loadingOverlayCourses);

        TableColumn<Course, String> colCourseId = new TableColumn<>("Course_ID");
        colCourseId.setCellValueFactory(data -> data.getValue().courseIdProperty());
        colCourseId.setPrefWidth(150);

        TableColumn<Course, String> colCourseName = new TableColumn<>("Course_Name");
        colCourseName.setCellValueFactory(data -> data.getValue().courseNameProperty());
        colCourseName.setPrefWidth(300);

        coursesTable.getColumns().addAll(colCourseId, colCourseName);
        coursesTable.setItems(coursesList);

        contentBox.getChildren().addAll(bar, tableContainer);
        container.getChildren().addAll(lblTitle, contentBox);

        coursesPanel = new ScrollPane(container);
        coursesPanel.setFitToWidth(true);
        coursesPanel.setFitToHeight(true);
        coursesPanel.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
    }

    // --- PANEL 4A: BATCHES ---
    private void buildBatchesPanel() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(24));
        container.getStyleClass().add("root");

        Label lblTitle = new Label("Batches Directory");
        lblTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: -text-main;");

        VBox contentBox = new VBox(15);
        contentBox.getStyleClass().add("content-card");
        VBox.setVgrow(contentBox, Priority.ALWAYS);

        HBox bar = new HBox(15);
        bar.setAlignment(Pos.CENTER_LEFT);

        Label lblSubtitle = new Label("Dynamic Batch List");
        lblSubtitle.setStyle("-fx-font-weight: bold; -fx-text-fill: -text-muted; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnManageBatches = new Button("Manage Batches ⚙️");
        btnManageBatches.getStyleClass().addAll("btn", "btn-secondary");
        btnManageBatches.setVisible(loggedInUser != null && loggedInUser.getRole() == LoggedInUser.Role.ADMIN);
        btnManageBatches.setManaged(loggedInUser != null && loggedInUser.getRole() == LoggedInUser.Role.ADMIN);
        btnManageBatches.setOnAction(e -> openBatchesEditorModal());

        Button btnRefresh = new Button("Refresh Data 🔄");
        btnRefresh.getStyleClass().addAll("btn", "btn-primary");
        btnRefresh.setOnAction(e -> refreshBatchesData());

        bar.getChildren().addAll(lblSubtitle, spacer, btnManageBatches, btnRefresh);

        StackPane tableContainer = new StackPane();
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        batchesTable = new TableView<>();
        batchesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Progress Overlay
        loadingOverlayBatches = new VBox(15);
        loadingOverlayBatches.setAlignment(Pos.CENTER);
        loadingOverlayBatches.getStyleClass().add("loading-overlay");
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setStyle("-fx-progress-color: -primary-color;");
        Label lblLoading = new Label("Loading batches from Google Sheets...");
        lblLoading.getStyleClass().add("loading-text");
        loadingOverlayBatches.getChildren().addAll(progressIndicator, lblLoading);
        loadingOverlayBatches.setVisible(false);

        tableContainer.getChildren().addAll(batchesTable, loadingOverlayBatches);

        TableColumn<Batch, String> colBatchId = new TableColumn<>("Batch_ID");
        colBatchId.setCellValueFactory(data -> data.getValue().batchIdProperty());
        colBatchId.setPrefWidth(100);

        TableColumn<Batch, String> colCourseId = new TableColumn<>("Course_ID");
        colCourseId.setCellValueFactory(data -> data.getValue().courseIdProperty());
        colCourseId.setPrefWidth(100);

        TableColumn<Batch, String> colBatchNo = new TableColumn<>("Batch_No");
        colBatchNo.setCellValueFactory(data -> data.getValue().batchNoProperty());
        colBatchNo.setPrefWidth(100);

        TableColumn<Batch, String> colStartDate = new TableColumn<>("Start_Date");
        colStartDate.setCellValueFactory(data -> data.getValue().startDateProperty());
        colStartDate.setPrefWidth(120);

        TableColumn<Batch, String> colEndDate = new TableColumn<>("End_Date");
        colEndDate.setCellValueFactory(data -> data.getValue().endDateProperty());
        colEndDate.setPrefWidth(120);

        TableColumn<Batch, String> colBatchTime = new TableColumn<>("Batch_Time");
        colBatchTime.setCellValueFactory(data -> data.getValue().batchTimeProperty());
        colBatchTime.setPrefWidth(150);

        batchesTable.getColumns().addAll(colBatchId, colCourseId, colBatchNo, colStartDate, colEndDate, colBatchTime);
        batchesTable.setItems(batchesList);

        contentBox.getChildren().addAll(bar, tableContainer);
        container.getChildren().addAll(lblTitle, contentBox);

        batchesPanel = new ScrollPane(container);
        batchesPanel.setFitToWidth(true);
        batchesPanel.setFitToHeight(true);
        batchesPanel.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
    }

    // --- PANEL 4B: COMPANIES ---
    private void buildCompaniesPanel() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(24));
        container.getStyleClass().add("root");

        Label lblTitle = new Label("Companies Directory");
        lblTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: -text-main;");

        VBox contentBox = new VBox(15);
        contentBox.getStyleClass().add("content-card");
        VBox.setVgrow(contentBox, Priority.ALWAYS);

        HBox bar = new HBox(15);
        bar.setAlignment(Pos.CENTER_LEFT);

        Label lblSubtitle = new Label("Registered HR & Company Contacts");
        lblSubtitle.setStyle("-fx-font-weight: bold; -fx-text-fill: -text-muted; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnManageCompanies = new Button("Manage Companies ⚙️");
        btnManageCompanies.getStyleClass().addAll("btn", "btn-secondary");
        btnManageCompanies.setVisible(loggedInUser != null && loggedInUser.getRole() == LoggedInUser.Role.ADMIN);
        btnManageCompanies.setManaged(loggedInUser != null && loggedInUser.getRole() == LoggedInUser.Role.ADMIN);
        btnManageCompanies.setOnAction(e -> openCompaniesEditorModal());

        Button btnRefresh = new Button("Refresh Data 🔄");
        btnRefresh.getStyleClass().addAll("btn", "btn-primary");
        btnRefresh.setOnAction(e -> refreshCompaniesData());

        bar.getChildren().addAll(lblSubtitle, spacer, btnManageCompanies, btnRefresh);

        StackPane tableContainer = new StackPane();
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        companiesTable = new TableView<>();
        companiesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Progress Overlay
        loadingOverlayCompanies = new VBox(15);
        loadingOverlayCompanies.setAlignment(Pos.CENTER);
        loadingOverlayCompanies.getStyleClass().add("loading-overlay");
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setStyle("-fx-progress-color: -primary-color;");
        Label lblLoading = new Label("Loading companies from Google Sheets...");
        lblLoading.getStyleClass().add("loading-text");
        loadingOverlayCompanies.getChildren().addAll(progressIndicator, lblLoading);
        loadingOverlayCompanies.setVisible(false);

        tableContainer.getChildren().addAll(companiesTable, loadingOverlayCompanies);

        TableColumn<Company, String> colCompanyId = new TableColumn<>("Company_ID");
        colCompanyId.setCellValueFactory(data -> data.getValue().companyIdProperty());
        colCompanyId.setPrefWidth(100);

        TableColumn<Company, String> colCompanyName = new TableColumn<>("Company_Name");
        colCompanyName.setCellValueFactory(data -> data.getValue().companyNameProperty());
        colCompanyName.setPrefWidth(180);

        TableColumn<Company, String> colHrName = new TableColumn<>("HR_Name");
        colHrName.setCellValueFactory(data -> data.getValue().hrNameProperty());
        colHrName.setPrefWidth(120);

        TableColumn<Company, String> colCompanyAddress = new TableColumn<>("Company_Address");
        colCompanyAddress.setCellValueFactory(data -> data.getValue().companyAddressProperty());
        colCompanyAddress.setPrefWidth(220);

        TableColumn<Company, String> colContactInfo = new TableColumn<>("Contact_Info");
        colContactInfo.setCellValueFactory(data -> data.getValue().contactInfoProperty());
        colContactInfo.setPrefWidth(150);

        companiesTable.getColumns().addAll(colCompanyId, colCompanyName, colHrName, colCompanyAddress, colContactInfo);
        companiesTable.setItems(companiesList);

        contentBox.getChildren().addAll(bar, tableContainer);
        container.getChildren().addAll(lblTitle, contentBox);

        companiesPanel = new ScrollPane(container);
        companiesPanel.setFitToWidth(true);
        companiesPanel.setFitToHeight(true);
        companiesPanel.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
    }

    // --- PANEL 4C: STUDENT PLACEMENTS ---
    private void buildPlacementsPanel() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(24));
        container.getStyleClass().add("root");

        Label lblTitle = new Label("Student Placements");
        lblTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: -text-main;");

        VBox contentBox = new VBox(15);
        contentBox.getStyleClass().add("content-card");
        VBox.setVgrow(contentBox, Priority.ALWAYS);

        HBox bar = new HBox(15);
        bar.setAlignment(Pos.CENTER_LEFT);

        Label lblSubtitle = new Label("Placement Selection Logs");
        lblSubtitle.setStyle("-fx-font-weight: bold; -fx-text-fill: -text-muted; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnManagePlacements = new Button("Manage Placements ⚙️");
        btnManagePlacements.getStyleClass().addAll("btn", "btn-secondary");
        btnManagePlacements.setVisible(loggedInUser != null && loggedInUser.getRole() == LoggedInUser.Role.ADMIN);
        btnManagePlacements.setManaged(loggedInUser != null && loggedInUser.getRole() == LoggedInUser.Role.ADMIN);
        btnManagePlacements.setOnAction(e -> openPlacementsEditorModal());

        Button btnRefresh = new Button("Refresh Data 🔄");
        btnRefresh.getStyleClass().addAll("btn", "btn-primary");
        btnRefresh.setOnAction(e -> refreshPlacementsData());

        bar.getChildren().addAll(lblSubtitle, spacer, btnManagePlacements, btnRefresh);

        StackPane tableContainer = new StackPane();
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        placementsTable = new TableView<>();
        placementsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Progress Overlay
        loadingOverlayPlacements = new VBox(15);
        loadingOverlayPlacements.setAlignment(Pos.CENTER);
        loadingOverlayPlacements.getStyleClass().add("loading-overlay");
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setStyle("-fx-progress-color: -primary-color;");
        Label lblLoading = new Label("Loading placements from Google Sheets...");
        lblLoading.getStyleClass().add("loading-text");
        loadingOverlayPlacements.getChildren().addAll(progressIndicator, lblLoading);
        loadingOverlayPlacements.setVisible(false);

        tableContainer.getChildren().addAll(placementsTable, loadingOverlayPlacements);

        TableColumn<StudentPlacement, String> colPlacementId = new TableColumn<>("Placement_ID");
        colPlacementId.setCellValueFactory(data -> data.getValue().placementIdProperty());
        colPlacementId.setPrefWidth(110);

        TableColumn<StudentPlacement, String> colStudentId = new TableColumn<>("Student_ID");
        colStudentId.setCellValueFactory(data -> data.getValue().studentIdProperty());
        colStudentId.setPrefWidth(100);

        TableColumn<StudentPlacement, String> colErpNo = new TableColumn<>("ERP_No");
        colErpNo.setCellValueFactory(data -> data.getValue().erpNoProperty());
        colErpNo.setPrefWidth(100);

        TableColumn<StudentPlacement, String> colCompanyId = new TableColumn<>("Company_ID");
        colCompanyId.setCellValueFactory(data -> data.getValue().companyIdProperty());
        colCompanyId.setPrefWidth(100);

        TableColumn<StudentPlacement, String> colPlacementStatus = new TableColumn<>("Placement_Status");
        colPlacementStatus.setCellValueFactory(data -> data.getValue().placementStatusProperty());
        colPlacementStatus.setPrefWidth(140);
        colPlacementStatus.setCellFactory(column -> new TableCell<StudentPlacement, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("status-badge");
                    String lower = item.toLowerCase();
                    if (lower.contains("select") || lower.contains("place") || lower.contains("pass")
                            || lower.contains("active")) {
                        badge.getStyleClass().add("status-active");
                    } else if (lower.contains("pend") || lower.contains("wait")) {
                        badge.getStyleClass().add("status-ready");
                    } else if (lower.contains("reject") || lower.contains("fail")) {
                        badge.getStyleClass().add("status-failed");
                    } else {
                        badge.getStyleClass().add("status-inactive");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        TableColumn<StudentPlacement, String> colSelectionDate = new TableColumn<>("Selection_Date");
        colSelectionDate.setCellValueFactory(data -> data.getValue().selectionDateProperty());
        colSelectionDate.setPrefWidth(120);

        TableColumn<StudentPlacement, String> colRemark = new TableColumn<>("Remark");
        colRemark.setCellValueFactory(data -> data.getValue().remarkProperty());
        colRemark.setPrefWidth(180);

        placementsTable.getColumns().addAll(colPlacementId, colStudentId, colErpNo, colCompanyId, colPlacementStatus,
                colSelectionDate, colRemark);
        placementsTable.setItems(placementsList);

        contentBox.getChildren().addAll(bar, tableContainer);
        container.getChildren().addAll(lblTitle, contentBox);

        placementsPanel = new ScrollPane(container);
        placementsPanel.setFitToWidth(true);
        placementsPanel.setFitToHeight(true);
        placementsPanel.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
    }

    // --- PANEL 5: REPORTS ---
    private void buildReportsPanel() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(24));
        container.getStyleClass().add("root");

        Label lblTitle = new Label("Institute Performance Reports");
        lblTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: -text-main;");

        VBox contentBox = new VBox(15);
        contentBox.getStyleClass().add("content-card");
        contentBox.setPrefHeight(400);

        Label cardTitle = new Label("Metrics Summary & Statistics");
        cardTitle.getStyleClass().add("card-title");

        GridPane gp = new GridPane();
        gp.setHgap(30);
        gp.setVgap(15);

        gp.add(new Label("Report Name"), 0, 0);
        gp.add(new Label("Result Value"), 1, 0);
        gp.add(new Label("Status Indicator"), 2, 0);

        gp.getChildren().forEach(n -> n.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;"));

        gp.add(new Label("Overall Attendance Rate:"), 0, 1);
        gp.add(new Label("79.2%"), 1, 1);
        Label indicator1 = new Label("Healthy");
        indicator1.setStyle("-fx-text-fill: -success-color; -fx-font-weight: bold;");
        gp.add(indicator1, 2, 1);

        gp.add(new Label("Average Student Duration:"), 0, 2);
        gp.add(new Label("3.5 Months"), 1, 2);
        Label indicator2 = new Label("Expected");
        indicator2.setStyle("-fx-text-fill: -primary-color; -fx-font-weight: bold;");
        gp.add(indicator2, 2, 2);

        gp.add(new Label("Drop-out Rate (Inactive):"), 0, 3);
        gp.add(new Label("0%"), 1, 3);
        Label indicator3 = new Label("Optimal");
        indicator3.setStyle("-fx-text-fill: -success-color; -fx-font-weight: bold;");
        gp.add(indicator3, 2, 3);

        contentBox.getChildren().addAll(cardTitle, gp);
        container.getChildren().addAll(lblTitle, contentBox);

        reportsPanel = new ScrollPane(container);
        reportsPanel.setFitToWidth(true);
        reportsPanel.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
    }

    // --- PANEL 6: USERS ---
    private void buildUsersPanel() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(24));
        container.getStyleClass().add("root");

        Label lblTitle = new Label("Manage System Access");
        lblTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: -text-main;");

        VBox contentBox = new VBox(15);
        contentBox.getStyleClass().add("content-card");
        contentBox.setPrefHeight(400);

        Label cardTitle = new Label("Registered System Profiles");
        cardTitle.getStyleClass().add("card-title");

        VBox list = new VBox(10);
        list.getChildren().addAll(
                createUserProfileRow("Admin User (You)", "Admin", "Full administrator operations."),
                createUserProfileRow("Trainer Staff", "Trainer", "Attendance entries and status updates."),
                createUserProfileRow("Viewer Account", "Viewer", "Guest view-only accesses."));

        contentBox.getChildren().addAll(cardTitle, list);
        container.getChildren().addAll(lblTitle, contentBox);

        usersPanel = new ScrollPane(container);
        usersPanel.setFitToWidth(true);
        usersPanel.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
    }

    private HBox createUserProfileRow(String name, String role, String desc) {
        HBox box = new HBox(20);
        box.getStyleClass().add("user-profile-row");

        Circle av = new Circle(15, Color.web("#2563EB"));
        Label avText = new Label(role.substring(0, 1));
        avText.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        StackPane sp = new StackPane(av, avText);

        VBox txts = new VBox(2);
        Label lblName = new Label(name);
        lblName.setStyle("-fx-font-weight: bold;");
        Label lblDesc = new Label(desc);
        lblDesc.setStyle("-fx-font-size: 11px; -fx-text-fill: -text-muted;");
        txts.getChildren().addAll(lblName, lblDesc);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badge = new Label(role);
        badge.getStyleClass().add("role-badge");
        if ("Trainer".equals(role)) {
            badge.getStyleClass().add("role-badge-trainer");
        } else if ("Viewer".equals(role)) {
            badge.getStyleClass().add("role-badge-viewer");
        }

        box.getChildren().addAll(sp, txts, spacer, badge);
        return box;
    }

    // --- PANEL 7: SETTINGS ---
    private void buildSettingsPanel() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(24));
        container.getStyleClass().add("root");

        Label lblTitle = new Label("System Settings");
        lblTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: -text-main;");

        VBox contentBox = new VBox(15);
        contentBox.getStyleClass().add("content-card");
        contentBox.setPrefHeight(400);

        Label cardTitle = new Label("Global Configurations");
        cardTitle.getStyleClass().add("card-title");

        GridPane gp = new GridPane();
        gp.setHgap(15);
        gp.setVgap(15);

        gp.add(new Label("Application Name:"), 0, 0);
        TextField txtAppName = new TextField("YouvakendraSM");
        gp.add(txtAppName, 1, 0);

        gp.add(new Label("Primary Accent Color:"), 0, 1);
        ComboBox<String> cbColor = new ComboBox<>();
        cbColor.getItems().addAll("Ocean Blue (Default)", "Forest Green", "Crimson Red");
        cbColor.setValue("Ocean Blue (Default)");
        gp.add(cbColor, 1, 1);

        gp.add(new Label("Local Database Engine:"), 0, 2);
        Label lblEngine = new Label("In-Memory Mock Runtime");
        lblEngine.setStyle("-fx-font-weight: bold; -fx-text-fill: -text-muted;");
        gp.add(lblEngine, 1, 2);

        Button btnSaveSettings = new Button("Apply Configurations");
        btnSaveSettings.getStyleClass().addAll("btn", "btn-primary");
        btnSaveSettings.setOnAction(e -> {
            lblLogo.setText(txtAppName.getText());
            showInfoAlert("Success", "Settings applied successfully.", "Primary details configured.");
        });

        contentBox.getChildren().addAll(cardTitle, gp, btnSaveSettings);
        container.getChildren().addAll(lblTitle, contentBox);

        settingsPanel = new ScrollPane(container);
        settingsPanel.setFitToWidth(true);
        settingsPanel.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
    }

    // --- DIALOGS HELPER ---
    private void showInfoAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void fetchStudentsFromGoogleSheets() {
        if (loadingOverlay.isVisible()) {
            return;
        }
        loadingOverlay.setVisible(true);
        formBox.setDisable(true);

        javafx.concurrent.Task<List<Student>> loadTask = new javafx.concurrent.Task<>() {
            @Override
            protected List<Student> call() throws Exception {
                GoogleSheetsService service = new GoogleSheetsService();
                return service.readStudents();
            }
        };

        loadTask.setOnSucceeded(e -> {
            List<Student> loaded = loadTask.getValue();

            String trainerCourse = getTrainerCourseId();
            if (loggedInUser != null && loggedInUser.getRole() == LoggedInUser.Role.STUDENT) {
                loaded.removeIf(s -> !loggedInUser.getId().equalsIgnoreCase(s.getId()));
            } else if (trainerCourse != null) {
                loaded.removeIf(s -> {
                    String scId = s.getCourseId();
                    if (scId != null && !scId.trim().isEmpty()) {
                        return !trainerCourse.equalsIgnoreCase(scId.trim());
                    }
                    return !trainerCourse.equalsIgnoreCase(s.getCourse());
                });
            }

            studentsList.clear();
            studentsList.addAll(loaded);

            int maxIdNum = 0;
            for (Student s : loaded) {
                String id = s.getId();
                if (id != null && id.startsWith("ST")) {
                    try {
                        int num = Integer.parseInt(id.substring(2).trim());
                        if (num > maxIdNum) {
                            maxIdNum = num;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            nextStudentIdNum = maxIdNum + 1;

            presentStudentIds.clear();
            for (Student s : loaded) {
                if ("Active".equalsIgnoreCase(s.getStatus())) {
                    presentStudentIds.add(s.getId());
                }
            }

            loadingOverlay.setVisible(false);
            formBox.setDisable(false);
            updateDashboardMetrics();
            addActivity("🔄", "Fetched " + loaded.size() + " student profiles from Google Sheets");
        });

        loadTask.setOnFailed(e -> {
            Throwable ex = loadTask.getException();
            ex.printStackTrace();

            loadingOverlay.setVisible(false);
            formBox.setDisable(false);

            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            ex.printStackTrace(pw);
            String stackTrace = sw.toString();
            if (stackTrace.length() > 800) {
                stackTrace = stackTrace.substring(0, 800) + "\n...";
            }

            showErrorAlert("Data Load Error",
                    "Failed to connect to Google Sheets",
                    "Error details:\n" + ex.toString() + "\n\nStack Trace:\n" + stackTrace
                            + "\n\nPlease check your network connection and credentials.");
        });

        new Thread(loadTask).start();
    }

    public void refreshAttendanceData() {
        if (loadingOverlayAttendance.isVisible()) {
            return;
        }
        loadingOverlayAttendance.setVisible(true);
        attendanceTable.setDisable(true);

        javafx.concurrent.Task<List<Attendance>> loadTask = new javafx.concurrent.Task<>() {
            @Override
            protected List<Attendance> call() throws Exception {
                GoogleSheetsService service = new GoogleSheetsService();
                return service.getAttendanceRecords();
            }
        };

        loadTask.setOnSucceeded(e -> {
            List<Attendance> loaded = loadTask.getValue();

            String trainerCourse = getTrainerCourseId();
            if (loggedInUser != null && loggedInUser.getRole() == LoggedInUser.Role.STUDENT) {
                loaded.removeIf(a -> !loggedInUser.getId().equalsIgnoreCase(a.getStudentId()));
            } else if (trainerCourse != null) {
                loaded.removeIf(a -> {
                    String acId = a.getCourseId();
                    if (acId != null && !acId.trim().isEmpty()) {
                        return !trainerCourse.equalsIgnoreCase(acId.trim());
                    }
                    for (Student s : studentsList) {
                        if (s.getId().equalsIgnoreCase(a.getStudentId())) {
                            return false;
                        }
                    }
                    return true;
                });
            }

            attendanceList.clear();
            attendanceList.addAll(loaded);

            loadingOverlayAttendance.setVisible(false);
            attendanceTable.setDisable(false);
            addActivity("🔄", "Refreshed " + loaded.size() + " attendance records from Google Sheets");
        });

        loadTask.setOnFailed(e -> {
            Throwable ex = loadTask.getException();
            ex.printStackTrace();

            loadingOverlayAttendance.setVisible(false);
            attendanceTable.setDisable(false);

            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            ex.printStackTrace(pw);
            String stackTrace = sw.toString();
            if (stackTrace.length() > 800) {
                stackTrace = stackTrace.substring(0, 800) + "\n...";
            }

            showErrorAlert("Data Load Error",
                    "Failed to connect to Google Sheets Attendance Sheet",
                    "Error details:\n" + ex.toString() + "\n\nStack Trace:\n" + stackTrace
                            + "\n\nPlease check your network connection and credentials.");
        });

        new Thread(loadTask).start();
    }

    public void refreshCoursesData() {
        if (loadingOverlayCourses.isVisible()) {
            return;
        }
        loadingOverlayCourses.setVisible(true);
        coursesTable.setDisable(true);

        javafx.concurrent.Task<List<Course>> loadTask = new javafx.concurrent.Task<>() {
            @Override
            protected List<Course> call() throws Exception {
                GoogleSheetsService service = new GoogleSheetsService();
                return service.readCourses();
            }
        };

        loadTask.setOnSucceeded(e -> {
            List<Course> loaded = loadTask.getValue();

            String trainerCourse = getTrainerCourseId();
            if (loggedInUser != null && loggedInUser.getRole() == LoggedInUser.Role.STUDENT) {
                loaded.removeIf(c -> {
                    String scId = loggedInUser.getCourseId();
                    if (scId != null && !scId.trim().isEmpty()) {
                        return !scId.equalsIgnoreCase(c.getCourseId());
                    }
                    String des = loggedInUser.getDesignation();
                    if (des != null && des.contains(": ")) {
                        String cName = des.substring(des.indexOf(": ") + 2).trim();
                        return !cName.equalsIgnoreCase(c.getCourseName());
                    }
                    return true;
                });
            } else if (trainerCourse != null) {
                loaded.removeIf(c -> !trainerCourse.equalsIgnoreCase(c.getCourseId()));
            }

            coursesList.clear();
            coursesList.addAll(loaded);

            loadingOverlayCourses.setVisible(false);
            coursesTable.setDisable(false);
            addActivity("🔄", "Refreshed " + loaded.size() + " courses from Google Sheets");
        });

        loadTask.setOnFailed(e -> {
            Throwable ex = loadTask.getException();
            ex.printStackTrace();

            loadingOverlayCourses.setVisible(false);
            coursesTable.setDisable(false);

            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            ex.printStackTrace(pw);
            String stackTrace = sw.toString();
            if (stackTrace.length() > 800) {
                stackTrace = stackTrace.substring(0, 800) + "\n...";
            }

            showErrorAlert("Data Load Error",
                    "Failed to connect to Google Sheets Courses Sheet",
                    "Error details:\n" + ex.toString() + "\n\nStack Trace:\n" + stackTrace
                            + "\n\nPlease check your network connection and credentials.");
        });

        new Thread(loadTask).start();
    }

    public void refreshBatchesData() {
        if (loadingOverlayBatches.isVisible()) {
            return;
        }
        loadingOverlayBatches.setVisible(true);
        batchesTable.setDisable(true);

        javafx.concurrent.Task<List<Batch>> loadTask = new javafx.concurrent.Task<>() {
            @Override
            protected List<Batch> call() throws Exception {
                GoogleSheetsService service = new GoogleSheetsService();
                return service.readBatches();
            }
        };

        loadTask.setOnSucceeded(e -> {
            List<Batch> loaded = loadTask.getValue();

            String trainerCourse = getTrainerCourseId();
            if (loggedInUser != null && loggedInUser.getRole() == LoggedInUser.Role.STUDENT) {
                String studentBatchId = getStudentBatchId();
                if (studentBatchId != null && !studentBatchId.isEmpty()) {
                    loaded.removeIf(b -> !studentBatchId.equalsIgnoreCase(b.getBatchId()));
                }
            } else if (trainerCourse != null) {
                loaded.removeIf(b -> !trainerCourse.equalsIgnoreCase(b.getCourseId()));
            }

            batchesList.clear();
            batchesList.addAll(loaded);

            loadingOverlayBatches.setVisible(false);
            batchesTable.setDisable(false);
            addActivity("🔄", "Refreshed " + loaded.size() + " batches from Google Sheets");
        });

        loadTask.setOnFailed(e -> {
            Throwable ex = loadTask.getException();
            ex.printStackTrace();

            loadingOverlayBatches.setVisible(false);
            batchesTable.setDisable(false);

            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            ex.printStackTrace(pw);
            String stackTrace = sw.toString();
            if (stackTrace.length() > 800) {
                stackTrace = stackTrace.substring(0, 800) + "\n...";
            }

            showErrorAlert("Data Load Error",
                    "Failed to connect to Google Sheets Batches Sheet",
                    "Error details:\n" + ex.toString() + "\n\nStack Trace:\n" + stackTrace
                            + "\n\nPlease check your network connection and credentials.");
        });

        new Thread(loadTask).start();
    }

    public void refreshCompaniesData() {
        if (loadingOverlayCompanies.isVisible()) {
            return;
        }
        loadingOverlayCompanies.setVisible(true);
        companiesTable.setDisable(true);

        javafx.concurrent.Task<List<Company>> loadTask = new javafx.concurrent.Task<>() {
            @Override
            protected List<Company> call() throws Exception {
                GoogleSheetsService service = new GoogleSheetsService();
                return service.readCompanies();
            }
        };

        loadTask.setOnSucceeded(e -> {
            List<Company> loaded = loadTask.getValue();
            companiesList.clear();
            companiesList.addAll(loaded);

            loadingOverlayCompanies.setVisible(false);
            companiesTable.setDisable(false);
            addActivity("🔄", "Refreshed " + loaded.size() + " companies from Google Sheets");
        });

        loadTask.setOnFailed(e -> {
            Throwable ex = loadTask.getException();
            ex.printStackTrace();

            loadingOverlayCompanies.setVisible(false);
            companiesTable.setDisable(false);

            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            ex.printStackTrace(pw);
            String stackTrace = sw.toString();
            if (stackTrace.length() > 800) {
                stackTrace = stackTrace.substring(0, 800) + "\n...";
            }

            showErrorAlert("Data Load Error",
                    "Failed to connect to Google Sheets Companies Sheet",
                    "Error details:\n" + ex.toString() + "\n\nStack Trace:\n" + stackTrace
                            + "\n\nPlease check your network connection and credentials.");
        });

        new Thread(loadTask).start();
    }

    public void refreshPlacementsData() {
        if (loadingOverlayPlacements.isVisible()) {
            return;
        }
        loadingOverlayPlacements.setVisible(true);
        placementsTable.setDisable(true);

        javafx.concurrent.Task<List<StudentPlacement>> loadTask = new javafx.concurrent.Task<>() {
            @Override
            protected List<StudentPlacement> call() throws Exception {
                GoogleSheetsService service = new GoogleSheetsService();
                return service.readPlacements();
            }
        };

        loadTask.setOnSucceeded(e -> {
            List<StudentPlacement> loaded = loadTask.getValue();

            String trainerCourse = getTrainerCourseId();
            if (loggedInUser != null && loggedInUser.getRole() == LoggedInUser.Role.STUDENT) {
                loaded.removeIf(p -> !loggedInUser.getId().equalsIgnoreCase(p.getStudentId())
                        && !loggedInUser.getErpNo().equalsIgnoreCase(p.getErpNo()));
            } else if (trainerCourse != null) {
                loaded.removeIf(p -> {
                    for (Student s : studentsList) {
                        if (s.getId().equalsIgnoreCase(p.getStudentId())
                                || s.getErpNo().equalsIgnoreCase(p.getErpNo())) {
                            return false;
                        }
                    }
                    return true;
                });
            }

            placementsList.clear();
            placementsList.addAll(loaded);

            loadingOverlayPlacements.setVisible(false);
            placementsTable.setDisable(false);
            addActivity("🔄", "Refreshed " + loaded.size() + " placements from Google Sheets");
        });

        loadTask.setOnFailed(e -> {
            Throwable ex = loadTask.getException();
            ex.printStackTrace();

            loadingOverlayPlacements.setVisible(false);
            placementsTable.setDisable(false);

            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            ex.printStackTrace(pw);
            String stackTrace = sw.toString();
            if (stackTrace.length() > 800) {
                stackTrace = stackTrace.substring(0, 800) + "\n...";
            }

            showErrorAlert("Data Load Error",
                    "Failed to connect to Google Sheets Student Placements Sheet",
                    "Error details:\n" + ex.toString() + "\n\nStack Trace:\n" + stackTrace
                            + "\n\nPlease check your network connection and credentials.");
        });

        new Thread(loadTask).start();
    }

    private Stage editorModalStage;

    private void openStudentEditorModal() {
        if (editorModalStage != null && editorModalStage.isShowing()) {
            editorModalStage.toFront();
            return;
        }

        editorModalStage = new Stage();
        editorModalStage.initOwner(this.getScene().getWindow());
        editorModalStage.initModality(Modality.APPLICATION_MODAL);
        editorModalStage.setTitle("Student Details Manager");

        if (formBox.getParent() instanceof Pane) {
            ((Pane) formBox.getParent()).getChildren().remove(formBox);
        }

        formBox.setPrefWidth(450);
        formBox.setMaxWidth(Double.MAX_VALUE);

        if (cbEditorStudentSelect != null) {
            cbEditorStudentSelect.setItems(studentsList);
        }

        VBox modalLayout = new VBox(formBox);
        modalLayout.setPadding(new Insets(10));
        modalLayout.getStyleClass().add("root");

        Scene scene = new Scene(modalLayout, 500, 650);
        java.io.File cssFile = new java.io.File("styles.css");
        if (cssFile.exists()) {
            try {
                scene.getStylesheets().add(cssFile.toURI().toURL().toExternalForm());
            } catch (Exception ignored) {
            }
        }
        editorModalStage.setScene(scene);
        editorModalStage.setResizable(true);
        editorModalStage.show();
    }

    private void showUserProfilePopup() {
        Stage popup = new Stage();
        popup.initOwner(this.getScene().getWindow());
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("User Profile Details");

        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(24));
        layout.getStyleClass().add("content-card");
        layout.setStyle("-fx-background-color: -bg-card; -fx-pref-width: 380px; -fx-pref-height: 420px;");

        // Profile Picture
        StackPane picPane = new StackPane();
        Circle picBg = new Circle(50, Color.web("#F1F5F9"));
        picBg.setStroke(Color.web("#CBD5E1"));
        picBg.setStrokeWidth(2);

        String initialChar = "A";
        if (loggedInUser != null && loggedInUser.getName() != null && !loggedInUser.getName().isEmpty()) {
            initialChar = loggedInUser.getName().substring(0, 1).toUpperCase();
        }
        Text picText = new Text(initialChar);
        picText.setFont(Font.font("System", FontWeight.BOLD, 36));
        picText.setFill(Color.web("#64748B"));
        picPane.getChildren().addAll(picBg, picText);

        if (loggedInUser != null && loggedInUser.getPictureUrl() != null && !loggedInUser.getPictureUrl().isEmpty()) {
            try {
                javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView();
                javafx.scene.image.Image img = new javafx.scene.image.Image(loggedInUser.getPictureUrl(), true);
                imgView.setImage(img);
                imgView.setFitWidth(100);
                imgView.setFitHeight(100);
                imgView.setPreserveRatio(true);
                Circle clip = new Circle(50, 50, 50);
                imgView.setClip(clip);
                img.progressProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal.doubleValue() == 1.0 && !img.isError()) {
                        picText.setVisible(false);
                    }
                });
                picPane.getChildren().add(imgView);
            } catch (Exception e) {
                System.out.println("Could not load popup avatar: " + e.getMessage());
            }
        }

        // Details Grid
        GridPane info = new GridPane();
        info.setHgap(15);
        info.setVgap(10);
        info.setAlignment(Pos.CENTER);

        info.add(new Label("User Name:"), 0, 0);
        Label lblName = new Label(loggedInUser != null ? loggedInUser.getName() : "Admin User");
        lblName.setStyle("-fx-font-weight: bold;");
        info.add(lblName, 1, 0);

        info.add(new Label("Role:"), 0, 1);
        Label lblRole = new Label(loggedInUser != null ? loggedInUser.getRole().toString() : "ADMIN");
        lblRole.setStyle("-fx-font-weight: bold;");
        info.add(lblRole, 1, 1);

        info.add(new Label("User ID:"), 0, 2);
        Label lblId = new Label(loggedInUser != null ? loggedInUser.getId() : "N/A");
        lblId.setStyle("-fx-font-weight: bold;");
        info.add(lblId, 1, 2);

        if (loggedInUser != null && loggedInUser.getRole() == LoggedInUser.Role.STUDENT) {
            info.add(new Label("ERP No:"), 0, 3);
            Label lblErp = new Label(loggedInUser.getErpNo());
            lblErp.setStyle("-fx-font-weight: bold;");
            info.add(lblErp, 1, 3);
        }

        info.add(new Label("Center:"), 0, 4);
        Label lblCenter = new Label(
                loggedInUser != null && loggedInUser.getCenter() != null && !loggedInUser.getCenter().isEmpty()
                        ? loggedInUser.getCenter()
                        : "N/A");
        lblCenter.setStyle("-fx-font-weight: bold;");
        info.add(lblCenter, 1, 4);

        info.add(new Label("Designation:"), 0, 5);
        Label lblDesig = new Label(loggedInUser != null && loggedInUser.getDesignation() != null
                && !loggedInUser.getDesignation().isEmpty() ? loggedInUser.getDesignation() : "N/A");
        lblDesig.setStyle("-fx-font-weight: bold;");
        info.add(lblDesig, 1, 5);

        info.getChildren().forEach(node -> {
            if (node instanceof Label) {
                Label l = (Label) node;
                if (GridPane.getColumnIndex(l) != null && GridPane.getColumnIndex(l) == 0) {
                    l.setStyle("-fx-text-fill: -text-muted; -fx-font-weight: bold;");
                }
            }
        });

        Button btnImageIcon = new Button("🖼️");
        btnImageIcon.setStyle(
                "-fx-font-size: 14px; -fx-padding: 4 8; -fx-background-color: rgba(255,255,255,0.7); -fx-background-radius: 12;");
        btnImageIcon.setCursor(javafx.scene.Cursor.HAND);
        StackPane.setAlignment(btnImageIcon, Pos.BOTTOM_RIGHT);
        picPane.getChildren().add(btnImageIcon);

        btnImageIcon.setOnAction(e -> {
            if (loggedInUser != null && loggedInUser.getPictureUrl() != null
                    && !loggedInUser.getPictureUrl().isEmpty()) {
                Stage photoStage = new Stage();
                photoStage.initOwner(popup);
                photoStage.initModality(Modality.APPLICATION_MODAL);
                photoStage.setTitle(loggedInUser.getName() + " - Photo");

                VBox photoLayout = new VBox(15);
                photoLayout.setAlignment(Pos.CENTER);
                photoLayout.setPadding(new Insets(15));
                photoLayout.setStyle("-fx-background-color: #0F172A;");

                javafx.scene.image.ImageView fullImgView = new javafx.scene.image.ImageView();
                javafx.scene.image.Image img = new javafx.scene.image.Image(loggedInUser.getPictureUrl(), true);
                fullImgView.setImage(img);
                fullImgView.setFitWidth(400);
                fullImgView.setFitHeight(400);
                fullImgView.setPreserveRatio(true);

                Button btnClosePhoto = new Button("Close");
                btnClosePhoto.getStyleClass().addAll("btn", "btn-secondary");
                btnClosePhoto.setOnAction(event -> photoStage.close());

                photoLayout.getChildren().addAll(fullImgView, btnClosePhoto);

                Scene photoScene = new Scene(photoLayout);
                java.io.File fileCss = new java.io.File("styles.css");
                if (fileCss.exists()) {
                    try {
                        photoScene.getStylesheets().add(fileCss.toURI().toURL().toExternalForm());
                    } catch (Exception ignored) {
                    }
                }
                photoStage.setScene(photoScene);
                photoStage.showAndWait();
            }
        });

        HBox actionBox = new HBox(15);
        actionBox.setAlignment(Pos.CENTER);

        Button btnClose = new Button("Close Profile");
        btnClose.getStyleClass().addAll("btn", "btn-secondary");
        btnClose.setOnAction(e -> popup.close());

        Button btnLogout = new Button("Logout");
        btnLogout.getStyleClass().addAll("btn", "btn-danger");
        btnLogout.setOnAction(e -> {
            popup.close();
            try {
                Scene currentScene = this.getScene();
                Stage stage = (Stage) currentScene.getWindow();

                LoginView loginRoot = new LoginView(user -> {
                    try {
                        DashboardView dashboardRoot = new DashboardView(user);
                        stage.getScene().setRoot(dashboardRoot);
                        stage.setMinWidth(1200);
                        stage.setMinHeight(650);
                        stage.setWidth(1250);
                        stage.setHeight(720);
                        stage.centerOnScreen();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                currentScene.setRoot(loginRoot);
                stage.setTitle("YouvakendraSM - Login");
                stage.setMinWidth(500);
                stage.setMinHeight(600);
                stage.setWidth(600);
                stage.setHeight(650);
                stage.centerOnScreen();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        actionBox.getChildren().addAll(btnClose, btnLogout);

        layout.getChildren().addAll(picPane, info, actionBox);

        Scene scene = new Scene(layout, 380, 420);
        java.io.File cssFile = new java.io.File("styles.css");
        if (cssFile.exists()) {
            try {
                scene.getStylesheets().add(cssFile.toURI().toURL().toExternalForm());
            } catch (Exception ignored) {
            }
        }
        popup.setScene(scene);
        popup.setResizable(false);
        popup.showAndWait();
    }

    private String getTrainerCourseId() {
        if (loggedInUser == null || loggedInUser.getRole() != LoggedInUser.Role.TRAINER) {
            return null;
        }
        String userCourseId = loggedInUser.getCourseId();
        if (userCourseId != null && !userCourseId.trim().isEmpty()) {
            return userCourseId.trim();
        }
        String desig = loggedInUser.getDesignation();
        if (desig == null || desig.isEmpty()) {
            return null;
        }

        // If coursesList is already populated, check if any course ID is in the
        // designation
        if (coursesList != null && !coursesList.isEmpty()) {
            for (Course course : coursesList) {
                String cId = course.getCourseId();
                if (cId != null && !cId.isEmpty()) {
                    String regex = "\\b" + java.util.regex.Pattern.quote(cId) + "\\b";
                    java.util.regex.Pattern p = java.util.regex.Pattern.compile(regex,
                            java.util.regex.Pattern.CASE_INSENSITIVE);
                    if (p.matcher(desig).find()) {
                        return cId;
                    }
                }
            }
            // containment check fallback
            for (Course course : coursesList) {
                String cId = course.getCourseId();
                if (cId != null && !cId.isEmpty()) {
                    if (desig.toLowerCase().contains(cId.toLowerCase())) {
                        return cId;
                    }
                }
            }
        }

        // tokenization fallback, excluding common role/designation words
        List<String> exclusions = List.of("trainer", "teacher", "instructor", "staff", "admin", "viewer", "center", "designation", "professor", "faculty", "youvakendra");
        String[] tokens = desig.split("[\\s\\-_,:/]+");
        for (String token : tokens) {
            if (token.length() >= 2 && token.length() <= 8) {
                if (token.matches("[A-Za-z0-9]+") && !exclusions.contains(token.toLowerCase())) {
                    return token.toUpperCase();
                }
            }
        }

        return desig;
    }

    private String getStudentBatchId() {
        if (loggedInUser == null || loggedInUser.getRole() != LoggedInUser.Role.STUDENT) {
            return null;
        }
        for (Student s : studentsList) {
            if (s.getId().equalsIgnoreCase(loggedInUser.getId())) {
                return s.getBatchId();
            }
        }
        return null;
    }

    private void refreshAllData() {
        fetchDataForDashboard();
        fetchStudentsFromGoogleSheets();
        refreshCoursesData();
        refreshBatchesData();
        refreshCompaniesData();
        refreshPlacementsData();
    }

    private void openCoursesEditorModal() {
        Stage modalStage = new Stage();
        modalStage.initOwner(this.getScene().getWindow());
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Course Manager");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.getStyleClass().add("content-card");
        layout.setStyle("-fx-background-color: -bg-app; -fx-pref-width: 420px;");

        Label lblTitle = new Label("Course Details Editor");
        lblTitle.getStyleClass().add("card-title");

        HBox selectorRow = new HBox(10);
        selectorRow.setAlignment(Pos.CENTER_LEFT);
        Label lblSelect = new Label("Select Course:");
        lblSelect.getStyleClass().add("form-label");
        ComboBox<Course> cbSelect = new ComboBox<>();
        cbSelect.setPromptText("Choose Course to Edit/Delete");
        cbSelect.setItems(coursesList);
        cbSelect.setPrefWidth(220);
        cbSelect.getStyleClass().add("combo-box");
        cbSelect.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Course c, boolean empty) {
                super.updateItem(c, empty);
                setText((empty || c == null) ? null : c.getCourseName() + " (" + c.getCourseId() + ")");
            }
        });
        cbSelect.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Course c, boolean empty) {
                super.updateItem(c, empty);
                setText((empty || c == null) ? "Choose Course to Edit/Delete"
                        : c.getCourseName() + " (" + c.getCourseId() + ")");
            }
        });
        selectorRow.getChildren().addAll(lblSelect, cbSelect);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(10, 0, 10, 0));

        Label lblId = new Label("Course ID:");
        lblId.getStyleClass().add("form-label");
        TextField txtId = new TextField();
        txtId.setPromptText("e.g. DM");
        txtId.getStyleClass().add("text-input");
        grid.add(lblId, 0, 0);
        grid.add(txtId, 1, 0);

        Label lblName = new Label("Course Name:");
        lblName.getStyleClass().add("form-label");
        TextField txtName = new TextField();
        txtName.setPromptText("e.g. Digital Marketing");
        txtName.getStyleClass().add("text-input");
        grid.add(lblName, 0, 1);
        grid.add(txtName, 1, 1);

        Label lblMsg = new Label();
        lblMsg.setStyle("-fx-font-weight: bold;");

        HBox btnBox = new HBox(8);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new Insets(10, 0, 0, 0));

        Button btnAdd = new Button("Add");
        btnAdd.getStyleClass().addAll("btn", "btn-primary");

        Button btnUpdate = new Button("Update");
        btnUpdate.getStyleClass().addAll("btn", "btn-success");
        btnUpdate.setDisable(true);

        Button btnDelete = new Button("Delete");
        btnDelete.getStyleClass().addAll("btn", "btn-danger");
        btnDelete.setDisable(true);

        Button btnClear = new Button("Clear");
        btnClear.getStyleClass().addAll("btn", "btn-secondary");

        Button btnClose = new Button("Close");
        btnClose.getStyleClass().addAll("btn", "btn-secondary");
        btnClose.setOnAction(e -> modalStage.close());

        btnBox.getChildren().addAll(btnAdd, btnUpdate, btnDelete, btnClear, btnClose);

        cbSelect.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtId.setText(newVal.getCourseId());
                txtId.setEditable(false);
                txtId.setDisable(true);
                txtName.setText(newVal.getCourseName());
                btnAdd.setDisable(true);
                btnUpdate.setDisable(false);
                btnDelete.setDisable(false);
            }
        });

        Runnable resetForm = () -> {
            cbSelect.setValue(null);
            txtId.clear();
            txtId.setEditable(true);
            txtId.setDisable(false);
            txtName.clear();
            btnAdd.setDisable(false);
            btnUpdate.setDisable(true);
            btnDelete.setDisable(true);
            lblMsg.setText("");
        };
        btnClear.setOnAction(e -> resetForm.run());

        btnAdd.setOnAction(e -> {
            String cid = txtId.getText().trim();
            String cname = txtName.getText().trim();
            if (cid.isEmpty() || cname.isEmpty()) {
                lblMsg.setText("Fields cannot be empty.");
                lblMsg.setStyle("-fx-text-fill: -danger-color;");
                return;
            }
            for (Course course : coursesList) {
                if (course.getCourseId().equalsIgnoreCase(cid)) {
                    lblMsg.setText("Course ID already exists!");
                    lblMsg.setStyle("-fx-text-fill: -danger-color;");
                    return;
                }
            }

            layout.setDisable(true);
            lblMsg.setText("Adding course...");
            lblMsg.setStyle("-fx-text-fill: -primary-color;");

            Course newCourse = new Course(cid, cname);
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    new GoogleSheetsService().addCourse(newCourse);
                    return null;
                }
            };
            task.setOnSucceeded(event -> {
                layout.setDisable(false);
                coursesList.add(newCourse);
                addActivity("➕",
                        "Added new course: " + newCourse.getCourseName() + " (" + newCourse.getCourseId() + ")");
                resetForm.run();
                lblMsg.setText("Course added successfully.");
                lblMsg.setStyle("-fx-text-fill: -success-color;");
                refreshCoursesData();
            });
            task.setOnFailed(event -> {
                layout.setDisable(false);
                Throwable t = task.getException();
                lblMsg.setText("Failed: " + t.getMessage());
                lblMsg.setStyle("-fx-text-fill: -danger-color;");
            });
            new Thread(task).start();
        });

        btnUpdate.setOnAction(e -> {
            Course selected = cbSelect.getValue();
            if (selected == null)
                return;
            String cname = txtName.getText().trim();
            if (cname.isEmpty()) {
                lblMsg.setText("Course Name cannot be empty.");
                lblMsg.setStyle("-fx-text-fill: -danger-color;");
                return;
            }

            layout.setDisable(true);
            lblMsg.setText("Updating course...");
            lblMsg.setStyle("-fx-text-fill: -primary-color;");

            Course updatedCourse = new Course(selected.getCourseId(), cname);
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    new GoogleSheetsService().updateCourse(updatedCourse);
                    return null;
                }
            };
            task.setOnSucceeded(event -> {
                layout.setDisable(false);
                selected.setCourseName(cname);
                addActivity("📝", "Updated course: " + cname + " (" + selected.getCourseId() + ")");
                resetForm.run();
                lblMsg.setText("Course updated successfully.");
                lblMsg.setStyle("-fx-text-fill: -success-color;");
                refreshCoursesData();
            });
            task.setOnFailed(event -> {
                layout.setDisable(false);
                Throwable t = task.getException();
                lblMsg.setText("Failed: " + t.getMessage());
                lblMsg.setStyle("-fx-text-fill: -danger-color;");
            });
            new Thread(task).start();
        });

        btnDelete.setOnAction(e -> {
            Course selected = cbSelect.getValue();
            if (selected == null)
                return;

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Delete");
            confirm.setHeaderText("Delete course " + selected.getCourseName() + "?");
            confirm.setContentText("This will permanently remove the course. Continue?");
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }

            layout.setDisable(true);
            lblMsg.setText("Deleting course...");
            lblMsg.setStyle("-fx-text-fill: -danger-color;");

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    new GoogleSheetsService().deleteCourse(selected.getCourseId());
                    return null;
                }
            };
            task.setOnSucceeded(event -> {
                layout.setDisable(false);
                coursesList.remove(selected);
                addActivity("❌", "Deleted course: " + selected.getCourseName() + " (" + selected.getCourseId() + ")");
                resetForm.run();
                lblMsg.setText("Course deleted successfully.");
                lblMsg.setStyle("-fx-text-fill: -success-color;");
                refreshCoursesData();
            });
            task.setOnFailed(event -> {
                layout.setDisable(false);
                Throwable t = task.getException();
                lblMsg.setText("Failed: " + t.getMessage());
                lblMsg.setStyle("-fx-text-fill: -danger-color;");
            });
            new Thread(task).start();
        });

        layout.getChildren().addAll(lblTitle, selectorRow, grid, lblMsg, btnBox);

        Scene scene = new Scene(layout);
        java.io.File cssFile = new java.io.File("styles.css");
        if (cssFile.exists()) {
            try {
                scene.getStylesheets().add(cssFile.toURI().toURL().toExternalForm());
            } catch (Exception ignored) {
            }
        }
        modalStage.setScene(scene);
        modalStage.setResizable(false);
        modalStage.show();
    }

    private void openBatchesEditorModal() {
        Stage modalStage = new Stage();
        modalStage.initOwner(this.getScene().getWindow());
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Batch Manager");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.getStyleClass().add("content-card");
        layout.setStyle("-fx-background-color: -bg-app; -fx-pref-width: 450px;");

        Label lblTitle = new Label("Batch Details Editor");
        lblTitle.getStyleClass().add("card-title");

        HBox selectorRow = new HBox(10);
        selectorRow.setAlignment(Pos.CENTER_LEFT);
        Label lblSelect = new Label("Select Batch:");
        lblSelect.getStyleClass().add("form-label");
        ComboBox<Batch> cbSelect = new ComboBox<>();
        cbSelect.setPromptText("Choose Batch to Edit/Delete");
        cbSelect.setItems(batchesList);
        cbSelect.setPrefWidth(250);
        cbSelect.getStyleClass().add("combo-box");
        cbSelect.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Batch b, boolean empty) {
                super.updateItem(b, empty);
                setText((empty || b == null) ? null : b.getBatchNo() + " (" + b.getBatchId() + ")");
            }
        });
        cbSelect.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Batch b, boolean empty) {
                super.updateItem(b, empty);
                setText((empty || b == null) ? "Choose Batch to Edit/Delete"
                        : b.getBatchNo() + " (" + b.getBatchId() + ")");
            }
        });
        selectorRow.getChildren().addAll(lblSelect, cbSelect);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(10, 0, 10, 0));

        Label lblId = new Label("Batch ID:");
        lblId.getStyleClass().add("form-label");
        TextField txtId = new TextField();
        txtId.setPromptText("e.g. B1");
        txtId.getStyleClass().add("text-input");
        grid.add(lblId, 0, 0);
        grid.add(txtId, 1, 0);

        Label lblCourse = new Label("Course ID:");
        lblCourse.getStyleClass().add("form-label");
        ComboBox<Course> cbCourseSel = new ComboBox<>();
        cbCourseSel.setPromptText("Select Course");
        cbCourseSel.setItems(coursesList);
        cbCourseSel.getStyleClass().add("combo-box");
        cbCourseSel.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Course c, boolean empty) {
                super.updateItem(c, empty);
                setText((empty || c == null) ? null : c.getCourseName() + " (" + c.getCourseId() + ")");
            }
        });
        cbCourseSel.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Course c, boolean empty) {
                super.updateItem(c, empty);
                setText((empty || c == null) ? "Select Course" : c.getCourseName() + " (" + c.getCourseId() + ")");
            }
        });
        grid.add(lblCourse, 0, 1);
        grid.add(cbCourseSel, 1, 1);

        Label lblBatchNo = new Label("Batch No:");
        lblBatchNo.getStyleClass().add("form-label");
        TextField txtBatchNo = new TextField();
        txtBatchNo.setPromptText("e.g. Batch 1");
        txtBatchNo.getStyleClass().add("text-input");
        grid.add(lblBatchNo, 0, 2);
        grid.add(txtBatchNo, 1, 2);

        Label lblStartDate = new Label("Start Date:");
        lblStartDate.getStyleClass().add("form-label");
        DatePicker dpStartDate = new DatePicker();
        dpStartDate.getStyleClass().add("date-picker");
        grid.add(lblStartDate, 0, 3);
        grid.add(dpStartDate, 1, 3);

        Label lblEndDate = new Label("End Date:");
        lblEndDate.getStyleClass().add("form-label");
        DatePicker dpEndDate = new DatePicker();
        dpEndDate.getStyleClass().add("date-picker");
        grid.add(lblEndDate, 0, 4);
        grid.add(dpEndDate, 1, 4);

        Label lblBatchTime = new Label("Batch Time:");
        lblBatchTime.getStyleClass().add("form-label");
        TextField txtBatchTime = new TextField();
        txtBatchTime.setPromptText("e.g. 09:00 AM - 11:00 AM");
        txtBatchTime.getStyleClass().add("text-input");
        grid.add(lblBatchTime, 0, 5);
        grid.add(txtBatchTime, 1, 5);

        Label lblMsg = new Label();
        lblMsg.setStyle("-fx-font-weight: bold;");

        HBox btnBox = new HBox(8);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new Insets(10, 0, 0, 0));

        Button btnAdd = new Button("Add");
        btnAdd.getStyleClass().addAll("btn", "btn-primary");

        Button btnUpdate = new Button("Update");
        btnUpdate.getStyleClass().addAll("btn", "btn-success");
        btnUpdate.setDisable(true);

        Button btnDelete = new Button("Delete");
        btnDelete.getStyleClass().addAll("btn", "btn-danger");
        btnDelete.setDisable(true);

        Button btnClear = new Button("Clear");
        btnClear.getStyleClass().addAll("btn", "btn-secondary");

        Button btnClose = new Button("Close");
        btnClose.getStyleClass().addAll("btn", "btn-secondary");
        btnClose.setOnAction(e -> modalStage.close());

        btnBox.getChildren().addAll(btnAdd, btnUpdate, btnDelete, btnClear, btnClose);

        cbSelect.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtId.setText(newVal.getBatchId());
                txtId.setEditable(false);
                txtId.setDisable(true);

                Course matchedCourse = null;
                for (Course c : coursesList) {
                    if (c.getCourseId().equalsIgnoreCase(newVal.getCourseId())) {
                        matchedCourse = c;
                        break;
                    }
                }
                cbCourseSel.setValue(matchedCourse);
                txtBatchNo.setText(newVal.getBatchNo());
                try {
                    dpStartDate.setValue(LocalDate.parse(newVal.getStartDate(), DATE_FORMATTER));
                } catch (Exception ex) {
                    dpStartDate.setValue(null);
                }
                try {
                    dpEndDate.setValue(LocalDate.parse(newVal.getEndDate(), DATE_FORMATTER));
                } catch (Exception ex) {
                    dpEndDate.setValue(null);
                }
                txtBatchTime.setText(newVal.getBatchTime());

                btnAdd.setDisable(true);
                btnUpdate.setDisable(false);
                btnDelete.setDisable(false);
            }
        });

        Runnable resetForm = () -> {
            cbSelect.setValue(null);
            txtId.clear();
            txtId.setEditable(true);
            txtId.setDisable(false);
            cbCourseSel.setValue(null);
            txtBatchNo.clear();
            dpStartDate.setValue(null);
            dpEndDate.setValue(null);
            txtBatchTime.clear();

            btnAdd.setDisable(false);
            btnUpdate.setDisable(true);
            btnDelete.setDisable(true);
            lblMsg.setText("");
        };
        btnClear.setOnAction(e -> resetForm.run());

        btnAdd.setOnAction(e -> {
            String bid = txtId.getText().trim();
            Course course = cbCourseSel.getValue();
            String bno = txtBatchNo.getText().trim();
            String btime = txtBatchTime.getText().trim();

            if (bid.isEmpty() || course == null || bno.isEmpty() || dpStartDate.getValue() == null
                    || dpEndDate.getValue() == null || btime.isEmpty()) {
                lblMsg.setText("All fields are required.");
                lblMsg.setStyle("-fx-text-fill: -danger-color;");
                return;
            }

            for (Batch batch : batchesList) {
                if (batch.getBatchId().equalsIgnoreCase(bid)) {
                    lblMsg.setText("Batch ID already exists!");
                    lblMsg.setStyle("-fx-text-fill: -danger-color;");
                    return;
                }
            }

            layout.setDisable(true);
            lblMsg.setText("Adding batch...");
            lblMsg.setStyle("-fx-text-fill: -primary-color;");

            String sdate = dpStartDate.getValue().format(DATE_FORMATTER);
            String edate = dpEndDate.getValue().format(DATE_FORMATTER);
            Batch newBatch = new Batch(bid, course.getCourseId(), bno, sdate, edate, btime);

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    new GoogleSheetsService().addBatch(newBatch);
                    return null;
                }
            };
            task.setOnSucceeded(event -> {
                layout.setDisable(false);
                batchesList.add(newBatch);
                addActivity("➕", "Added new batch: " + newBatch.getBatchNo() + " (" + newBatch.getBatchId() + ")");
                resetForm.run();
                lblMsg.setText("Batch added successfully.");
                lblMsg.setStyle("-fx-text-fill: -success-color;");
                refreshBatchesData();
            });
            task.setOnFailed(event -> {
                layout.setDisable(false);
                Throwable t = task.getException();
                lblMsg.setText("Failed: " + t.getMessage());
                lblMsg.setStyle("-fx-text-fill: -danger-color;");
            });
            new Thread(task).start();
        });

        btnUpdate.setOnAction(e -> {
            Batch selected = cbSelect.getValue();
            if (selected == null)
                return;
            Course course = cbCourseSel.getValue();
            String bno = txtBatchNo.getText().trim();
            String btime = txtBatchTime.getText().trim();

            if (course == null || bno.isEmpty() || dpStartDate.getValue() == null || dpEndDate.getValue() == null
                    || btime.isEmpty()) {
                lblMsg.setText("All fields are required.");
                lblMsg.setStyle("-fx-text-fill: -danger-color;");
                return;
            }

            layout.setDisable(true);
            lblMsg.setText("Updating batch...");
            lblMsg.setStyle("-fx-text-fill: -primary-color;");

            String sdate = dpStartDate.getValue().format(DATE_FORMATTER);
            String edate = dpEndDate.getValue().format(DATE_FORMATTER);
            Batch updatedBatch = new Batch(selected.getBatchId(), course.getCourseId(), bno, sdate, edate, btime);

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    new GoogleSheetsService().updateBatch(updatedBatch);
                    return null;
                }
            };
            task.setOnSucceeded(event -> {
                layout.setDisable(false);
                selected.setCourseId(course.getCourseId());
                selected.setBatchNo(bno);
                selected.setStartDate(sdate);
                selected.setEndDate(edate);
                selected.setBatchTime(btime);

                addActivity("📝", "Updated batch: " + bno + " (" + selected.getBatchId() + ")");
                resetForm.run();
                lblMsg.setText("Batch updated successfully.");
                lblMsg.setStyle("-fx-text-fill: -success-color;");
                refreshBatchesData();
            });
            task.setOnFailed(event -> {
                layout.setDisable(false);
                Throwable t = task.getException();
                lblMsg.setText("Failed: " + t.getMessage());
                lblMsg.setStyle("-fx-text-fill: -danger-color;");
            });
            new Thread(task).start();
        });

        btnDelete.setOnAction(e -> {
            Batch selected = cbSelect.getValue();
            if (selected == null)
                return;

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Delete");
            confirm.setHeaderText("Delete batch " + selected.getBatchNo() + "?");
            confirm.setContentText("This will permanently remove the batch. Continue?");
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }

            layout.setDisable(true);
            lblMsg.setText("Deleting batch...");
            lblMsg.setStyle("-fx-text-fill: -danger-color;");

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    new GoogleSheetsService().deleteBatch(selected.getBatchId());
                    return null;
                }
            };
            task.setOnSucceeded(event -> {
                layout.setDisable(false);
                batchesList.remove(selected);
                addActivity("❌", "Deleted batch: " + selected.getBatchNo() + " (" + selected.getBatchId() + ")");
                resetForm.run();
                lblMsg.setText("Batch deleted successfully.");
                lblMsg.setStyle("-fx-text-fill: -success-color;");
                refreshBatchesData();
            });
            task.setOnFailed(event -> {
                layout.setDisable(false);
                Throwable t = task.getException();
                lblMsg.setText("Failed: " + t.getMessage());
                lblMsg.setStyle("-fx-text-fill: -danger-color;");
            });
            new Thread(task).start();
        });

        layout.getChildren().addAll(lblTitle, selectorRow, grid, lblMsg, btnBox);

        Scene scene = new Scene(layout);
        java.io.File cssFile = new java.io.File("styles.css");
        if (cssFile.exists()) {
            try {
                scene.getStylesheets().add(cssFile.toURI().toURL().toExternalForm());
            } catch (Exception ignored) {
            }
        }
        modalStage.setScene(scene);
        modalStage.setResizable(false);
        modalStage.show();
    }

    private void openCompaniesEditorModal() {
        Stage modalStage = new Stage();
        modalStage.initOwner(this.getScene().getWindow());
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Company Manager");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.getStyleClass().add("content-card");
        layout.setStyle("-fx-background-color: -bg-app; -fx-pref-width: 480px;");

        Label lblTitle = new Label("Company Details Editor");
        lblTitle.getStyleClass().add("card-title");

        HBox selectorRow = new HBox(10);
        selectorRow.setAlignment(Pos.CENTER_LEFT);
        Label lblSelect = new Label("Select Company:");
        lblSelect.getStyleClass().add("form-label");
        ComboBox<Company> cbSelect = new ComboBox<>();
        cbSelect.setPromptText("Choose Company to Edit/Delete");
        cbSelect.setItems(companiesList);
        cbSelect.setPrefWidth(280);
        cbSelect.getStyleClass().add("combo-box");
        cbSelect.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Company c, boolean empty) {
                super.updateItem(c, empty);
                setText((empty || c == null) ? null : c.getCompanyName() + " (" + c.getCompanyId() + ")");
            }
        });
        cbSelect.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Company c, boolean empty) {
                super.updateItem(c, empty);
                setText((empty || c == null) ? "Choose Company to Edit/Delete"
                        : c.getCompanyName() + " (" + c.getCompanyId() + ")");
            }
        });
        selectorRow.getChildren().addAll(lblSelect, cbSelect);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(10, 0, 10, 0));

        Label lblId = new Label("Company ID:");
        lblId.getStyleClass().add("form-label");
        TextField txtId = new TextField();
        txtId.setPromptText("e.g. CO1");
        txtId.getStyleClass().add("text-input");
        grid.add(lblId, 0, 0);
        grid.add(txtId, 1, 0);

        Label lblName = new Label("Company Name:");
        lblName.getStyleClass().add("form-label");
        TextField txtName = new TextField();
        txtName.setPromptText("e.g. Google India");
        txtName.getStyleClass().add("text-input");
        grid.add(lblName, 0, 1);
        grid.add(txtName, 1, 1);

        Label lblHr = new Label("HR Name:");
        lblHr.getStyleClass().add("form-label");
        TextField txtHr = new TextField();
        txtHr.setPromptText("e.g. Jane Doe");
        txtHr.getStyleClass().add("text-input");
        grid.add(lblHr, 0, 2);
        grid.add(txtHr, 1, 2);

        Label lblAddress = new Label("Address:");
        lblAddress.getStyleClass().add("form-label");
        TextField txtAddress = new TextField();
        txtAddress.setPromptText("e.g. Mumbai, Maharashtra");
        txtAddress.getStyleClass().add("text-input");
        grid.add(lblAddress, 0, 3);
        grid.add(txtAddress, 1, 3);

        Label lblContact = new Label("Contact Info:");
        lblContact.getStyleClass().add("form-label");
        TextField txtContact = new TextField();
        txtContact.setPromptText("e.g. hr@google.com");
        txtContact.getStyleClass().add("text-input");
        grid.add(lblContact, 0, 4);
        grid.add(txtContact, 1, 4);

        Label lblMsg = new Label();
        lblMsg.setStyle("-fx-font-weight: bold;");

        HBox btnBox = new HBox(8);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new Insets(10, 0, 0, 0));

        Button btnAdd = new Button("Add");
        btnAdd.getStyleClass().addAll("btn", "btn-primary");

        Button btnUpdate = new Button("Update");
        btnUpdate.getStyleClass().addAll("btn", "btn-success");
        btnUpdate.setDisable(true);

        Button btnDelete = new Button("Delete");
        btnDelete.getStyleClass().addAll("btn", "btn-danger");
        btnDelete.setDisable(true);

        Button btnClear = new Button("Clear");
        btnClear.getStyleClass().addAll("btn", "btn-secondary");

        Button btnClose = new Button("Close");
        btnClose.getStyleClass().addAll("btn", "btn-secondary");
        btnClose.setOnAction(e -> modalStage.close());

        btnBox.getChildren().addAll(btnAdd, btnUpdate, btnDelete, btnClear, btnClose);

        cbSelect.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtId.setText(newVal.getCompanyId());
                txtId.setEditable(false);
                txtId.setDisable(true);
                txtName.setText(newVal.getCompanyName());
                txtHr.setText(newVal.getHrName());
                txtAddress.setText(newVal.getCompanyAddress());
                txtContact.setText(newVal.getContactInfo());

                btnAdd.setDisable(true);
                btnUpdate.setDisable(false);
                btnDelete.setDisable(false);
            }
        });

        Runnable resetForm = () -> {
            cbSelect.setValue(null);
            txtId.clear();
            txtId.setEditable(true);
            txtId.setDisable(false);
            txtName.clear();
            txtHr.clear();
            txtAddress.clear();
            txtContact.clear();

            btnAdd.setDisable(false);
            btnUpdate.setDisable(true);
            btnDelete.setDisable(true);
            lblMsg.setText("");
        };
        btnClear.setOnAction(e -> resetForm.run());

        btnAdd.setOnAction(e -> {
            String cid = txtId.getText().trim();
            String cname = txtName.getText().trim();
            String hr = txtHr.getText().trim();
            String addr = txtAddress.getText().trim();
            String cont = txtContact.getText().trim();

            if (cid.isEmpty() || cname.isEmpty() || hr.isEmpty() || addr.isEmpty() || cont.isEmpty()) {
                lblMsg.setText("All fields are required.");
                lblMsg.setStyle("-fx-text-fill: -danger-color;");
                return;
            }

            for (Company comp : companiesList) {
                if (comp.getCompanyId().equalsIgnoreCase(cid)) {
                    lblMsg.setText("Company ID already exists!");
                    lblMsg.setStyle("-fx-text-fill: -danger-color;");
                    return;
                }
            }

            layout.setDisable(true);
            lblMsg.setText("Adding company...");
            lblMsg.setStyle("-fx-text-fill: -primary-color;");

            Company newComp = new Company(cid, cname, hr, addr, cont);

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    new GoogleSheetsService().addCompany(newComp);
                    return null;
                }
            };
            task.setOnSucceeded(event -> {
                layout.setDisable(false);
                companiesList.add(newComp);
                addActivity("➕",
                        "Added new company: " + newComp.getCompanyName() + " (" + newComp.getCompanyId() + ")");
                resetForm.run();
                lblMsg.setText("Company added successfully.");
                lblMsg.setStyle("-fx-text-fill: -success-color;");
                refreshCompaniesData();
            });
            task.setOnFailed(event -> {
                layout.setDisable(false);
                Throwable t = task.getException();
                lblMsg.setText("Failed: " + t.getMessage());
                lblMsg.setStyle("-fx-text-fill: -danger-color;");
            });
            new Thread(task).start();
        });

        btnUpdate.setOnAction(e -> {
            Company selected = cbSelect.getValue();
            if (selected == null)
                return;
            String cname = txtName.getText().trim();
            String hr = txtHr.getText().trim();
            String addr = txtAddress.getText().trim();
            String cont = txtContact.getText().trim();

            if (cname.isEmpty() || hr.isEmpty() || addr.isEmpty() || cont.isEmpty()) {
                lblMsg.setText("All fields are required.");
                lblMsg.setStyle("-fx-text-fill: -danger-color;");
                return;
            }

            layout.setDisable(true);
            lblMsg.setText("Updating company...");
            lblMsg.setStyle("-fx-text-fill: -primary-color;");

            Company updatedComp = new Company(selected.getCompanyId(), cname, hr, addr, cont);

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    new GoogleSheetsService().updateCompany(updatedComp);
                    return null;
                }
            };
            task.setOnSucceeded(event -> {
                layout.setDisable(false);
                selected.setCompanyName(cname);
                selected.setHrName(hr);
                selected.setCompanyAddress(addr);
                selected.setContactInfo(cont);

                addActivity("📝", "Updated company: " + cname + " (" + selected.getCompanyId() + ")");
                resetForm.run();
                lblMsg.setText("Company updated successfully.");
                lblMsg.setStyle("-fx-text-fill: -success-color;");
                refreshCompaniesData();
            });
            task.setOnFailed(event -> {
                layout.setDisable(false);
                Throwable t = task.getException();
                lblMsg.setText("Failed: " + t.getMessage());
                lblMsg.setStyle("-fx-text-fill: -danger-color;");
            });
            new Thread(task).start();
        });

        btnDelete.setOnAction(e -> {
            Company selected = cbSelect.getValue();
            if (selected == null)
                return;

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Delete");
            confirm.setHeaderText("Delete company " + selected.getCompanyName() + "?");
            confirm.setContentText("This will permanently remove the company. Continue?");
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }

            layout.setDisable(true);
            lblMsg.setText("Deleting company...");
            lblMsg.setStyle("-fx-text-fill: -danger-color;");

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    new GoogleSheetsService().deleteCompany(selected.getCompanyId());
                    return null;
                }
            };
            task.setOnSucceeded(event -> {
                layout.setDisable(false);
                companiesList.remove(selected);
                addActivity("❌",
                        "Deleted company: " + selected.getCompanyName() + " (" + selected.getCompanyId() + ")");
                resetForm.run();
                lblMsg.setText("Company deleted successfully.");
                lblMsg.setStyle("-fx-text-fill: -success-color;");
                refreshCompaniesData();
            });
            task.setOnFailed(event -> {
                layout.setDisable(false);
                Throwable t = task.getException();
                lblMsg.setText("Failed: " + t.getMessage());
                lblMsg.setStyle("-fx-text-fill: -danger-color;");
            });
            new Thread(task).start();
        });

        layout.getChildren().addAll(lblTitle, selectorRow, grid, lblMsg, btnBox);

        Scene scene = new Scene(layout);
        java.io.File cssFile = new java.io.File("styles.css");
        if (cssFile.exists()) {
            try {
                scene.getStylesheets().add(cssFile.toURI().toURL().toExternalForm());
            } catch (Exception ignored) {
            }
        }
        modalStage.setScene(scene);
        modalStage.setResizable(false);
        modalStage.show();
    }

    private void openPlacementsEditorModal() {
        Stage modalStage = new Stage();
        modalStage.initOwner(this.getScene().getWindow());
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Placement Manager");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.getStyleClass().add("content-card");
        layout.setStyle("-fx-background-color: -bg-app; -fx-pref-width: 480px;");

        Label lblTitle = new Label("Placement Details Editor");
        lblTitle.getStyleClass().add("card-title");

        HBox selectorRow = new HBox(10);
        selectorRow.setAlignment(Pos.CENTER_LEFT);
        Label lblSelect = new Label("Select Placement:");
        lblSelect.getStyleClass().add("form-label");
        ComboBox<StudentPlacement> cbSelect = new ComboBox<>();
        cbSelect.setPromptText("Choose Placement to Edit/Delete");
        cbSelect.setItems(placementsList);
        cbSelect.setPrefWidth(280);
        cbSelect.getStyleClass().add("combo-box");
        cbSelect.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(StudentPlacement p, boolean empty) {
                super.updateItem(p, empty);
                setText((empty || p == null) ? null : p.getPlacementId() + " - Student ID: " + p.getStudentId());
            }
        });
        cbSelect.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(StudentPlacement p, boolean empty) {
                super.updateItem(p, empty);
                setText((empty || p == null) ? "Choose Placement to Edit/Delete"
                        : p.getPlacementId() + " - Student ID: " + p.getStudentId());
            }
        });
        selectorRow.getChildren().addAll(lblSelect, cbSelect);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(10, 0, 10, 0));

        Label lblId = new Label("Placement ID:");
        lblId.getStyleClass().add("form-label");
        TextField txtId = new TextField();
        txtId.setPromptText("e.g. PL1");
        txtId.getStyleClass().add("text-input");
        grid.add(lblId, 0, 0);
        grid.add(txtId, 1, 0);

        Label lblStudent = new Label("Student:");
        lblStudent.getStyleClass().add("form-label");
        ComboBox<Student> cbStudentSel = new ComboBox<>();
        cbStudentSel.setPromptText("Select Student");
        cbStudentSel.setItems(studentsList);
        cbStudentSel.getStyleClass().add("combo-box");
        cbStudentSel.setPrefWidth(250);
        cbStudentSel.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Student s, boolean empty) {
                super.updateItem(s, empty);
                setText((empty || s == null) ? null : s.getName() + " (" + s.getId() + ")");
            }
        });
        cbStudentSel.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Student s, boolean empty) {
                super.updateItem(s, empty);
                setText((empty || s == null) ? "Select Student" : s.getName() + " (" + s.getId() + ")");
            }
        });
        grid.add(lblStudent, 0, 1);
        grid.add(cbStudentSel, 1, 1);

        Label lblErp = new Label("ERP No:");
        lblErp.getStyleClass().add("form-label");
        TextField txtErp = new TextField();
        txtErp.setEditable(false);
        txtErp.setDisable(true);
        txtErp.getStyleClass().add("text-input");
        grid.add(lblErp, 0, 2);
        grid.add(txtErp, 1, 2);

        cbStudentSel.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtErp.setText(newVal.getErpNo());
            } else {
                txtErp.clear();
            }
        });

        Label lblCompany = new Label("Company:");
        lblCompany.getStyleClass().add("form-label");
        ComboBox<Company> cbCompanySel = new ComboBox<>();
        cbCompanySel.setPromptText("Select Company");
        cbCompanySel.setItems(companiesList);
        cbCompanySel.getStyleClass().add("combo-box");
        cbCompanySel.setPrefWidth(250);
        cbCompanySel.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Company c, boolean empty) {
                super.updateItem(c, empty);
                setText((empty || c == null) ? null : c.getCompanyName() + " (" + c.getCompanyId() + ")");
            }
        });
        cbCompanySel.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Company c, boolean empty) {
                super.updateItem(c, empty);
                setText((empty || c == null) ? "Select Company" : c.getCompanyName() + " (" + c.getCompanyId() + ")");
            }
        });
        grid.add(lblCompany, 0, 3);
        grid.add(cbCompanySel, 1, 3);

        Label lblStatus = new Label("Status:");
        lblStatus.getStyleClass().add("form-label");
        ComboBox<String> cbStatus = new ComboBox<>();
        cbStatus.getItems().addAll("Placed", "Pending", "Selected", "Rejected", "Interviewing");
        cbStatus.setValue("Pending");
        cbStatus.getStyleClass().add("combo-box");
        cbStatus.setPrefWidth(250);
        grid.add(lblStatus, 0, 4);
        grid.add(cbStatus, 1, 4);

        Label lblDate = new Label("Selection Date:");
        lblDate.getStyleClass().add("form-label");
        DatePicker dpDate = new DatePicker();
        dpDate.getStyleClass().add("date-picker");
        grid.add(lblDate, 0, 5);
        grid.add(dpDate, 1, 5);

        Label lblRemark = new Label("Remark:");
        lblRemark.getStyleClass().add("form-label");
        TextField txtRemark = new TextField();
        txtRemark.setPromptText("e.g. Package: 4.5 LPA");
        txtRemark.getStyleClass().add("text-input");
        grid.add(lblRemark, 0, 6);
        grid.add(txtRemark, 1, 6);

        Label lblMsg = new Label();
        lblMsg.setStyle("-fx-font-weight: bold;");

        HBox btnBox = new HBox(8);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new Insets(10, 0, 0, 0));

        Button btnAdd = new Button("Add");
        btnAdd.getStyleClass().addAll("btn", "btn-primary");

        Button btnUpdate = new Button("Update");
        btnUpdate.getStyleClass().addAll("btn", "btn-success");
        btnUpdate.setDisable(true);

        Button btnDelete = new Button("Delete");
        btnDelete.getStyleClass().addAll("btn", "btn-danger");
        btnDelete.setDisable(true);

        Button btnClear = new Button("Clear");
        btnClear.getStyleClass().addAll("btn", "btn-secondary");

        Button btnClose = new Button("Close");
        btnClose.getStyleClass().addAll("btn", "btn-secondary");
        btnClose.setOnAction(e -> modalStage.close());

        btnBox.getChildren().addAll(btnAdd, btnUpdate, btnDelete, btnClear, btnClose);

        cbSelect.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtId.setText(newVal.getPlacementId());
                txtId.setEditable(false);
                txtId.setDisable(true);

                Student matchedStudent = null;
                for (Student s : studentsList) {
                    if (s.getId().equalsIgnoreCase(newVal.getStudentId())) {
                        matchedStudent = s;
                        break;
                    }
                }
                cbStudentSel.setValue(matchedStudent);
                txtErp.setText(newVal.getErpNo());

                Company matchedCompany = null;
                for (Company c : companiesList) {
                    if (c.getCompanyId().equalsIgnoreCase(newVal.getCompanyId())) {
                        matchedCompany = c;
                        break;
                    }
                }
                cbCompanySel.setValue(matchedCompany);
                cbStatus.setValue(newVal.getPlacementStatus());

                try {
                    dpDate.setValue(LocalDate.parse(newVal.getSelectionDate(), DATE_FORMATTER));
                } catch (Exception ex) {
                    dpDate.setValue(null);
                }
                txtRemark.setText(newVal.getRemark());

                btnAdd.setDisable(true);
                btnUpdate.setDisable(false);
                btnDelete.setDisable(false);
            }
        });

        Runnable resetForm = () -> {
            cbSelect.setValue(null);
            txtId.clear();
            txtId.setEditable(true);
            txtId.setDisable(false);
            cbStudentSel.setValue(null);
            txtErp.clear();
            cbCompanySel.setValue(null);
            cbStatus.setValue("Pending");
            dpDate.setValue(null);
            txtRemark.clear();

            btnAdd.setDisable(false);
            btnUpdate.setDisable(true);
            btnDelete.setDisable(true);
            lblMsg.setText("");
        };
        btnClear.setOnAction(e -> resetForm.run());

        btnAdd.setOnAction(e -> {
            String pid = txtId.getText().trim();
            Student stud = cbStudentSel.getValue();
            Company comp = cbCompanySel.getValue();
            String stat = cbStatus.getValue();
            String rem = txtRemark.getText().trim();

            if (pid.isEmpty() || stud == null || comp == null || stat == null || dpDate.getValue() == null) {
                lblMsg.setText("All fields (except remark) are required.");
                lblMsg.setStyle("-fx-text-fill: -danger-color;");
                return;
            }

            for (StudentPlacement pl : placementsList) {
                if (pl.getPlacementId().equalsIgnoreCase(pid)) {
                    lblMsg.setText("Placement ID already exists!");
                    lblMsg.setStyle("-fx-text-fill: -danger-color;");
                    return;
                }
            }

            layout.setDisable(true);
            lblMsg.setText("Adding placement...");
            lblMsg.setStyle("-fx-text-fill: -primary-color;");

            String pdate = dpDate.getValue().format(DATE_FORMATTER);
            StudentPlacement newPlacement = new StudentPlacement(pid, stud.getId(), stud.getErpNo(),
                    comp.getCompanyId(), stat, pdate, rem);

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    new GoogleSheetsService().addPlacement(newPlacement);
                    return null;
                }
            };
            task.setOnSucceeded(event -> {
                layout.setDisable(false);
                placementsList.add(newPlacement);
                addActivity("➕", "Added placement record for student: " + stud.getName());
                resetForm.run();
                lblMsg.setText("Placement added successfully.");
                lblMsg.setStyle("-fx-text-fill: -success-color;");
                refreshPlacementsData();
            });
            task.setOnFailed(event -> {
                layout.setDisable(false);
                Throwable t = task.getException();
                lblMsg.setText("Failed: " + t.getMessage());
                lblMsg.setStyle("-fx-text-fill: -danger-color;");
            });
            new Thread(task).start();
        });

        btnUpdate.setOnAction(e -> {
            StudentPlacement selected = cbSelect.getValue();
            if (selected == null)
                return;
            Student stud = cbStudentSel.getValue();
            Company comp = cbCompanySel.getValue();
            String stat = cbStatus.getValue();
            String rem = txtRemark.getText().trim();

            if (stud == null || comp == null || stat == null || dpDate.getValue() == null) {
                lblMsg.setText("All fields (except remark) are required.");
                lblMsg.setStyle("-fx-text-fill: -danger-color;");
                return;
            }

            layout.setDisable(true);
            lblMsg.setText("Updating placement...");
            lblMsg.setStyle("-fx-text-fill: -primary-color;");

            String pdate = dpDate.getValue().format(DATE_FORMATTER);
            StudentPlacement updatedPlacement = new StudentPlacement(selected.getPlacementId(), stud.getId(),
                    stud.getErpNo(), comp.getCompanyId(), stat, pdate, rem);

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    new GoogleSheetsService().updatePlacement(updatedPlacement);
                    return null;
                }
            };
            task.setOnSucceeded(event -> {
                layout.setDisable(false);
                selected.setStudentId(stud.getId());
                selected.setErpNo(stud.getErpNo());
                selected.setCompanyId(comp.getCompanyId());
                selected.setPlacementStatus(stat);
                selected.setSelectionDate(pdate);
                selected.setRemark(rem);

                addActivity("📝", "Updated placement ID: " + selected.getPlacementId());
                resetForm.run();
                lblMsg.setText("Placement updated successfully.");
                lblMsg.setStyle("-fx-text-fill: -success-color;");
                refreshPlacementsData();
            });
            task.setOnFailed(event -> {
                layout.setDisable(false);
                Throwable t = task.getException();
                lblMsg.setText("Failed: " + t.getMessage());
                lblMsg.setStyle("-fx-text-fill: -danger-color;");
            });
            new Thread(task).start();
        });

        btnDelete.setOnAction(e -> {
            StudentPlacement selected = cbSelect.getValue();
            if (selected == null)
                return;

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Delete");
            confirm.setHeaderText("Delete placement record " + selected.getPlacementId() + "?");
            confirm.setContentText("This will permanently remove the placement. Continue?");
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }

            layout.setDisable(true);
            lblMsg.setText("Deleting placement...");
            lblMsg.setStyle("-fx-text-fill: -danger-color;");

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    new GoogleSheetsService().deletePlacement(selected.getPlacementId());
                    return null;
                }
            };
            task.setOnSucceeded(event -> {
                layout.setDisable(false);
                placementsList.remove(selected);
                addActivity("❌", "Deleted placement record: " + selected.getPlacementId());
                resetForm.run();
                lblMsg.setText("Placement deleted successfully.");
                lblMsg.setStyle("-fx-text-fill: -success-color;");
                refreshPlacementsData();
            });
            task.setOnFailed(event -> {
                layout.setDisable(false);
                Throwable t = task.getException();
                lblMsg.setText("Failed: " + t.getMessage());
                lblMsg.setStyle("-fx-text-fill: -danger-color;");
            });
            new Thread(task).start();
        });

        layout.getChildren().addAll(lblTitle, selectorRow, grid, lblMsg, btnBox);

        Scene scene = new Scene(layout);
        java.io.File cssFile = new java.io.File("styles.css");
        if (cssFile.exists()) {
            try {
                scene.getStylesheets().add(cssFile.toURI().toURL().toExternalForm());
            } catch (Exception ignored) {
            }
        }
        modalStage.setScene(scene);
        modalStage.setResizable(false);
        modalStage.show();
    }
}
