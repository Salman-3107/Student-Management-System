package com.example.student_management_system;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class NavigationService {

    private static NavigationService instance;

    private static final double APP_WIDTH  = 1536;
    private static final double APP_HEIGHT = 864;

    private NavigationService() {}

    public static NavigationService getInstance() {
        if (instance == null) {
            instance = new NavigationService();
        }
        return instance;
    }

    // ── Public pages ──────────────────────────────────────────────────────────

    public void openHomepage(Stage stage) throws Exception {
        loadScene(stage, "/com/example/student_management_system/homepage.fxml", "BUET - Homepage");
    }

    public void openLogin(Stage stage) throws Exception {
        loadScene(stage, "/com/example/student_management_system/login.fxml", "BUET - Login");
    }

    public void openAcademics(Stage stage) throws Exception {
        loadScene(stage, "/com/example/student_management_system/academics.fxml", "BUET - Academics");
    }

    public void openAdmissions(Stage stage) throws Exception {
        loadScene(stage, "/com/example/student_management_system/admissions.fxml", "BUET - Admissions");
    }

    public void openDepartments(Stage stage) throws Exception {
        loadScene(stage, "/com/example/student_management_system/departments_page.fxml", "BUET - Departments");
    }

    public void openResearch(Stage stage) throws Exception {
        loadScene(stage, "/com/example/student_management_system/research.fxml", "BUET - Research");
    }

    public void openCampusLife(Stage stage) throws Exception {
        loadScene(stage, "/com/example/student_management_system/campuslife.fxml", "BUET - Campus Life");
    }

    public void openContact(Stage stage) throws Exception {
        loadScene(stage, "/com/example/student_management_system/contact.fxml", "BUET - Contact");
    }

    public void openAboutBUET(Stage stage) throws Exception {
        loadScene(stage, "/com/example/student_management_system/about_buet.fxml", "BUET - About BUET");
    }

    // ── Role-based dashboard routing ──────────────────────────────────────────

    public void openDashboard(Stage stage, User user) throws Exception {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null when opening dashboard.");
        }

        switch (user.getRole()) {
            case STUDENT:
                openStudentDashboard(stage, user);
                break;
            case TEACHER:
                openTeacherDashboard(stage, user);
                break;
            case ADMIN:
                openAdminDashboard(stage, user);
                break;
            default:
                throw new IllegalStateException("Unsupported role: " + user.getRole());
        }
    }

    private void openStudentDashboard(Stage stage, User user) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/example/student_management_system/student_dashboard.fxml"));
        if (loader.getLocation() == null) throw new RuntimeException("student_dashboard.fxml not found");

        Parent root = loader.load();
        StudentDashboardController controller = loader.getController();
        if (controller != null) controller.loadStudentData(user.getUsername(), user.getFullName());

        Scene scene = new Scene(root, APP_WIDTH, APP_HEIGHT);
        applyCommonSettings(stage, scene, "BUET - Student Dashboard");
    }

    private void openTeacherDashboard(Stage stage, User user) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/example/student_management_system/teacher_dashboard.fxml"));
        if (loader.getLocation() == null) throw new RuntimeException("teacher_dashboard.fxml not found");

        Parent root = loader.load();
        TeacherDashboardController controller = loader.getController();
        if (controller != null) controller.loadTeacherData(user.getUsername(), user.getFullName());

        Scene scene = new Scene(root, APP_WIDTH, APP_HEIGHT);
        applyCommonSettings(stage, scene, "BUET - Teacher Dashboard");
    }

    private void openAdminDashboard(Stage stage, User user) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/example/student_management_system/admin_dashboard.fxml"));
        if (loader.getLocation() == null) throw new RuntimeException("admin_dashboard.fxml not found");

        Parent root = loader.load();
        AdminDashboardController controller = loader.getController();
        if (controller != null) controller.setCurrentUser(user);

        Scene scene = new Scene(root, APP_WIDTH, APP_HEIGHT);
        applyCommonSettings(stage, scene, "BUET - Admin Dashboard");
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private void loadScene(Stage stage, String fxmlPath, String title) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        if (loader.getLocation() == null) throw new RuntimeException("FXML not found: " + fxmlPath);
        Parent root = loader.load();
        Scene scene = new Scene(root, APP_WIDTH, APP_HEIGHT);
        applyCommonSettings(stage, scene, title);
    }

    private void applyCommonSettings(Stage stage, Scene scene, String title) {
        stage.setTitle(title);
        stage.setScene(scene);
        stage.setWidth(APP_WIDTH);
        stage.setHeight(APP_HEIGHT);
        stage.setMinWidth(1200);
        stage.setMinHeight(700);
        stage.setMaximized(true);
        stage.show();
    }
}
