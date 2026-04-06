package com.example.student_management_system;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StudentDashboardController {

    @FXML private BorderPane rootPane;
    @FXML private Label sidebarNameLabel, sidebarIdLabel;
    @FXML private Label homeNameLabel, homeIdLabel, homeDeptLabel, homeSessionLabel, homeLevelTermLabel, homeEmailLabel;
    @FXML private Label infoNameLabel, infoIdLabel, infoDeptLabel, infoSessionLabel, infoLevelTermLabel, infoEmailLabel, infoMobileLabel, infoHallLabel;
    @FXML private ScrollPane homeSection, personalSection;
    @FXML private ScrollPane gradesSection, duesSection;
    @FXML private VBox gradesContentBox, duesContentBox;

    private String studentId;
    private String fullName;
    private String department;
    private String deptCode;
    private String session;
    private String levelTerm;
    private int currentLevel = 1;
    private int currentTerm = 1;

    public static class CourseRow {
        private final SimpleStringProperty courseNo;
        private final SimpleStringProperty courseTitle;
        private final SimpleStringProperty creditHours;
        private final SimpleStringProperty grade;
        private final SimpleStringProperty gradePoint;

        public CourseRow(String no, String title, String cr, String g, String gp) {
            this.courseNo = new SimpleStringProperty(no);
            this.courseTitle = new SimpleStringProperty(title);
            this.creditHours = new SimpleStringProperty(cr);
            this.grade = new SimpleStringProperty(g);
            this.gradePoint = new SimpleStringProperty(gp);
        }

        public String getCourseNo() { return courseNo.get(); }
        public String getCourseTitle() { return courseTitle.get(); }
        public String getCreditHours() { return creditHours.get(); }
        public String getGrade() { return grade.get(); }
        public String getGradePoint() { return gradePoint.get(); }
    }

    public static class DueRow {
        private final SimpleStringProperty type;
        private final SimpleStringProperty description;
        private final SimpleStringProperty amount;
        private final SimpleStringProperty status;

        public DueRow(String t, String d, String a, String s) {
            type = new SimpleStringProperty(t);
            description = new SimpleStringProperty(d);
            amount = new SimpleStringProperty(a);
            status = new SimpleStringProperty(s);
        }

        public String getType() { return type.get(); }
        public String getDescription() { return description.get(); }
        public String getAmount() { return amount.get(); }
        public String getStatus() { return status.get(); }
    }

    public void loadStudentData(String studentId, String fullName) {
        this.studentId = studentId;
        this.fullName = fullName;

        String batchCode = "";
        String departmentCode = "";

        if (studentId != null && studentId.length() >= 4) {
            batchCode = studentId.substring(0, 2);
            departmentCode = studentId.substring(2, 4);
        }

        this.deptCode = departmentCode;

        switch (batchCode) {
            case "24":
                session = "2024-25";
                levelTerm = "Level 1 / Term 2";
                currentLevel = 1;
                currentTerm = 2;
                break;
            case "23":
                session = "2023-24";
                levelTerm = "Level 2 / Term 1";
                currentLevel = 2;
                currentTerm = 1;
                break;
            case "22":
                session = "2022-23";
                levelTerm = "Level 2 / Term 2";
                currentLevel = 2;
                currentTerm = 2;
                break;
            case "21":
                session = "2021-22";
                levelTerm = "Level 3 / Term 1";
                currentLevel = 3;
                currentTerm = 1;
                break;
            case "20":
                session = "2020-21";
                levelTerm = "Level 4 / Term 1";
                currentLevel = 4;
                currentTerm = 1;
                break;
            default:
                session = "Unknown";
                levelTerm = "Unknown";
                currentLevel = 1;
                currentTerm = 1;
        }

        String emailDomain;
        switch (departmentCode) {
            case "01":
                department = "Department of Architecture";
                emailDomain = "ugrad.arch.buet.ac.bd";
                break;
            case "18":
                department = "Department of Biomedical Engineering";
                emailDomain = "ugrad.bme.buet.ac.bd";
                break;
            case "04":
                department = "Department of Civil Engineering";
                emailDomain = "ugrad.ce.buet.ac.bd";
                break;
            case "02":
                department = "Department of Chemical Engineering";
                emailDomain = "ugrad.che.buet.ac.bd";
                break;
            case "05":
                department = "Department of Computer Science and Engineering";
                emailDomain = "ugrad.cse.buet.ac.bd";
                break;
            case "06":
                department = "Department of Electrical and Electronic Engineering";
                emailDomain = "ugrad.eee.buet.ac.bd";
                break;
            case "08":
                department = "Department of Industrial and Production Engineering";
                emailDomain = "ugrad.ipe.buet.ac.bd";
                break;
            case "10":
                department = "Department of Mechanical Engineering";
                emailDomain = "ugrad.me.buet.ac.bd";
                break;
            case "11":
                department = "Department of Materials and Metallurgical Engineering";
                emailDomain = "ugrad.mme.buet.ac.bd";
                break;
            case "12":
                department = "Department of Naval Architecture and Marine Engineering";
                emailDomain = "ugrad.name.buet.ac.bd";
                break;
            case "15":
                department = "Department of Urban and Regional Planning";
                emailDomain = "ugrad.urp.buet.ac.bd";
                break;
            case "16":
                department = "Department of Water Resources Engineering";
                emailDomain = "ugrad.wre.buet.ac.bd";
                break;
            case "17":
                department = "Department of Nanomaterials and Ceramic Engineering";
                emailDomain = "ugrad.nce.buet.ac.bd";
                break;
            default:
                department = "Department Not Assigned";
                emailDomain = "student.buet.ac.bd";
        }

        String email = studentId + "@" + emailDomain;

        sidebarNameLabel.setText(fullName);
        sidebarIdLabel.setText(studentId + " | " + levelTerm);

        homeNameLabel.setText(fullName);
        homeIdLabel.setText(studentId);
        homeDeptLabel.setText(department);
        homeSessionLabel.setText(session);
        homeLevelTermLabel.setText(levelTerm);
        homeEmailLabel.setText(email);

        infoNameLabel.setText(fullName);
        infoIdLabel.setText(studentId);
        infoDeptLabel.setText(department);
        infoSessionLabel.setText(session);
        infoLevelTermLabel.setText(levelTerm);
        infoEmailLabel.setText(email);
        infoMobileLabel.setText("Not added yet");
        infoHallLabel.setText("Not added yet");
    }

    @FXML
    private void showHome() {
        switchTo(homeSection);
    }

    @FXML
    private void showPersonalInfo() {
        switchTo(personalSection);
    }

    @FXML
    private void showGrades() {
        hideAll();
        gradesContentBox.getChildren().clear();

        // buildGradesPanel returns a ScrollPane wrapping a VBox;
        // extract the VBox content and place it directly in our scrollable section
        ScrollPane built = buildGradesPanel();
        if (built.getContent() instanceof VBox vb) {
            gradesContentBox.getChildren().add(vb);
        } else {
            gradesContentBox.getChildren().add(built);
        }

        gradesSection.setVisible(true);
        gradesSection.setManaged(true);
    }

    @FXML
    private void showDues() {
        hideAll();
        duesContentBox.getChildren().clear();

        ScrollPane built = buildDuesPanel();
        if (built.getContent() instanceof VBox vb) {
            duesContentBox.getChildren().add(vb);
        } else {
            duesContentBox.getChildren().add(built);
        }

        duesSection.setVisible(true);
        duesSection.setManaged(true);
    }

    private void switchTo(Region node) {
        hideAll();
        node.setVisible(true);
        node.setManaged(true);
    }

    private void hideAll() {
        for (Region r : new Region[]{homeSection, personalSection, gradesSection, duesSection}) {
            r.setVisible(false);
            r.setManaged(false);
        }
    }

    private ScrollPane buildGradesPanel() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color:#1a1a1a;");

        VBox header = new VBox(4);
        header.setStyle("-fx-background-color:#222222;-fx-border-color:transparent transparent transparent #cc0000;-fx-border-width:0 0 0 4;-fx-padding:12;-fx-background-radius:4;");

        Label title = new Label("View Grades");
        title.setStyle("-fx-text-fill:white;-fx-font-size:20px;-fx-font-weight:bold;");

        Label subtitle = new Label("Select a term to view your grade sheet");
        subtitle.setStyle("-fx-text-fill:#aaaaaa;-fx-font-size:12px;");

        header.getChildren().addAll(title, subtitle);

        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color:#2d2d2d;-fx-padding:20;-fx-background-radius:6;-fx-border-color:#3a3a3a;-fx-border-radius:6;");

        Label lbl = new Label("Session  |  Level / Term");
        lbl.setStyle("-fx-text-fill:#aaaaaa;-fx-font-size:13px;");

        ComboBox<String> box = new ComboBox<>();
        box.setStyle("-fx-background-color:#3a3a3a;-fx-text-fill:white;-fx-font-size:13px;-fx-pref-width:260;");
        box.setPromptText("Select a Level/Term");

        int[][] all = {
                {1, 1}, {1, 2}, {2, 1}, {2, 2},
                {3, 1}, {3, 2}, {4, 1}, {4, 2}
        };

        List<int[]> allowedTerms = new ArrayList<>();
        for (int[] lt : all) {
            int l = lt[0];
            int trm = lt[1];
            if (l < currentLevel || (l == currentLevel && trm <= currentTerm)) {
                box.getItems().add("Level-" + l + ", Term-" + trm + "  |  Session " + session);
                allowedTerms.add(new int[]{l, trm});
            }
        }

        Button btn = new Button("Show");
        btn.setStyle("-fx-background-color:#cc0000;-fx-text-fill:white;-fx-font-weight:bold;-fx-padding:10 24;-fx-background-radius:4;-fx-cursor:hand;");

        Button refreshBtn = new Button("Refresh Results");
        refreshBtn.setStyle("-fx-background-color:#3f3f3f;-fx-text-fill:white;-fx-font-weight:bold;-fx-padding:10 18;-fx-background-radius:4;-fx-cursor:hand;");

        VBox res = new VBox(0);
        res.setStyle("-fx-background-color:#1a1a1a;");

        Runnable reloadSelectedTerm = () -> {
            int idx = box.getSelectionModel().getSelectedIndex();
            if (idx < 0) return;

            int level = allowedTerms.get(idx)[0];
            int term = allowedTerms.get(idx)[1];

            res.getChildren().clear();
            res.getChildren().add(buildGradeSheet(level, term));
        };

        btn.setOnAction(e -> reloadSelectedTerm.run());
        refreshBtn.setOnAction(e -> reloadSelectedTerm.run());

        row.getChildren().addAll(lbl, box, btn, refreshBtn);
        root.getChildren().addAll(header, row, res);

        ScrollPane sp = new ScrollPane(root);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color:#1a1a1a;-fx-background:#1a1a1a;");
        return sp;
    }

    private VBox buildGradeSheet(int level, int term) {
        List<CourseRow> courses = loadCourseRowsFromDB(level, term);

        VBox sheet = new VBox(0);
        sheet.setStyle("-fx-background-color:#2d2d2d;-fx-padding:24;-fx-background-radius:8;-fx-border-color:#3a3a3a;-fx-border-radius:8;");

        VBox sh = new VBox(4);
        sh.setAlignment(Pos.CENTER);
        sh.setPadding(new Insets(0, 0, 16, 0));

        Label st = new Label("Level-" + level + ", Term-" + term + "  |  Session " + session);
        st.setStyle("-fx-text-fill:white;-fx-font-size:15px;-fx-font-weight:bold;");

        Label sd = new Label(department);
        sd.setStyle("-fx-text-fill:#f0c040;-fx-font-size:13px;-fx-font-weight:bold;");

        sh.getChildren().addAll(st, sd);

        HBox sr = new HBox(40);
        sr.setPadding(new Insets(0, 0, 14, 0));

        VBox nc = new VBox(2);
        nc.getChildren().addAll(lbl2("Student's Name"), val2(fullName));

        VBox ic = new VBox(2);
        ic.getChildren().addAll(lbl2("Student No"), val2(studentId));

        sr.getChildren().addAll(nc, ic);

        TableView<CourseRow> tbl = new TableView<>();
        tbl.setStyle("-fx-background-color:#1e1e1e;-fx-border-color:#444;-fx-border-radius:6;");
        tbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tbl.setPrefHeight(Math.max(courses.size() * 38 + 42, 150));
        tbl.setFixedCellSize(38);

        tbl.getColumns().addAll(
                mkCol("Course No", "courseNo", 110),
                mkCol("Course Title", "courseTitle", 0),
                mkCol("Credit Hours", "creditHours", 100),
                mkCol("Grade", "grade", 80),
                mkCol("Grade Point", "gradePoint", 100)
        );

        tbl.setItems(FXCollections.observableArrayList(courses));

        double tc = 0;
        double tg = 0;

        for (CourseRow r : courses) {
            double c = parseDoubleSafe(r.getCreditHours());
            double g = parseDoubleSafe(r.getGradePoint());
            tc += c;
            tg += c * g;
        }

        double gpa = tc > 0 ? tg / tc : 0.0;

        VBox sum = new VBox(8);
        sum.setPadding(new Insets(16, 0, 0, 0));

        Label note = new Label("* Only published grades are shown here. Press Refresh Results to load the latest published data from the server.");
        note.setStyle("-fx-text-fill:#aaaaaa;-fx-font-size:11px;-fx-font-style:italic;");

        HBox sg = new HBox(60);
        sg.setAlignment(Pos.CENTER_LEFT);

        VBox ls = new VBox(6);
        addSR(ls, "Registered Credit Hours in this Term :", String.format("%.2f", tc));
        addSR(ls, "Credit Hours Earned in this Term :", String.format("%.2f", tc));
        addSR(ls, "Total Credit Hours :", String.format("%.2f", tc));

        VBox rs = new VBox(8);

        HBox gr = new HBox(15);
        gr.setAlignment(Pos.CENTER_LEFT);
        Label gl = new Label("GPA:");
        gl.setStyle("-fx-text-fill:#aaaaaa;-fx-font-size:13px;");
        Label gv = new Label(String.format("%.2f", gpa));
        gv.setStyle("-fx-text-fill:#f0c040;-fx-font-size:20px;-fx-font-weight:bold;");
        gr.getChildren().addAll(gl, gv);

        HBox cr2 = new HBox(15);
        cr2.setAlignment(Pos.CENTER_LEFT);
        Label cl = new Label("CGPA:");
        cl.setStyle("-fx-text-fill:#aaaaaa;-fx-font-size:13px;");
        Label cv = new Label(String.format("%.2f", gpa));
        cv.setStyle("-fx-text-fill:#f0c040;-fx-font-size:20px;-fx-font-weight:bold;");
        cr2.getChildren().addAll(cl, cv);

        rs.getChildren().addAll(gr, cr2);
        sg.getChildren().addAll(ls, rs);
        sum.getChildren().addAll(note, sg);

        sheet.getChildren().addAll(sh, sr, tbl, sum);
        return sheet;
    }

    private List<CourseRow> loadCourseRowsFromDB(int level, int term) {
        List<CourseRow> rows = new ArrayList<>();
        String departmentShort = getDepartmentShortCode(deptCode);

        String sql = """
                SELECT
                    c.id,
                    c.course_code,
                    c.course_title,
                    c.credit_hours,
                    COALESCE(gr.letter_grade, '0.00') AS letter_grade,
                    COALESCE(gr.grade_point, 0.00) AS grade_point
                FROM courses c
                LEFT JOIN (
                    SELECT
                        co.course_id,
                        gr.letter_grade,
                        gr.grade_point
                    FROM enrollments e
                    JOIN course_offerings co ON e.offering_id = co.id
                    LEFT JOIN grade_records gr
                        ON gr.student_username = e.student_username
                       AND gr.offering_id = e.offering_id
                       AND COALESCE(gr.published, FALSE) = TRUE
                    WHERE e.student_username = ?
                ) gr ON gr.course_id = c.id
                WHERE c.department = ?
                  AND c.level = ?
                  AND c.term = ?
                ORDER BY c.course_code
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, studentId);
            ps.setString(2, departmentShort);
            ps.setInt(3, level);
            ps.setInt(4, term);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new CourseRow(
                            rs.getString("course_code"),
                            rs.getString("course_title"),
                            String.format("%.2f", rs.getDouble("credit_hours")),
                            normalizeGrade(rs.getString("letter_grade")),
                            String.format("%.2f", rs.getDouble("grade_point"))
                    ));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rows;
    }

    private String getDepartmentShortCode(String code) {
        switch (code) {
            case "01": return "ARCH";
            case "02": return "ChE";
            case "04": return "CE";
            case "05": return "CSE";
            case "06": return "EEE";
            case "08": return "IPE";
            case "10": return "ME";
            case "11": return "MME";
            case "12": return "NAME";
            case "15": return "URP";
            case "16": return "WRE";
            case "17": return "NCE";
            case "18": return "BME";
            default: return "CSE";
        }
    }

    private String normalizeGrade(String grade) {
        if (grade == null || grade.isBlank() || "-".equals(grade.trim())) {
            return "0.00";
        }
        return grade.trim();
    }

    private double parseDoubleSafe(String text) {
        try {
            return Double.parseDouble(text);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private Label lbl2(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-text-fill:#aaaaaa;-fx-font-size:12px;");
        return l;
    }

    private Label val2(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-text-fill:white;-fx-font-size:14px;-fx-font-weight:bold;");
        return l;
    }

    private TableColumn<CourseRow, String> mkCol(String h, String p, double w) {
        TableColumn<CourseRow, String> c = new TableColumn<>(h);
        c.setCellValueFactory(new PropertyValueFactory<>(p));
        c.setSortable(false);
        if (w > 0) c.setMinWidth(w);
        return c;
    }

    private void addSR(VBox b, String lbl, String val) {
        HBox r = new HBox(10);
        r.setAlignment(Pos.CENTER_LEFT);

        Label l = new Label(lbl);
        l.setStyle("-fx-text-fill:#aaaaaa;-fx-font-size:13px;-fx-min-width:300;");

        Label v = new Label(val);
        v.setStyle("-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:bold;");

        r.getChildren().addAll(l, v);
        b.getChildren().add(r);
    }

    private ScrollPane buildDuesPanel() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color:#1a1a1a;");

        VBox hdr = new VBox(4);
        hdr.setStyle("-fx-background-color:#222222;-fx-border-color:transparent transparent transparent #cc0000;-fx-border-width:0 0 0 4;-fx-padding:12;-fx-background-radius:4;");

        Label t = new Label("Dues & Payments");
        t.setStyle("-fx-text-fill:white;-fx-font-size:20px;-fx-font-weight:bold;");

        Label s = new Label("Outstanding dues for the current session");
        s.setStyle("-fx-text-fill:#aaaaaa;-fx-font-size:12px;");

        hdr.getChildren().addAll(t, s);

        HBox cards = new HBox(15);
        cards.getChildren().addAll(
                dCard("Total Outstanding", "৳ 0", "#66cc88"),
                dCard("Clearance Dues", "৳ 0", "#c8862a"),
                dCard("Registration Dues", "৳ 0", "#2a7ac8"),
                dCard("Hall Dues", "৳ 0", "#2aa87a"),
                dCard("Dining Dues", "৳ 0", "#8a2ac8")
        );

        VBox tc = new VBox(14);
        tc.setStyle("-fx-background-color:#2d2d2d;-fx-padding:20;-fx-background-radius:8;-fx-border-color:#3a3a3a;-fx-border-radius:8;");

        Label tt = new Label("Due Details");
        tt.setStyle("-fx-text-fill:#f0c040;-fx-font-size:16px;-fx-font-weight:bold;");

        TableView<DueRow> tbl = new TableView<>();
        tbl.setStyle("-fx-background-color:#1e1e1e;-fx-border-color:#444;-fx-border-radius:6;");
        tbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tbl.setPrefHeight(210);
        tbl.setFixedCellSize(38);

        TableColumn<DueRow, String> cs = mkDCol("Status", "status", 110);
        cs.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String i, boolean e) {
                super.updateItem(i, e);
                if (e || i == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(i);
                    setStyle(i.equals("Pending")
                            ? "-fx-text-fill:#ff6666;-fx-font-weight:bold;-fx-alignment:CENTER;"
                            : "-fx-text-fill:#66cc88;-fx-font-weight:bold;-fx-alignment:CENTER;");
                }
            }
        });

        tbl.getColumns().addAll(
                mkDCol("Due Type", "type", 160),
                mkDCol("Description", "description", 0),
                mkDCol("Amount (BDT)", "amount", 130),
                cs
        );

        tbl.setItems(FXCollections.observableArrayList(
                new DueRow("No dues", "No due records connected yet", "৳ 0", "Cleared")
        ));

        Label note = new Label("⚠  Dues module is still placeholder-based.");
        note.setStyle("-fx-text-fill:#ffaa44;-fx-font-size:12px;-fx-font-style:italic;-fx-padding:8 0 0 0;");

        tc.getChildren().addAll(tt, tbl, note);
        root.getChildren().addAll(hdr, cards, tc);

        ScrollPane sp = new ScrollPane(root);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color:#1a1a1a;-fx-background:#1a1a1a;");
        return sp;
    }

    private VBox dCard(String l, String v, String ac) {
        VBox c = new VBox(6);
        c.setAlignment(Pos.CENTER_LEFT);
        c.setPadding(new Insets(16));
        HBox.setHgrow(c, Priority.ALWAYS);
        c.setStyle("-fx-background-color:#2d2d2d;-fx-background-radius:8;-fx-border-color:" + ac + ";-fx-border-width:0 0 3 0;-fx-border-radius:8;");

        Label ll = new Label(l);
        ll.setStyle("-fx-text-fill:#aaaaaa;-fx-font-size:12px;");

        Label vl = new Label(v);
        vl.setStyle("-fx-text-fill:" + ac + ";-fx-font-size:18px;-fx-font-weight:bold;");

        c.getChildren().addAll(ll, vl);
        return c;
    }

    private TableColumn<DueRow, String> mkDCol(String h, String p, double w) {
        TableColumn<DueRow, String> c = new TableColumn<>(h);
        c.setCellValueFactory(new PropertyValueFactory<>(p));
        c.setSortable(false);
        if (w > 0) c.setMinWidth(w);
        return c;
    }

    @FXML
    private void handleLogout() {
        try {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            NavigationService.getInstance().openLogin(stage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}