package com.example.student_management_system;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class LoginController implements Initializable {

    private static final int MAX_INPUT_LENGTH = 64;
    private static final String PREF_REMEMBER  = "rememberMe";
    private static final String PREF_USERNAME  = "savedUsername";
    private static final String PREF_USER_TYPE = "savedUserType";

    private final Preferences prefs = Preferences.userNodeForPackage(LoginController.class);

    @FXML private ComboBox<String> userTypeCombo;
    @FXML private TextField        usernameField;
    @FXML private PasswordField    passwordField;
    @FXML private CheckBox         rememberMeCheckBox;
    @FXML private HBox             errorBox;
    @FXML private Label            errorLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userTypeCombo.setItems(FXCollections.observableArrayList("Student", "Teacher", "Admin"));
        userTypeCombo.getSelectionModel().selectFirst();

        hideError();

        // Restore Remember Me
        boolean remembered = prefs.getBoolean(PREF_REMEMBER, false);
        if (remembered) {
            rememberMeCheckBox.setSelected(true);
            String savedUsername = prefs.get(PREF_USERNAME,  "");
            String savedUserType = prefs.get(PREF_USER_TYPE, "Student");
            if (!savedUsername.isEmpty()) usernameField.setText(savedUsername);
            if (!savedUserType.isEmpty()) userTypeCombo.setValue(savedUserType);
            passwordField.requestFocus();
        }

        // Hide error on any user input change
        usernameField.textProperty().addListener((obs, o, n) -> hideError());
        passwordField.textProperty().addListener((obs, o, n) -> hideError());
        userTypeCombo.valueProperty().addListener((obs, o, n) -> hideError());
    }

    @FXML
    protected void handleLogin() {
        hideError();

        String selectedUserType = userTypeCombo.getValue();
        String username         = sanitise(usernameField.getText());
        String password         = passwordField.getText();

        // ── Validation ──────────────────────────────────────────────────────
        if (selectedUserType == null || selectedUserType.isBlank()) {
            showError("Please select a user type.");
            return;
        }
        if (username.isEmpty()) {
            showError("Please enter your username.");
            usernameField.requestFocus();
            return;
        }
        if (password == null || password.isBlank()) {
            showError("Please enter your password.");
            passwordField.requestFocus();
            return;
        }
        if (username.length() > MAX_INPUT_LENGTH || password.length() > MAX_INPUT_LENGTH) {
            showError("Input too long. Please check your credentials.");
            return;
        }

        // ── Authentication ───────────────────────────────────────────────────
        try {
            Optional<User> result = AuthService.getInstance().authenticate(username, password);

            if (result.isEmpty()) {
                showError("Incorrect username or password. Please try again.");
                passwordField.clear();
                passwordField.requestFocus();
                return;
            }

            User user = result.get();

            if (!user.getRoleLabel().equalsIgnoreCase(selectedUserType)) {
                showError("The selected user type does not match this account.");
                passwordField.clear();
                passwordField.requestFocus();
                return;
            }

            // ── Remember Me ─────────────────────────────────────────────────
            if (rememberMeCheckBox != null && rememberMeCheckBox.isSelected()) {
                prefs.putBoolean(PREF_REMEMBER,  true);
                prefs.put(PREF_USERNAME,  username);
                prefs.put(PREF_USER_TYPE, selectedUserType);
            } else {
                prefs.putBoolean(PREF_REMEMBER, false);
                prefs.remove(PREF_USERNAME);
                prefs.remove(PREF_USER_TYPE);
            }

            Stage stage = (Stage) usernameField.getScene().getWindow();
            NavigationService.getInstance().openDashboard(stage, user);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Login failed: " + e.getMessage());
        }
    }

    @FXML
    protected void handleBack() {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            NavigationService.getInstance().openHomepage(stage);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not go back: " + e.getMessage());
        }
    }

    @FXML
    protected void handleForgotPassword() {
        showAlert(Alert.AlertType.INFORMATION, "Forgot Password",
            "Please contact the administration office to reset your password.\n\n" +
            "Email: admin@buet.ac.bd\n" +
            "Phone: +880-2-9665650\n" +
            "Office hours: Sun-Thu, 08:00-17:00");
    }

    @FXML
    protected void handleRegister() {
        showAlert(Alert.AlertType.INFORMATION, "New Registration",
            "New student registration is processed through the Admission Office.\n\n" +
            "Apply online: www.buet.ac.bd/admissions\n" +
            "Email: admission@buet.ac.bd\n" +
            "Phone: +880-2-9665650");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void showError(String message) {
        errorLabel.setText(message);
        errorBox.setVisible(true);
        errorBox.setManaged(true);
    }

    private void hideError() {
        errorLabel.setText("");
        errorBox.setVisible(false);
        errorBox.setManaged(false);
    }

    private String sanitise(String input) {
        return input == null ? "" : input.trim();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
