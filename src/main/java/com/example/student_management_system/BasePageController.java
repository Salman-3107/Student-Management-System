package com.example.student_management_system;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

public abstract class BasePageController {

    protected Stage getCurrentStage() {
        return Stage.getWindows().stream()
                .filter(w -> w instanceof Stage && w.isShowing())
                .map(w -> (Stage) w)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No active stage found"));
    }

    @FXML
    protected void showHome() {
        try { NavigationService.getInstance().openHomepage(getCurrentStage()); }
        catch (Exception e) { showError("Navigation Error", e.getMessage()); }
    }

    @FXML
    protected void showAcademics() {
        try { NavigationService.getInstance().openAcademics(getCurrentStage()); }
        catch (Exception e) { showError("Navigation Error", e.getMessage()); }
    }

    @FXML
    protected void showAdmissions() {
        try { NavigationService.getInstance().openAdmissions(getCurrentStage()); }
        catch (Exception e) { showError("Navigation Error", e.getMessage()); }
    }

    @FXML
    protected void showDepartments() {
        try { NavigationService.getInstance().openDepartments(getCurrentStage()); }
        catch (Exception e) { showError("Navigation Error", e.getMessage()); }
    }

    @FXML
    protected void showResearch() {
        try { NavigationService.getInstance().openResearch(getCurrentStage()); }
        catch (Exception e) { showError("Navigation Error", e.getMessage()); }
    }

    @FXML
    protected void showCampusLife() {
        try { NavigationService.getInstance().openCampusLife(getCurrentStage()); }
        catch (Exception e) { showError("Navigation Error", e.getMessage()); }
    }

    @FXML
    protected void showContact() {
        try { NavigationService.getInstance().openContact(getCurrentStage()); }
        catch (Exception e) { showError("Navigation Error", e.getMessage()); }
    }

    @FXML
    protected void showAboutBUET() {
        try { NavigationService.getInstance().openAboutBUET(getCurrentStage()); }
        catch (Exception e) { showError("Navigation Error", e.getMessage()); }
    }

    @FXML
    protected void handleLogin() {
        try { NavigationService.getInstance().openLogin(getCurrentStage()); }
        catch (Exception e) { showError("Navigation Error", e.getMessage()); }
    }

    protected void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    protected void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
