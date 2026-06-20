import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import java.util.List;
import java.util.function.Consumer;

public class LoginView extends StackPane {
    private final Consumer<LoggedInUser> onLoginSuccess;
    private final GoogleSheetsService sheetsService = new GoogleSheetsService();

    private VBox card;
    private VBox portalsBox;
    private VBox formBox;

    // Login Form elements
    private Label lblPortalTitle;
    private TextField txtUsername; // Used for Admin ID / Trainer ID / Student ID
    private TextField txtErpNo; // Student specific
    private PasswordField txtPassword;
    private TextField txtPasswordVisible; // For toggling visibility
    private CheckBox cbShowPassword;
    private Button btnSubmit;
    private Button btnBack;
    private Label lblErrorMessage;
    private ProgressIndicator loadingIndicator;

    private LoggedInUser.Role selectedRole;

    public LoginView(Consumer<LoggedInUser> onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;

        getStyleClass().add("login-root");

        // Overlay glassmorphism card
        card = new VBox(24);
        card.getStyleClass().add("login-card");
        card.setAlignment(Pos.CENTER);
        card.setMaxSize(450, 500);

        // Header Title
        Label lblAppTitle = new Label("YOUVAKENDRA");
        lblAppTitle.getStyleClass().add("login-app-title");
        Label lblAppSubtitle = new Label("Student Management System");
        lblAppSubtitle.getStyleClass().add("login-app-subtitle");

        VBox headerBox = new VBox(4, lblAppTitle, lblAppSubtitle);
        headerBox.setAlignment(Pos.CENTER);

        // Create Portals selection and Form containers
        buildPortalsView();
        buildFormView();

        card.getChildren().addAll(headerBox, portalsBox);
        getChildren().add(card);
    }

    private void buildPortalsView() {
        portalsBox = new VBox(20);
        portalsBox.setAlignment(Pos.CENTER);
        portalsBox.setPadding(new Insets(20, 0, 10, 0));

        Label lblSelect = new Label("Select Portal to Sign In");
        lblSelect.getStyleClass().add("login-section-title");

        Button btnAdmin = createPortalButton("👮  Admin Portal", LoggedInUser.Role.ADMIN);
        Button btnTrainer = createPortalButton("👨‍🏫  Trainer Portal", LoggedInUser.Role.TRAINER);
        Button btnStudent = createPortalButton("🎓  Student Portal", LoggedInUser.Role.STUDENT);

        portalsBox.getChildren().addAll(lblSelect, btnAdmin, btnTrainer, btnStudent);
    }

