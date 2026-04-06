 package com.example.student_management_system;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Application entry point.
 *
 * Single, unambiguous main class. HelloApplication.java and the
 * duplicate Launcher.java pattern have been removed; this class
 * owns application startup entirely.
 */
public class BUETApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            NavigationService.getInstance().openHomepage(primaryStage);
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Fatal: could not load homepage – " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
