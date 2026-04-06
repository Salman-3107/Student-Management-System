package com.example.student_management_system;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ContactController extends BasePageController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField subjectField;
    @FXML private TextArea  messageArea;

    @FXML
    protected void sendMessage() {
        String name    = nameField    != null ? nameField.getText().trim()    : "";
        String email   = emailField   != null ? emailField.getText().trim()   : "";
        String subject = subjectField != null ? subjectField.getText().trim() : "";
        String message = messageArea  != null ? messageArea.getText().trim()  : "";

        if (name.isEmpty() || email.isEmpty() || subject.isEmpty() || message.isEmpty()) {
            showError("Incomplete Form", "Please fill in all fields before sending your message.");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            showError("Invalid Email", "Please enter a valid email address.");
            return;
        }

        showInfo("Message Sent",
            "Thank you, " + name + "!\n\n" +
            "Your message has been sent to the BUET administration.\n" +
            "We will respond to " + email + " within 2–3 working days.\n\n" +
            "Subject: " + subject);

        // Clear the form
        nameField.clear();
        emailField.clear();
        subjectField.clear();
        messageArea.clear();
    }
}