    private Button createPortalButton(String text, LoggedInUser.Role role) {
        Button btn = new Button(text);
        btn.getStyleClass().addAll("btn", "btn-portal");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> showLoginForm(role));
        return btn;
    }

    private void buildFormView() {
        formBox = new VBox(14);
        formBox.setAlignment(Pos.CENTER_LEFT);
        formBox.setPadding(new Insets(10, 10, 10, 10));

        lblPortalTitle = new Label();
        lblPortalTitle.getStyleClass().add("login-section-title");
        lblPortalTitle.setAlignment(Pos.CENTER);
        lblPortalTitle.setMaxWidth(Double.MAX_VALUE);

        // Grid for inputs
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        VBox.setVgrow(grid, Priority.ALWAYS);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(30);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(70);
        grid.getColumnConstraints().addAll(col1, col2);

        // Common Fields
        Label lblUsername = new Label("User ID:");
        lblUsername.getStyleClass().add("form-label");
        txtUsername = new TextField();
        txtUsername.setPromptText("Enter your ID");
        txtUsername.getStyleClass().add("text-input");

        Label lblErp = new Label("ERP No:");
        lblErp.getStyleClass().add("form-label");
        txtErpNo = new TextField();
        txtErpNo.setPromptText("Enter ERP number");
        txtErpNo.getStyleClass().add("text-input");

        Label lblPassword = new Label("Password:");
        lblPassword.getStyleClass().add("form-label");

        // Stack pane for password toggle
        txtPassword = new PasswordField();
        txtPassword.setPromptText("Enter password");
        txtPassword.getStyleClass().add("text-input");

        txtPasswordVisible = new TextField();
        txtPasswordVisible.setPromptText("Enter password");
        txtPasswordVisible.getStyleClass().add("text-input");
        txtPasswordVisible.setVisible(false);

        // Bind visibility text field and password field
        txtPassword.textProperty().bindBidirectional(txtPasswordVisible.textProperty());

        StackPane passwordStack = new StackPane(txtPassword, txtPasswordVisible);

        cbShowPassword = new CheckBox("Show Password");
        cbShowPassword.getStyleClass().add("login-checkbox");
        cbShowPassword.setOnAction(e -> {
            boolean visible = cbShowPassword.isSelected();
            txtPasswordVisible.setVisible(visible);
            txtPassword.setVisible(!visible);
        });

        // Set grid elements layout
        // Row indices will be dynamic since ERP No is student-only
        grid.add(lblUsername, 0, 0);
        grid.add(txtUsername, 1, 0);

        grid.add(lblErp, 0, 1);
        grid.add(txtErpNo, 1, 1);

        grid.add(lblPassword, 0, 2);
        grid.add(passwordStack, 1, 2);

        grid.add(cbShowPassword, 1, 3);

        // Feedback / error message
        lblErrorMessage = new Label();
        lblErrorMessage.getStyleClass().add("login-error-message");
        lblErrorMessage.setWrapText(true);
        lblErrorMessage.setVisible(false);

        // Loading spinner
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setStyle("-fx-progress-color: -primary-color;");
        loadingIndicator.setPrefSize(24, 24);
        loadingIndicator.setVisible(false);

        HBox submitBox = new HBox(12);
        submitBox.setAlignment(Pos.CENTER);
        btnSubmit = new Button("Sign In");
        btnSubmit.getStyleClass().addAll("btn", "btn-primary");
        btnSubmit.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnSubmit, Priority.ALWAYS);
        btnSubmit.setOnAction(e -> performLogin());

        btnBack = new Button("Back");
        btnBack.getStyleClass().addAll("btn", "btn-secondary");
        btnBack.setOnAction(e -> {
            lblErrorMessage.setVisible(false);
            card.getChildren().remove(formBox);
            card.getChildren().add(portalsBox);
        });

        submitBox.getChildren().addAll(btnBack, btnSubmit, loadingIndicator);

        formBox.getChildren().addAll(lblPortalTitle, grid, lblErrorMessage, submitBox);
    }

    private void showLoginForm(LoggedInUser.Role role) {
        this.selectedRole = role;
        txtUsername.clear();
        txtErpNo.clear();
        txtPassword.clear();
        cbShowPassword.setSelected(false);
        txtPasswordVisible.setVisible(false);
        txtPassword.setVisible(true);

        card.getChildren().remove(portalsBox);
        card.getChildren().add(formBox);

        // Adjust fields depending on portal selected
        if (role == LoggedInUser.Role.ADMIN) {
            lblPortalTitle.setText("Admin Authentication");
            txtUsername.setPromptText("e.g. AD001");
            toggleErpField(false);
        } else if (role == LoggedInUser.Role.TRAINER) {
            lblPortalTitle.setText("Trainer Authentication");
            txtUsername.setPromptText("e.g. TR001");
            toggleErpField(false);
        } else {
            lblPortalTitle.setText("Student Authentication");
            txtUsername.setPromptText("e.g. ST001");
            toggleErpField(true);
        }
    }

    private void toggleErpField(boolean visible) {
        // Find ERP labels/fields in formBox grid
        GridPane grid = (GridPane) formBox.getChildren().get(1);
        grid.getChildren().forEach(node -> {
            Integer row = GridPane.getRowIndex(node);
            if (row != null && row == 1) {
                node.setVisible(visible);
                node.setManaged(visible);
            }
        });
    }

    private void performLogin() {
        String id = txtUsername.getText().trim();
        String password = txtPassword.getText();
        String erp = txtErpNo.getText().trim();

        if (id.isEmpty()) {
            showError("User ID is required.");
            return;
        }
        if (selectedRole == LoggedInUser.Role.STUDENT && erp.isEmpty()) {
            showError("ERP Number is required.");
            return;
        }
        if (password.isEmpty()) {
            showError("Password is required.");
            return;
        }

        // Show spinner and disable inputs
        setFormDisabled(true);
        loadingIndicator.setVisible(true);
        lblErrorMessage.setVisible(false);

        // Asynchronous Task for Auth
        Task<LoggedInUser> authTask = new Task<>() {
            @Override
            protected LoggedInUser call() throws Exception {
                if (selectedRole == LoggedInUser.Role.ADMIN) {
                    List<AdminProfile> admins = sheetsService.readAdminProfiles();
                    for (AdminProfile admin : admins) {
                        if (id.equalsIgnoreCase(admin.getAdminId()) && password.equals(admin.getPassword())) {
                            return new LoggedInUser(
                                    LoggedInUser.Role.ADMIN,
                                    admin.getAdminId(),
                                    admin.getAdminName(),
                                    admin.getAdminPictureUrl(),
                                    admin.getCenter(),
                                    admin.getDesignation(),
                                    "");
                        }
                    }
                } else if (selectedRole == LoggedInUser.Role.TRAINER) {
                    List<TrainerProfile> trainers = sheetsService.readTrainerProfiles();
                    for (TrainerProfile trainer : trainers) {
                        if (id.equalsIgnoreCase(trainer.getTrainerId()) && password.equals(trainer.getPassword())) {
                            return new LoggedInUser(
                                    LoggedInUser.Role.TRAINER,
                                    trainer.getTrainerId(),
                                    trainer.getTrainerName(),
                                    trainer.getTrainerPictureUrl(),
                                    trainer.getCenter(),
                                    trainer.getDesignation(),
                                    "");
                        }
                    }
                } else if (selectedRole == LoggedInUser.Role.STUDENT) {
                    List<StudentLoginProfile> studentLogins = sheetsService.readStudentLoginProfiles();
                    StudentLoginProfile matchedLogin = null;
                    for (StudentLoginProfile profile : studentLogins) {
                        if ((id.equalsIgnoreCase(profile.getStudentId()) || erp.equalsIgnoreCase(profile.getErpNo()))
                                && password.equals(profile.getPassword())) {
                            matchedLogin = profile;
                            break;
                        }
                    }
                    if (matchedLogin != null) {
                        // Find matching Student details in the main Students sheet for their name,
                        // center, course
                        List<Student> students = sheetsService.readStudents();
                        String studentName = "Student User";
                        String center = "";
                        String course = "";
                        for (Student s : students) {
                            if (matchedLogin.getStudentId().equalsIgnoreCase(s.getStudentId())
                                    || matchedLogin.getErpNo().equalsIgnoreCase(s.getErpNo())) {
                                studentName = s.getStudentName();
                                center = s.getCenter();
                                course = s.getCourse();
                                break;
                            }
                        }
                        return new LoggedInUser(
                                LoggedInUser.Role.STUDENT,
                                matchedLogin.getStudentId(),
                                studentName,
                                matchedLogin.getImageUrl(),
                                center,
                                "Course: " + course,
                                matchedLogin.getErpNo());
                    }
                }
                throw new Exception("Invalid User ID, ERP Number, or Password.");
            }
        };

        authTask.setOnSucceeded(e -> {
            setFormDisabled(false);
            loadingIndicator.setVisible(false);
            LoggedInUser user = authTask.getValue();
            Platform.runLater(() -> onLoginSuccess.accept(user));
        });

        authTask.setOnFailed(e -> {
            setFormDisabled(false);
            loadingIndicator.setVisible(false);
            Throwable ex = authTask.getException();
            showError(ex.getMessage());
        });

        new Thread(authTask).start();
    }

    private void setFormDisabled(boolean disabled) {
        txtUsername.setDisable(disabled);
        txtErpNo.setDisable(disabled);
        txtPassword.setDisable(disabled);
        txtPasswordVisible.setDisable(disabled);
        cbShowPassword.setDisable(disabled);
        btnSubmit.setDisable(disabled);
        btnBack.setDisable(disabled);
    }

    private void showError(String msg) {
        lblErrorMessage.setText(msg);
        lblErrorMessage.setVisible(true);
    }
}
