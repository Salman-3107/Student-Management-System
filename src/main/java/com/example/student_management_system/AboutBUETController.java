package com.example.student_management_system;

import javafx.fxml.FXML;

public class AboutBUETController extends BasePageController {

    @FXML
    protected void showAboutBUET() {
        try { NavigationService.getInstance().openAboutBUET(getCurrentStage()); }
        catch (Exception e) { showError("Navigation Error", e.getMessage()); }
    }

    @FXML
    protected void openStudentPortal() {
        try { NavigationService.getInstance().openLogin(getCurrentStage()); }
        catch (Exception e) { showError("Navigation Error", e.getMessage()); }
    }

    @FXML
    protected void openFacultyPortal() {
        try { NavigationService.getInstance().openLogin(getCurrentStage()); }
        catch (Exception e) { showError("Navigation Error", e.getMessage()); }
    }
}
