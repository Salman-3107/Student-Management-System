package com.example.student_management_system;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;

public class DepartmentsPageController extends BasePageController {

    private void openDeptDetail(DepartmentInfo dept) {
        Stage currentStage = getCurrentStage();
        Stage deptStage = new Stage();
        deptStage.setTitle(dept.getName() + " — BUET");

        // Header
        ImageView logo = new ImageView();
        URL logoUrl = getClass().getResource("/buet logo.png");
        if (logoUrl != null) {
            logo.setImage(new Image(logoUrl.toExternalForm()));
            logo.setFitHeight(60); logo.setFitWidth(62); logo.setPreserveRatio(true);
        }
        VBox uniText = new VBox(3);
        Label bangla = new Label("বাংলাদেশ প্রকৌশল বিশ্ববিদ্যালয়");
        bangla.setStyle("-fx-text-fill: #d4a017; -fx-font-size: 18px; -fx-font-weight: bold;");
        Label engName = new Label("BANGLADESH UNIVERSITY OF ENGINEERING AND TECHNOLOGY");
        engName.setStyle("-fx-text-fill: #f5f5f0; -fx-font-size: 13px; -fx-font-weight: bold;");
        Label subtitle = new Label("Student Management System");
        subtitle.setStyle("-fx-text-fill: #9a9a8a; -fx-font-size: 11px; -fx-font-style: italic;");
        uniText.getChildren().addAll(bangla, engName, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backBtn = new Button("← Back to Departments");
        backBtn.setStyle("-fx-background-color: #c8102e; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 4px; -fx-cursor: hand; -fx-padding: 10 24 10 24;");
        backBtn.setOnAction(e -> { deptStage.close(); currentStage.show(); currentStage.toFront(); });

        HBox universityBar = new HBox(20, logo, uniText, spacer, backBtn);
        universityBar.setAlignment(Pos.CENTER_LEFT);
        universityBar.setPadding(new Insets(15, 30, 15, 30));
        universityBar.setStyle("-fx-background-color: #0f0f0f;");

        Label breadcrumb = new Label("Departments  ›  " + dept.getName());
        breadcrumb.setStyle("-fx-text-fill: rgba(255,255,255,0.92); -fx-font-size: 13px; -fx-font-weight: bold;");
        HBox navBar = new HBox(breadcrumb);
        navBar.setAlignment(Pos.CENTER_LEFT);
        navBar.setPadding(new Insets(14, 30, 14, 30));
        navBar.setStyle("-fx-background-color: #c8102e; -fx-min-height: 48px;");

        VBox heroBox = new VBox(10);
        heroBox.setPadding(new Insets(40));
        heroBox.setStyle("-fx-background-color: #1e0a0a; -fx-border-color: #c8102e; -fx-border-width: 0 0 3 0;");
        Label deptTitle = new Label(dept.getName() + "  (" + dept.getShortName() + ")");
        deptTitle.setStyle("-fx-text-fill: #f5f5f0; -fx-font-size: 30px; -fx-font-weight: bold;");
        Label shortDesc = new Label(dept.getDescription());
        shortDesc.setWrapText(true);
        shortDesc.setStyle("-fx-text-fill: #9a9a8a; -fx-font-size: 15px; -fx-line-spacing: 4;");
        heroBox.getChildren().addAll(deptTitle, shortDesc);

        VBox body = new VBox(0);
        body.setStyle("-fx-background-color: #1a1a1a;");

        // Gallery
        VBox galleryBox = new VBox(20);
        galleryBox.setPadding(new Insets(30, 40, 10, 40));
        galleryBox.setStyle("-fx-background-color: #1a1a1a;");
        Label galleryTitle = new Label("📸  Department Gallery");
        galleryTitle.setStyle("-fx-text-fill: #f0f0e8; -fx-font-size: 19px; -fx-font-weight: bold;");
        galleryBox.getChildren().add(galleryTitle);
        double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
        for (String imagePath : dept.getImagePaths()) {
            URL imgUrl = getClass().getResource(imagePath);
            if (imgUrl != null) {
                try {
                    Image image = new Image(imgUrl.toExternalForm(), true);
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(screenWidth - 100);
                    imageView.setPreserveRatio(true);
                    imageView.setSmooth(true);
                    VBox imgCard = new VBox(imageView);
                    imgCard.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 8px; -fx-border-color: #2e2e2e; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-padding: 8;");
                    galleryBox.getChildren().add(imgCard);
                } catch (Exception ignored) {}
            }
        }

        // About
        VBox aboutBox = new VBox(15);
        aboutBox.setPadding(new Insets(30, 40, 40, 40));
        aboutBox.setStyle("-fx-background-color: #1a1a1a;");
        Label aboutTitle = new Label("📖  About the Department");
        aboutTitle.setStyle("-fx-text-fill: #f0f0e8; -fx-font-size: 19px; -fx-font-weight: bold;");
        aboutBox.getChildren().add(aboutTitle);
        for (String para : dept.getAboutParagraphs()) {
            Label p = new Label(para);
            p.setWrapText(true);
            p.setStyle("-fx-text-fill: #c8c8c0; -fx-font-size: 14px; -fx-line-spacing: 5;");
            aboutBox.getChildren().add(p);
        }

        body.getChildren().addAll(galleryBox, aboutBox);

        Label copyright = new Label("© 2026 Bangladesh University of Engineering and Technology. All Rights Reserved.");
        copyright.setStyle("-fx-text-fill: #404040; -fx-font-size: 11.5px;");
        HBox footer = new HBox(copyright);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(20, 30, 20, 30));
        footer.setStyle("-fx-background-color: #0a0a0a; -fx-border-color: #c8102e; -fx-border-width: 3 0 0 0;");

        ScrollPane scrollPane = new ScrollPane(body);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #1a1a1a; -fx-background: #1a1a1a;");

        VBox root = new VBox(universityBar, navBar, heroBox, scrollPane, footer);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        root.setStyle("-fx-background-color: #1a1a1a;");

        javafx.geometry.Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(root, bounds.getWidth(), bounds.getHeight());
        URL cssUrl = getClass().getResource("/com/example/student_management_system/styles.css");
        if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

        deptStage.setScene(scene);
        deptStage.setMaximized(true);
        currentStage.hide();
        deptStage.show();
    }

    @FXML protected void openCE(MouseEvent e)   { openDeptDetail(DeptData.CE()); }
    @FXML protected void openEEE(MouseEvent e)  { openDeptDetail(DeptData.EEE()); }
    @FXML protected void openCSE(MouseEvent e)  { openDeptDetail(DeptData.CSE()); }
    @FXML protected void openME(MouseEvent e)   { openDeptDetail(DeptData.ME()); }
    @FXML protected void openChE(MouseEvent e)  { openDeptDetail(DeptData.ChE()); }
    @FXML protected void openIPE(MouseEvent e)  { openDeptDetail(DeptData.IPE()); }
    @FXML protected void openWRE(MouseEvent e)  { openDeptDetail(DeptData.WRE()); }
    @FXML protected void openNAME(MouseEvent e) { openDeptDetail(DeptData.NAME()); }
    @FXML protected void openBME(MouseEvent e)  { openDeptDetail(DeptData.BME()); }
    @FXML protected void openMME(MouseEvent e)  { openDeptDetail(DeptData.MME()); }
    @FXML protected void openNCE(MouseEvent e)  { openDeptDetail(DeptData.NCE()); }
    @FXML protected void openArch(MouseEvent e) { openDeptDetail(DeptData.Arch()); }
    @FXML protected void openURP(MouseEvent e)  { openDeptDetail(DeptData.URP()); }
}
