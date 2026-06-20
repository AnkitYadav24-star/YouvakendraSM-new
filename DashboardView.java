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
        if (batchesPanel != null) batchesPanel.setVisible(false);
        if (companiesPanel != null) companiesPanel.setVisible(false);
        if (placementsPanel != null) placementsPanel.setVisible(false);
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
        txtSearch.setPromptText("Search students by name, ID, course...");
        txtSearch.getStyleClass().add("search-field");
        txtSearch.setPrefWidth(350);

        // Dynamic search binding
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredStudents.setPredicate(student -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lower = newValue.toLowerCase();
                return student.getId().toLowerCase().contains(lower)
                        || student.getName().toLowerCase().contains(lower)
                        || student.getCourse().toLowerCase().contains(lower)
                        || student.getBatch().toLowerCase().contains(lower)
                        || student.getStatus().toLowerCase().contains(lower);
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
        txtAvatarChar = new Text("A");
        txtAvatarChar.getStyleClass().add("avatar-text");
        avatarPane.getChildren().addAll(avatarBg, txtAvatarChar);

        // Profile Text
        VBox profileTxtBox = new VBox();
        profileTxtBox.setAlignment(Pos.CENTER_LEFT);
        lblHeaderUserName = new Label("Admin User");
        lblHeaderUserName.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        lblHeaderRoleBadge = new Label("Admin");
        lblHeaderRoleBadge.getStyleClass().addAll("role-badge");

        profileTxtBox.getChildren().addAll(lblHeaderUserName, lblHeaderRoleBadge);

        header.getChildren().addAll(txtSearch, spacer, btnNotification, lblRoleSelectText, cbRoleSelector, avatarPane,
                profileTxtBox);
        return header;
    }

    private void switchUserRole(String role) {
        this.currentRole = role;
        lblHeaderRoleBadge.getStyleClass().removeAll("role-badge", "role-badge-trainer", "role-badge-viewer");
        lblHeaderRoleBadge.getStyleClass().add("role-badge");

        if ("Admin".equals(role)) {
            lblHeaderUserName.setText("Admin User");
            lblHeaderRoleBadge.setText("Admin");
            txtAvatarChar.setText("A");
        } else if ("Trainer".equals(role)) {
            lblHeaderRoleBadge.getStyleClass().add("role-badge-trainer");
            lblHeaderUserName.setText("Trainer Staff");
            lblHeaderRoleBadge.setText("Trainer");
            txtAvatarChar.setText("T");
        } else {
            lblHeaderRoleBadge.getStyleClass().add("role-badge-viewer");
            lblHeaderUserName.setText("Viewer Account");
            lblHeaderRoleBadge.setText("Viewer");
            txtAvatarChar.setText("V");
        }

        applyRolePermissions(role);
        addActivity("👥", "Switched view mode to: " + role);
    }

    private void applyRolePermissions(String role) {
        boolean isAdmin = "Admin".equals(role);
        boolean isTrainer = "Trainer".equals(role);
        boolean isViewer = "Viewer".equals(role);

        // Apply to student form fields
        txtName.setDisable(isTrainer || isViewer);
        txtFatherName.setDisable(isTrainer || isViewer);
        txtMotherName.setDisable(isTrainer || isViewer);
        txtMobile.setDisable(isTrainer || isViewer);
        txtAltMobile.setDisable(isTrainer || isViewer);
        txtEmail.setDisable(isTrainer || isViewer);
        txtAddress.setDisable(isTrainer || isViewer);

        // Trainer can edit course/batch/status and date (for registration
        // support/attendance check)
        cbCourse.setDisable(isViewer);
        cbBatch.setDisable(isViewer);
        dpAdmissionDate.setDisable(isViewer);
        cbStatus.setDisable(isViewer);

        // Buttons configuration
        btnAdd.setDisable(!isAdmin); // Only Admin adds new
        btnDelete.setDisable(!isAdmin); // Only Admin deletes
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

        // Present & Absent counts from today's attendance data (if any loaded), or fall back to checking in-memory state
        String todayStr = LocalDate.now().toString();
        long presentCount = attendanceList.stream()
                .filter(a -> todayStr.equals(a.getDate()) && "Present".equalsIgnoreCase(a.getStatus()))
                .count();
        long absentCount = attendanceList.stream()
                .filter(a -> todayStr.equals(a.getDate()) && "Absent".equalsIgnoreCase(a.getStatus()))
                .count();

        // If no attendance records exist for today, calculate based on presentStudentIds list (marked in daily check-in) or overall status
        if (presentCount == 0 && absentCount == 0) {
            presentCount = presentStudentIds.size();
            absentCount = total - presentCount;
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

    // --- PANEL 2: STUDENT MANAGEMENT ---
    private void buildStudentsPanel() {
        studentsPanel = new VBox(20);
        studentsPanel.setPadding(new Insets(24));
        studentsPanel.getStyleClass().add("root");

        Label lblTitle = new Label("Student Directory & Profiles");
        lblTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: -text-main;");

        // Split Layout: Table left, Form right
        HBox bodyLayout = new HBox(20);
        HBox.setHgrow(bodyLayout, Priority.ALWAYS);
        VBox.setVgrow(bodyLayout, Priority.ALWAYS);

        // Left Table Box (65%)
        VBox tableBox = new VBox(15);
        tableBox.getStyleClass().add("content-card");
        tableBox.setPrefWidth(850);
        HBox.setHgrow(tableBox, Priority.ALWAYS);

        Label lblTableTitle = new Label("Student Enrollments");
        lblTableTitle.getStyleClass().add("card-title");

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

        // Table Columns
        TableColumn<Student, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> data.getValue().idProperty());
        colId.setPrefWidth(80);

        TableColumn<Student, String> colName = new TableColumn<>("Student Name");
        colName.setCellValueFactory(data -> data.getValue().nameProperty());
        colName.setPrefWidth(180);

        TableColumn<Student, String> colCourse = new TableColumn<>("Course");
        colCourse.setCellValueFactory(data -> data.getValue().courseProperty());
        colCourse.setPrefWidth(90);

        TableColumn<Student, String> colBatch = new TableColumn<>("Batch");
        colBatch.setCellValueFactory(data -> data.getValue().batchProperty());
        colBatch.setPrefWidth(120);

        TableColumn<Student, String> colAdm = new TableColumn<>("Admission");
        colAdm.setCellValueFactory(data -> data.getValue().admissionDateProperty());
        colAdm.setPrefWidth(120);

        TableColumn<Student, String> colCmp = new TableColumn<>("Completion");
        colCmp.setCellValueFactory(data -> data.getValue().completionDateProperty());
        colCmp.setPrefWidth(120);

        TableColumn<Student, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());
        colStatus.setPrefWidth(130);

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

        studentTable.getColumns().addAll(colId, colName, colCourse, colBatch, colAdm, colCmp, colStatus);
        studentTable.setItems(filteredStudents);

        tableBox.getChildren().addAll(lblTableTitle, tableContainer);

        // Right Form Box (35%)
        formBox = new VBox(15);
        formBox.getStyleClass().add("content-card");
        formBox.setPrefWidth(480);
        formBox.setMaxWidth(500);

        Label lblFormTitle = new Label("Student Details Editor");
        lblFormTitle.getStyleClass().add("card-title");

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
        gp.add(new Label("Student Name *:"), 0, 1);
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
        gp.add(new Label("Mobile Number *:"), 0, 4);
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
        gp.add(new Label("Course Select *:"), 0, 7);
        gp.add(cbCourse, 1, 7);

        // Batch
        cbBatch = new ComboBox<>();
        cbBatch.getItems().addAll("A (10AM-1PM)", "B (2PM-5PM)", "C (3PM-6PM)");
        cbBatch.setMaxWidth(Double.MAX_VALUE);
        gp.add(new Label("Batch Time *:"), 0, 8);
        gp.add(cbBatch, 1, 8);

        // Admission Date
        dpAdmissionDate = new DatePicker();
        dpAdmissionDate.setMaxWidth(Double.MAX_VALUE);
        gp.add(new Label("Admission Date *:"), 0, 9);
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

        formBox.getChildren().addAll(lblFormTitle, formScroll, lblFormMessage, btnBox);

        bodyLayout.getChildren().addAll(tableBox, formBox);
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
            int durationMonths = "DTP".equals(course) ? 4 : 3;
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
        cbCourse.setValue(s.getCourse());
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

        String id = String.format("ST%04d", nextStudentIdNum++);
        Student s = new Student(
                id,
                txtName.getText().trim(),
                txtFatherName.getText().trim(),
                txtMotherName.getText().trim(),
                txtMobile.getText().trim(),
                txtAltMobile.getText().trim(),
                txtEmail.getText().trim(),
                txtAddress.getText().trim(),
                cbCourse.getValue(),
                cbBatch.getValue(),
                dpAdmissionDate.getValue().format(DATE_FORMATTER),
                lblCompletionDate.getText(),
                cbStatus.getValue());

        studentsList.add(s);
        addActivity("➕", "Added new student: " + s.getName() + " (" + s.getId() + ")");
        clearFormSelection();
        updateDashboardMetrics();

        showInfoAlert("Success", "Student profiles added successfully.", "Record " + id + " created.");
    }

    private void doUpdateStudent() {
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;
        if (!validateForm())
            return;

        selected.setName(txtName.getText().trim());
        selected.setFatherName(txtFatherName.getText().trim());
        selected.setMotherName(txtMotherName.getText().trim());
        selected.setMobile(txtMobile.getText().trim());
        selected.setAltMobile(txtAltMobile.getText().trim());
        selected.setEmail(txtEmail.getText().trim());
        selected.setAddress(txtAddress.getText().trim());
        selected.setCourse(cbCourse.getValue());
        selected.setBatch(cbBatch.getValue());
        selected.setAdmissionDate(dpAdmissionDate.getValue().format(DATE_FORMATTER));
        selected.setCompletionDate(lblCompletionDate.getText());
        selected.setStatus(cbStatus.getValue());

        addActivity("📝", "Updated details for: " + selected.getName() + " (" + selected.getId() + ")");

        // Refresh table display
        studentTable.refresh();
        clearFormSelection();
        updateDashboardMetrics();

        showInfoAlert("Success", "Student profiles updated successfully.", "Record updated.");
    }

    private void doDeleteStudent() {
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Remove student record?");
        confirm.setContentText(
                "Are you sure you want to permanently delete " + selected.getName() + " (" + selected.getId() + ")?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            studentsList.remove(selected);
            addActivity("❌", "Deleted student profile: " + selected.getName() + " (" + selected.getId() + ")");
            clearFormSelection();
            updateDashboardMetrics();
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

        // Setup columns in the exact order requested: Attendance_ID, Date, Student_ID, Batch_ID, Status, Marked_By, Marked_Time
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

        attendanceTable.getColumns().addAll(colAttId, colDate, colStudentId, colBatchId, colStatus, colMarkedBy, colMarkedTime);
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

        Button btnRefresh = new Button("Refresh Data 🔄");
        btnRefresh.getStyleClass().addAll("btn", "btn-primary");
        btnRefresh.setOnAction(e -> refreshCoursesData());

        bar.getChildren().addAll(lblSubtitle, spacer, btnRefresh);

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

        Button btnRefresh = new Button("Refresh Data 🔄");
        btnRefresh.getStyleClass().addAll("btn", "btn-primary");
        btnRefresh.setOnAction(e -> refreshBatchesData());

        bar.getChildren().addAll(lblSubtitle, spacer, btnRefresh);

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

        Button btnRefresh = new Button("Refresh Data 🔄");
        btnRefresh.getStyleClass().addAll("btn", "btn-primary");
        btnRefresh.setOnAction(e -> refreshCompaniesData());

        bar.getChildren().addAll(lblSubtitle, spacer, btnRefresh);

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

        Button btnRefresh = new Button("Refresh Data 🔄");
        btnRefresh.getStyleClass().addAll("btn", "btn-primary");
        btnRefresh.setOnAction(e -> refreshPlacementsData());

        bar.getChildren().addAll(lblSubtitle, spacer, btnRefresh);

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
                    if (lower.contains("select") || lower.contains("place") || lower.contains("pass") || lower.contains("active")) {
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

        placementsTable.getColumns().addAll(colPlacementId, colStudentId, colErpNo, colCompanyId, colPlacementStatus, colSelectionDate, colRemark);
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
            studentsList.clear();
            studentsList.addAll(loaded);

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
                "Error details:\n" + ex.toString() + "\n\nStack Trace:\n" + stackTrace + "\n\nPlease check your network connection and credentials.");
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
                "Error details:\n" + ex.toString() + "\n\nStack Trace:\n" + stackTrace + "\n\nPlease check your network connection and credentials.");
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
                "Error details:\n" + ex.toString() + "\n\nStack Trace:\n" + stackTrace + "\n\nPlease check your network connection and credentials.");
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
                "Error details:\n" + ex.toString() + "\n\nStack Trace:\n" + stackTrace + "\n\nPlease check your network connection and credentials.");
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
                "Error details:\n" + ex.toString() + "\n\nStack Trace:\n" + stackTrace + "\n\nPlease check your network connection and credentials.");
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
                "Error details:\n" + ex.toString() + "\n\nStack Trace:\n" + stackTrace + "\n\nPlease check your network connection and credentials.");
        });

        new Thread(loadTask).start();
    }
}
