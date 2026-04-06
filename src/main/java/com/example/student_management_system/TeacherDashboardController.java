package com.example.student_management_system;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Set;
import java.util.HashSet;

import java.time.LocalDate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeacherDashboardController {

    @FXML private BorderPane rootPane;
    @FXML private Label sidebarNameLabel, sidebarIdLabel, sidebarDeptLabel;
    @FXML private Label homeNameLabel, homeIdLabel, homeDeptLabel, homeDesigLabel, homeEmailLabel;
    @FXML private Label profNameLabel, profIdLabel, profDeptLabel, profDesigLabel, profEmailLabel;
    @FXML private Label statCourses, statStudents, statAttendance, statAssignments;
    @FXML private VBox todaysClassesBox, recentActivityBox, profileCourseListBox;
    @FXML private VBox coursesContentBox, attendanceContentBox, marksContentBox;
    @FXML private VBox assignmentsContentBox, announcementsContentBox, studentsContentBox;

    @FXML private ScrollPane homeSection, profileSection;
    @FXML private ScrollPane coursesSection, attendanceSection, marksSection;
    @FXML private ScrollPane assignmentsSection, announcementsSection, studentsSection;

    private String teacherUsername;
    private String teacherName = "Teacher";
    private String teacherId   = "";
    private String department  = "Department";
    private String email       = "";
    private String designation = "Lecturer";

    private final List<String[]> myCourses   = new ArrayList<>(); // [code,title,credits,lt,section]
    private final List<String[]> myStudents  = new ArrayList<>(); // [username,name,att%,lt,courseCode]

    // ── INIT ─────────────────────────────────────────────────────────────

    public void loadTeacherData(String username, String fullName) {
        this.teacherUsername = username;
        this.teacherName     = fullName;
        this.teacherId       = username.toUpperCase();
        this.email           = username.toLowerCase() + "@cse.buet.ac.bd";

        loadTeacherProfile(username);
        loadCoursesFromDB(username);
        loadStudentsFromDB(username);

        sidebarNameLabel.setText(teacherName);
        sidebarIdLabel.setText(email);
        sidebarDeptLabel.setText(abbreviateDept(department));

        homeNameLabel.setText(teacherName);
        homeIdLabel.setText(teacherId);
        homeDeptLabel.setText(department);
        homeDesigLabel.setText(designation);
        homeEmailLabel.setText(email);

        profNameLabel.setText(teacherName);
        profIdLabel.setText(teacherId);
        profDeptLabel.setText(department);
        profDesigLabel.setText(designation);
        profEmailLabel.setText(email);

        statCourses.setText(String.valueOf(myCourses.size()));
        statStudents.setText(String.valueOf(countDistinctStudents()));
        statAttendance.setText(calculateAverageAttendance());
        statAssignments.setText("0");

        buildTodaysClasses();
        buildRecentActivity();
        buildProfileCourseList();
    }

    private void loadTeacherProfile(String username) {
        String sql = "SELECT COALESCE(t.department,'CSE') AS dept, COALESCE(t.designation,'Lecturer') AS desig " +
                     "FROM teachers t WHERE t.username = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    department  = expandDept(rs.getString("dept"));
                    designation = rs.getString("desig");
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadCoursesFromDB(String username) {
        myCourses.clear();
        String sql = """
                SELECT c.course_code, c.course_title, c.credit_hours,
                       CONCAT('L', co.level, 'T', co.term) AS lt, co.section_name, co.id
                FROM course_offerings co
                JOIN courses c ON co.course_id = c.id
                WHERE co.teacher_username = ?
                ORDER BY co.level, co.term, co.section_name, c.course_code
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    myCourses.add(new String[]{
                            rs.getString("course_code"),
                            rs.getString("course_title"),
                            String.format("%.2f", rs.getDouble("credit_hours")),
                            rs.getString("lt"),
                            rs.getString("section_name"),
                            rs.getString("id")   // index 5 = offering_id
                    });
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadStudentsFromDB(String username) {
        myStudents.clear();
        String sql = """
                SELECT e.student_username,
                       COALESCE(u.full_name, e.student_username) AS full_name,
                       COALESCE(
                           CONCAT(ROUND(100 * SUM(CASE WHEN ar.status <> 'ABSENT' THEN 1 ELSE 0 END)
                           / NULLIF(COUNT(ar.id), 0)), '%'), '0%') AS attendance_pct,
                       CONCAT('L', co.level, 'T', co.term) AS lt,
                       c.course_code
                FROM enrollments e
                JOIN course_offerings co ON e.offering_id = co.id
                JOIN courses c ON co.course_id = c.id
                LEFT JOIN users u ON u.username = e.student_username
                LEFT JOIN attendance_records ar
                    ON ar.student_username = e.student_username
                   AND ar.offering_id = e.offering_id
                WHERE co.teacher_username = ? AND e.status = 'ENROLLED'
                GROUP BY e.student_username, u.full_name, co.level, co.term, c.course_code
                ORDER BY e.student_username, c.course_code
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    myStudents.add(new String[]{
                            rs.getString("student_username"),
                            rs.getString("full_name"),
                            rs.getString("attendance_pct"),
                            rs.getString("lt"),
                            rs.getString("course_code")
                    });
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── MARKS SECTION (fully dynamic) ────────────────────────────────────

    private void buildMarksSection() {
        marksContentBox.getChildren().clear();

        // ---- course selector ----
        VBox selector = card("Select Course");
        ComboBox<String> courseBox = styledCombo(320);
        List<String[]> offerings = DataService.getInstance().getTeacherOfferings(teacherUsername);

        for (String[] o : offerings) {
            courseBox.getItems().add(o[1] + "  " + o[2] + " (" + o[3] + " / Sec " + o[4] + ")");
        }
        if (!courseBox.getItems().isEmpty()) courseBox.getSelectionModel().selectFirst();

        Button loadBtn = btn("Load Students & Grades", "#cc0000");
        Button refreshBtn = btn("Refresh", "#3f3f3f");

        HBox selRow = new HBox(12, lbl("Course:"), courseBox, loadBtn, refreshBtn);
        selRow.setAlignment(Pos.CENTER_LEFT);
        selector.getChildren().add(selRow);
        marksContentBox.getChildren().add(selector);

        // ---- grade sheet card ----
        VBox gradeCard = card("Grade Sheet  (Attendance /30 · Class Test /60 · Written Main Exam /210 = /300)");
        Label hint = new Label("Follow BUET style: Attendance 30, CT 60, Written Main Exam 210. Saving marks keeps them unpublished until you click Publish Grades.");
        hint.setStyle("-fx-text-fill:#aaaaaa;-fx-font-style:italic;-fx-font-size:12px;");
        gradeCard.getChildren().add(hint);

        // dynamic table holder
        VBox tableHolder = new VBox(0);
        gradeCard.getChildren().add(tableHolder);

        HBox btnRow = new HBox(12);
        btnRow.getChildren().addAll(
                btn("💾 Save All Grades", "#2aa87a"),
                btn("✅ Publish Grades", "#cc0000")
        );
        gradeCard.getChildren().add(btnRow);
        marksContentBox.getChildren().add(gradeCard);

        // ---- load button action ----
        loadBtn.setOnAction(e -> {
            int idx = courseBox.getSelectionModel().getSelectedIndex();
            if (idx < 0 || idx >= offerings.size()) return;

            String[] offering = offerings.get(idx);
            int offeringId = Integer.parseInt(offering[0]);

            List<String[]> rows = DataService.getInstance()
                    .getStudentsForOfferingWithGrades(offeringId);

            tableHolder.getChildren().clear();

            // editable rows: [username, name, att, ct, asgn, mid, final, total, grade, gp, published]
            List<String[]> editData = new ArrayList<>();
            List<TextField[]> fieldRefs = new ArrayList<>();

            for (String[] row : rows) {
                TextField attF   = numField(row[2], 60);
                TextField ctF    = numField(row[3], 60);
                TextField mainF  = numField(row[6], 70);
                Label totalLbl   = val(row[7]);
                Label gradeLbl   = val(row[8]);
                Label gpLbl      = val(row[9]);

                fieldRefs.add(new TextField[]{attF, ctF, mainF});

                Runnable recalc = () -> {
                    try {
                        double a = Math.max(0, Math.min(30, parseD(attF.getText())));
                        double ct2 = Math.max(0, Math.min(60, parseD(ctF.getText())));
                        double main = Math.max(0, Math.min(210, parseD(mainF.getText())));
                        double total300 = a + ct2 + main;
                        double total100 = (total300 / 300.0) * 100.0;
                        String[] lg = letterGrade(total100);
                        totalLbl.setText(String.format("%.2f", total300));
                        gradeLbl.setText(lg[0]);
                        gpLbl.setText(lg[1]);
                        gradeLbl.setStyle("-fx-text-fill:" + gradeColor(lg[0]) + ";-fx-font-weight:bold;");
                    } catch (Exception ignored) {}
                };
                attF.textProperty().addListener((obs, o2, n) -> recalc.run());
                ctF.textProperty().addListener((obs, o2, n) -> recalc.run());
                mainF.textProperty().addListener((obs, o2, n) -> recalc.run());
                gradeLbl.setStyle("-fx-text-fill:" + gradeColor(row[8]) + ";-fx-font-weight:bold;");

                editData.add(row);

                HBox studentRow = new HBox(8);
                studentRow.setPadding(new Insets(8, 12, 8, 12));
                studentRow.setAlignment(Pos.CENTER_LEFT);
                studentRow.setStyle("-fx-background-color:#252525;-fx-background-radius:4;-fx-border-color:transparent transparent #333 transparent;-fx-border-width:0 0 1 0;");

                Label idLbl = new Label(row[0]);
                idLbl.setStyle("-fx-text-fill:#aaaaaa;-fx-font-size:12px;-fx-min-width:90;");
                Label nameLbl = new Label(row[1]);
                nameLbl.setStyle("-fx-text-fill:white;-fx-font-size:13px;-fx-min-width:200;");
                HBox.setHgrow(nameLbl, Priority.ALWAYS);

                studentRow.getChildren().addAll(idLbl, nameLbl,
                        miniLbl("Att/30"), attF,
                        miniLbl("CT/60"), ctF,
                        miniLbl("Written/210"), mainF,
                        miniLbl("Total/300"), totalLbl,
                        miniLbl("Grade"), gradeLbl,
                        miniLbl("GP"), gpLbl);

                tableHolder.getChildren().add(studentRow);
            }

            if (rows.isEmpty()) {
                Label empty = new Label("No enrolled students found for this offering.");
                empty.setStyle("-fx-text-fill:#aaaaaa;-fx-font-size:13px;-fx-padding:16;");
                tableHolder.getChildren().add(empty);
            }

            // Save handler
            ((Button) btnRow.getChildren().get(0)).setOnAction(ev -> {
                int saved = 0;
                for (int i = 0; i < editData.size(); i++) {
                    try {
                        String sUser = editData.get(i)[0];
                        TextField[] f = fieldRefs.get(i);
                        double att  = parseD(f[0].getText());
                        double ct2  = parseD(f[1].getText());
                        double main = parseD(f[2].getText());
                        DataService.getInstance().saveOrUpdateGrade(sUser, offeringId, att, ct2, main);
                        saved++;
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
                showAlert(Alert.AlertType.INFORMATION, "Grades Saved",
                        "Successfully saved grades for " + saved + " student(s).");
            });

            // Publish handler
            ((Button) btnRow.getChildren().get(1)).setOnAction(ev -> {
                try (Connection con = DBConnection.getConnection();
                     PreparedStatement ps = con.prepareStatement(
                             "UPDATE grade_records SET published = TRUE WHERE offering_id = ?")) {
                    ps.setInt(1, offeringId);
                    ps.executeUpdate();
                    showAlert(Alert.AlertType.INFORMATION, "Published",
                            "Grades have been published. Students on any connected client can refresh and see the latest results.");
                } catch (Exception ex) { ex.printStackTrace(); }
            });
        });

        refreshBtn.setOnAction(e -> loadBtn.fire());

        // auto-load first course
        if (!offerings.isEmpty()) loadBtn.fire();
    }

    // ── ATTENDANCE SECTION (fully dynamic) ───────────────────────────────

    private void buildAttendanceSection() {
        attendanceContentBox.getChildren().clear();

        VBox selector = card("Mark Attendance");

        ComboBox<String> courseBox = styledCombo(320);
        List<String[]> offerings = DataService.getInstance().getTeacherOfferings(teacherUsername);
        for (String[] o : offerings) {
            courseBox.getItems().add(o[1] + "  " + o[2] + " (" + o[3] + " / Sec " + o[4] + ")");
        }
        if (!courseBox.getItems().isEmpty()) courseBox.getSelectionModel().selectFirst();

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setStyle("-fx-background-color:#3a3a3a;-fx-text-fill:white;");

        Button loadBtn = btn("Load Students", "#cc0000");

        HBox selRow = new HBox(12, lbl("Course:"), courseBox, lbl("Date:"), datePicker, loadBtn);
        selRow.setAlignment(Pos.CENTER_LEFT);
        selector.getChildren().add(selRow);
        attendanceContentBox.getChildren().add(selector);

        VBox sheetCard = card("Attendance Sheet");
        VBox studentRows = new VBox(4);
        sheetCard.getChildren().add(studentRows);
        Button saveAttBtn = btn("💾 Save Attendance", "#2aa87a");
        sheetCard.getChildren().add(saveAttBtn);
        attendanceContentBox.getChildren().add(sheetCard);

        List<String[]> enrolledStudents = new ArrayList<>();
        List<ToggleGroup> toggleGroups  = new ArrayList<>();

        loadBtn.setOnAction(e -> {
            int idx = courseBox.getSelectionModel().getSelectedIndex();
            if (idx < 0 || idx >= offerings.size()) return;
            String[] offering = offerings.get(idx);
            int offeringId = Integer.parseInt(offering[0]);

            studentRows.getChildren().clear();
            enrolledStudents.clear();
            toggleGroups.clear();

            // header
            HBox hdr = new HBox(8);
            hdr.setPadding(new Insets(6, 12, 6, 12));
            hdr.setStyle("-fx-background-color:#1e1e1e;");
            hdr.getChildren().addAll(
                    hdrLbl("Student ID", 100), hdrLbl("Name", 220),
                    hdrLbl("Present", 80), hdrLbl("Absent", 80), hdrLbl("Late", 80));
            studentRows.getChildren().add(hdr);

            // load existing attendance for date
            Map<String, String> existing = new HashMap<>();
            LocalDate date = datePicker.getValue();
            String sql = "SELECT student_username, status FROM attendance_records WHERE offering_id = ? AND class_date = ?";
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps2 = con.prepareStatement(sql)) {
                ps2.setInt(1, offeringId);
                ps2.setDate(2, Date.valueOf(date));
                try (ResultSet rs2 = ps2.executeQuery()) {
                    while (rs2.next()) existing.put(rs2.getString(1), rs2.getString(2));
                }
            } catch (Exception ex) { ex.printStackTrace(); }

            String sql2 = """
                    SELECT e.student_username, COALESCE(u.full_name, e.student_username) AS full_name
                    FROM enrollments e
                    LEFT JOIN users u ON u.username = e.student_username
                    WHERE e.offering_id = ? AND e.status = 'ENROLLED'
                    ORDER BY e.student_username
                    """;
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps2 = con.prepareStatement(sql2)) {
                ps2.setInt(1, offeringId);
                try (ResultSet rs2 = ps2.executeQuery()) {
                    while (rs2.next()) {
                        String sid  = rs2.getString("student_username");
                        String snam = rs2.getString("full_name");
                        enrolledStudents.add(new String[]{sid, snam, offering[0]});

                        ToggleGroup tg = new ToggleGroup();
                        RadioButton present = radioBtn("Present", "#66cc88", tg);
                        RadioButton absent  = radioBtn("Absent",  "#ff6666", tg);
                        RadioButton late    = radioBtn("Late",    "#ffaa44", tg);

                        String prev = existing.getOrDefault(sid, "PRESENT");
                        if      (prev.equals("ABSENT")) absent.setSelected(true);
                        else if (prev.equals("LATE"))   late.setSelected(true);
                        else                             present.setSelected(true);

                        toggleGroups.add(tg);

                        HBox row = new HBox(8);
                        row.setPadding(new Insets(8, 12, 8, 12));
                        row.setAlignment(Pos.CENTER_LEFT);
                        row.setStyle("-fx-background-color:#252525;-fx-background-radius:4;");

                        Label idL  = new Label(sid);  idL.setStyle("-fx-text-fill:#aaaaaa;-fx-min-width:100;");
                        Label nmL  = new Label(snam); nmL.setStyle("-fx-text-fill:white;-fx-min-width:220;");
                        row.getChildren().addAll(idL, nmL, present, absent, late);
                        studentRows.getChildren().add(row);
                    }
                }
            } catch (Exception ex) { ex.printStackTrace(); }

            if (enrolledStudents.isEmpty()) {
                studentRows.getChildren().add(noDataLabel("No enrolled students found."));
            }
        });

        saveAttBtn.setOnAction(e -> {
            int idx = courseBox.getSelectionModel().getSelectedIndex();
            if (idx < 0 || idx >= offerings.size()) return;
            String[] offering = offerings.get(idx);
            int offeringId = Integer.parseInt(offering[0]);
            LocalDate date = datePicker.getValue();

            if (enrolledStudents.isEmpty()) { showAlert(Alert.AlertType.WARNING, "No Students", "Load students first."); return; }

            String upsert = """
                    INSERT INTO attendance_records (student_username, offering_id, class_date, status)
                    VALUES (?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE status = VALUES(status)
                    """;
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps2 = con.prepareStatement(upsert)) {
                for (int i = 0; i < enrolledStudents.size(); i++) {
                    String sid = enrolledStudents.get(i)[0];
                    Toggle sel = toggleGroups.get(i).getSelectedToggle();
                    String status = "PRESENT";
                    if (sel != null) status = ((RadioButton) sel).getText().toUpperCase();
                    ps2.setString(1, sid);
                    ps2.setInt(2, offeringId);
                    ps2.setDate(3, Date.valueOf(date));
                    ps2.setString(4, status);
                    ps2.addBatch();
                }
                ps2.executeBatch();
                showAlert(Alert.AlertType.INFORMATION, "Saved",
                        "Attendance saved for " + date + " (" + enrolledStudents.size() + " students).");
                // reload student list to refresh attendance %
                loadStudentsFromDB(teacherUsername);
                statAttendance.setText(calculateAverageAttendance());
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        // auto-load first
        if (!offerings.isEmpty()) loadBtn.fire();
    }

    // ── COURSES SECTION ───────────────────────────────────────────────────

    private void buildCoursesSection() {
        coursesContentBox.getChildren().clear();

        if (myCourses.isEmpty()) {
            VBox empty = card("My Assigned Courses");
            empty.getChildren().add(noDataLabel("No courses assigned yet. Contact admin to get courses assigned."));
            coursesContentBox.getChildren().add(empty);
            return;
        }

        for (String[] c : myCourses) {
            VBox card = new VBox(12);
            card.setPadding(new Insets(18));
            card.setStyle("-fx-background-color:#2d2d2d;-fx-background-radius:8;-fx-border-color:#3a3a3a;-fx-border-radius:8;");

            HBox titleRow = new HBox(15);
            titleRow.setAlignment(Pos.CENTER_LEFT);

            Label no = new Label(c[0]);
            no.setStyle("-fx-text-fill:#f0c040;-fx-font-size:16px;-fx-font-weight:bold;");
            Label nm = new Label(c[1]);
            nm.setStyle("-fx-text-fill:white;-fx-font-size:15px;-fx-font-weight:bold;");
            HBox.setHgrow(nm, Priority.ALWAYS);
            Label cr = lbl(c[2] + " Credit Hours");
            titleRow.getChildren().addAll(no, nm, cr);

            HBox infoRow = new HBox(12);
            infoRow.setAlignment(Pos.CENTER_LEFT);
            infoRow.getChildren().addAll(chip("Level/Term", c[3]), chip("Section", c[4]));

            // count enrolled students for this offering
            int enr = 0;
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(
                         "SELECT COUNT(*) FROM enrollments WHERE offering_id = ? AND status = 'ENROLLED'")) {
                ps.setInt(1, Integer.parseInt(c[5]));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) enr = rs.getInt(1);
            } catch (Exception ex) { ex.printStackTrace(); }
            infoRow.getChildren().add(chip("Enrolled", String.valueOf(enr)));

            HBox btnRow = new HBox(10);
            Button markAttBtn = btn("Mark Attendance", "#cc0000");
            Button enterMarksBtn = btn("Enter Marks", "#2a7ac8");
            btnRow.getChildren().addAll(markAttBtn, enterMarksBtn);

            markAttBtn.setOnAction(ev -> { buildAttendanceSection(); showAnchor(attendanceSection); });
            enterMarksBtn.setOnAction(ev -> { buildMarksSection(); showAnchor(marksSection); });

            card.getChildren().addAll(titleRow, infoRow, btnRow);
            coursesContentBox.getChildren().add(card);
        }
    }

    // ── STUDENTS SECTION ─────────────────────────────────────────────────

    private void buildStudentsSection() {
        studentsContentBox.getChildren().clear();

        VBox selector = card("Student List by Course");
        ComboBox<String> cb = styledCombo(320);
        cb.getItems().add("All Courses");
        for (String[] c : myCourses)
            cb.getItems().add(c[0] + "  " + c[1] + " (" + c[3] + " / Sec " + c[4] + ")");
        cb.getSelectionModel().selectFirst();

        TextField searchField = textField("Search by ID or name...", 240);

        HBox sr = new HBox(12, lbl("Filter:"), cb, lbl("Search:"), searchField);
        sr.setAlignment(Pos.CENTER_LEFT);
        selector.getChildren().add(sr);
        studentsContentBox.getChildren().add(selector);

        TableView<String[]> tbl = makeTable(380);

        TableColumn<String[], String> colAtt = strCol("Attendance", 2, 110);
        colAtt.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                double pct = Double.parseDouble(item.replace("%", "").trim().isEmpty() ? "0" : item.replace("%", ""));
                String color = pct >= 85 ? "#66cc88" : pct >= 75 ? "#ffaa44" : "#ff6666";
                setStyle("-fx-text-fill:" + color + ";-fx-font-weight:bold;-fx-alignment:CENTER;");
            }
        });

        tbl.getColumns().addAll(
                strCol("Student ID", 0, 120), strCol("Name", 1, 0),
                colAtt, strCol("Level/Term", 3, 110), strCol("Course", 4, 120));

        ObservableList<String[]> allData = FXCollections.observableArrayList(myStudents);
        tbl.setItems(allData);
        studentsContentBox.getChildren().add(tbl);

        // filter logic
        Runnable applyFilter = () -> {
            String selCourse = cb.getSelectionModel().getSelectedItem();
            String searchTxt = searchField.getText().toLowerCase().trim();
            List<String[]> filtered = new ArrayList<>();
            for (String[] s : myStudents) {
                boolean courseOk = selCourse == null || selCourse.equals("All Courses") || s[4].equalsIgnoreCase(selCourse.split("\\s")[0]);
                boolean searchOk = searchTxt.isEmpty() || s[0].toLowerCase().contains(searchTxt) || s[1].toLowerCase().contains(searchTxt);
                if (courseOk && searchOk) filtered.add(s);
            }
            tbl.setItems(FXCollections.observableArrayList(filtered));
        };
        cb.setOnAction(e -> applyFilter.run());
        searchField.textProperty().addListener((obs, o, n) -> applyFilter.run());
    }

    // ── ANNOUNCEMENTS SECTION ─────────────────────────────────────────────

    private void buildAnnouncementsSection() {
        announcementsContentBox.getChildren().clear();

        VBox createCard = card("Post New Announcement");
        ComboBox<String> cb = styledCombo(260);
        cb.getItems().add("All My Courses");
        for (String[] c : myCourses)
            cb.getItems().add(c[0] + "  " + c[1] + " (" + c[3] + " / Sec " + c[4] + ")");
        cb.getSelectionModel().selectFirst();

        TextField subjectField = textField("Subject / Title", 300);
        TextArea msgArea = new TextArea();
        msgArea.setPromptText("Write your announcement here...");
        msgArea.setStyle("-fx-background-color:#2a2a2a;-fx-border-color:#444;-fx-border-radius:4;-fx-background-radius:4;-fx-text-fill:white;-fx-control-inner-background:#2a2a2a;");
        msgArea.setPrefHeight(120);

        HBox r1 = new HBox(12, lbl("Target:"), cb, lbl("Subject:"), subjectField);
        r1.setAlignment(Pos.CENTER_LEFT);

        Button postBtn = btn("📢 Post Announcement", "#cc0000");

        VBox listCard = card("Recent Announcements");
        VBox listBox = new VBox(8);
        listCard.getChildren().add(listBox);

        // load existing
        Runnable loadAnnouncements = () -> {
            listBox.getChildren().clear();
            String sql = """
                SELECT subject, message, target_course, created_at
                FROM announcements
                WHERE teacher_username = ?
                ORDER BY created_at DESC
                LIMIT 20
                """;
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, teacherUsername);
                try (ResultSet rs = ps.executeQuery()) {
                    boolean any = false;
                    while (rs.next()) {
                        any = true;
                        VBox item = new VBox(4);
                        item.setPadding(new Insets(10, 14, 10, 14));
                        item.setStyle("-fx-background-color:#252525;-fx-background-radius:6;");
                        Label subj = new Label(rs.getString("subject"));
                        subj.setStyle("-fx-text-fill:#f0c040;-fx-font-size:13px;-fx-font-weight:bold;");
                        Label target = new Label("Target: " + rs.getString("target_course") + "  |  " + rs.getString("created_at"));
                        target.setStyle("-fx-text-fill:#aaaaaa;-fx-font-size:11px;");
                        Label msg = new Label(rs.getString("message"));
                        msg.setWrapText(true);
                        msg.setStyle("-fx-text-fill:#cccccc;-fx-font-size:12px;");
                        item.getChildren().addAll(subj, target, msg);
                        listBox.getChildren().add(item);
                    }
                    if (!any) listBox.getChildren().add(noDataLabel("No announcements posted yet."));
                }
            } catch (Exception ex) {
                listBox.getChildren().add(noDataLabel("announcements table not found — run the migration SQL."));
            }
        };
        loadAnnouncements.run();

        postBtn.setOnAction(e -> {
            String subject = subjectField.getText().trim();
            String message = msgArea.getText().trim();
            if (subject.isEmpty() || message.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Subject and message cannot be empty."); return;
            }
            String targetCourse = cb.getSelectionModel().getSelectedItem();
            String sql = "INSERT INTO announcements (teacher_username, subject, message, target_course, created_at) VALUES (?, ?, ?, ?, NOW())";
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, teacherUsername);
                ps.setString(2, subject);
                ps.setString(3, message);
                ps.setString(4, targetCourse);
                ps.executeUpdate();
                subjectField.clear(); msgArea.clear();
                loadAnnouncements.run();
                showAlert(Alert.AlertType.INFORMATION, "Posted", "Announcement posted successfully.");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not post announcement.\nMake sure the announcements table exists.");
            }
        });

        createCard.getChildren().addAll(r1, msgArea, postBtn);
        announcementsContentBox.getChildren().addAll(createCard, listCard);
    }

    // ── ASSIGNMENTS SECTION ───────────────────────────────────────────────

    private void buildAssignmentsSection() {
        assignmentsContentBox.getChildren().clear();

        VBox createCard = card("Create New Assignment");

        ComboBox<String> cb = styledCombo(300);
        List<String[]> offerings = DataService.getInstance().getTeacherOfferings(teacherUsername);
        for (String[] o : offerings)
            cb.getItems().add(o[1] + "  " + o[2] + " (" + o[3] + " / Sec " + o[4] + ")");
        if (!cb.getItems().isEmpty()) cb.getSelectionModel().selectFirst();

        TextField titleF    = textField("Assignment Title", 280);
        DatePicker deadline = new DatePicker();
        deadline.setStyle("-fx-background-color:#3a3a3a;");
        TextField maxMarks  = textField("20", 80);
        TextArea desc       = new TextArea();
        desc.setPromptText("Instructions / description...");
        desc.setPrefHeight(80);
        desc.setStyle("-fx-background-color:#2a2a2a;-fx-border-color:#444;-fx-border-radius:4;-fx-background-radius:4;-fx-text-fill:white;-fx-control-inner-background:#2a2a2a;");

        HBox r1 = new HBox(12, lbl("Course:"), cb, lbl("Title:"), titleF);
        r1.setAlignment(Pos.CENTER_LEFT);
        HBox r2 = new HBox(12, lbl("Deadline:"), deadline, lbl("Max Marks:"), maxMarks);
        r2.setAlignment(Pos.CENTER_LEFT);

        Button createBtn = btn("➕ Create Assignment", "#cc0000");

        // list
        VBox listCard = card("Active Assignments");
        VBox listBox  = new VBox(8);
        listCard.getChildren().add(listBox);

        Runnable loadAssignments = () -> {
            listBox.getChildren().clear();
            String sql = """
                SELECT a.id, c.course_code, c.course_title, a.title, a.deadline, a.max_marks, a.description,
                       (SELECT COUNT(*) FROM assignment_submissions sub WHERE sub.assignment_id = a.id) AS submitted
                FROM assignments a
                JOIN course_offerings co ON a.offering_id = co.id
                JOIN courses c ON co.course_id = c.id
                WHERE co.teacher_username = ?
                ORDER BY a.deadline DESC
                LIMIT 30
                """;
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, teacherUsername);
                try (ResultSet rs = ps.executeQuery()) {
                    boolean any = false;
                    while (rs.next()) {
                        any = true;
                        HBox row = new HBox(12);
                        row.setPadding(new Insets(12, 16, 12, 16));
                        row.setAlignment(Pos.CENTER_LEFT);
                        row.setStyle("-fx-background-color:#252525;-fx-background-radius:6;");

                        VBox info = new VBox(3);
                        HBox.setHgrow(info, Priority.ALWAYS);
                        Label ttl = new Label(rs.getString("course_code") + " — " + rs.getString("title"));
                        ttl.setStyle("-fx-text-fill:#f0c040;-fx-font-size:13px;-fx-font-weight:bold;");
                        Label det = new Label("Deadline: " + rs.getString("deadline") +
                                "  |  Max: " + rs.getString("max_marks") +
                                "  |  Submitted: " + rs.getInt("submitted"));
                        det.setStyle("-fx-text-fill:#aaaaaa;-fx-font-size:12px;");
                        info.getChildren().addAll(ttl, det);
                        row.getChildren().addAll(info);
                        listBox.getChildren().add(row);
                    }
                    if (!any) listBox.getChildren().add(noDataLabel("No assignments created yet."));
                }
            } catch (Exception ex) {
                listBox.getChildren().add(noDataLabel("assignments table not found — run the migration SQL."));
            }
        };
        loadAssignments.run();

        createBtn.setOnAction(e -> {
            int idx = cb.getSelectionModel().getSelectedIndex();
            if (idx < 0 || idx >= offerings.size()) return;
            String offId = offerings.get(idx)[0];
            String title = titleF.getText().trim();
            if (title.isEmpty()) { showAlert(Alert.AlertType.WARNING, "Validation", "Title is required."); return; }
            String sql = "INSERT INTO assignments (offering_id, title, description, deadline, max_marks) VALUES (?, ?, ?, ?, ?)";
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, Integer.parseInt(offId));
                ps.setString(2, title);
                ps.setString(3, desc.getText().trim());
                ps.setDate(4, deadline.getValue() != null ? Date.valueOf(deadline.getValue()) : Date.valueOf(LocalDate.now()));
                ps.setDouble(5, parseD(maxMarks.getText()));
                ps.executeUpdate();
                titleF.clear(); desc.clear(); maxMarks.setText("20");
                loadAssignments.run();
                showAlert(Alert.AlertType.INFORMATION, "Created", "Assignment created successfully.");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not create assignment.\nMake sure assignments table exists.");
            }
        });

        createCard.getChildren().addAll(r1, r2, desc, createBtn);
        assignmentsContentBox.getChildren().addAll(createCard, listCard);
    }

    // ── HOME BUILDERS ─────────────────────────────────────────────────────

    private void buildTodaysClasses() {
        todaysClassesBox.getChildren().clear();
        int shown = 0;
        for (String[] c : myCourses) {
            if (shown >= 3) break;
            String line = c[0] + "  " + c[1] + " (" + c[3] + " / Sec " + c[4] + ")";
            todaysClassesBox.getChildren().add(classItem("Scheduled", line, "Section " + c[4]));
            shown++;
        }
        if (shown == 0) todaysClassesBox.getChildren().add(classItem("—", "No assigned classes found", "—"));
    }

    private HBox classItem(String time, String course, String room) {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color:#252525;-fx-background-radius:6;-fx-border-color:transparent transparent transparent #cc0000;-fx-border-width:0 0 0 3;");
        Label tL = new Label(time); tL.setStyle("-fx-text-fill:#f0c040;-fx-font-size:12px;-fx-min-width:140;");
        Label cL = new Label(course); cL.setStyle("-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:bold;");
        HBox sp = new HBox(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label rL = new Label(room); rL.setStyle("-fx-text-fill:#aaaaaa;-fx-font-size:12px;");
        box.getChildren().addAll(tL, cL, sp, rL);
        return box;
    }

    private void buildRecentActivity() {
        recentActivityBox.getChildren().clear();
        String[][] acts = {
                {"✔", "Logged in successfully.", "#66cc88"},
                {"📘", myCourses.size() + " course(s) loaded from database.", "#f0c040"},
                {"👥", countDistinctStudents() + " enrolled student(s) found.", "#aaaaaa"},
                {"📝", "Marks module ready — click Enter Marks to start.", "#aaaaaa"},
                {"📢", "Post announcements from the Announcements tab.", "#f0c040"}
        };
        for (String[] a : acts) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8, 12, 8, 12));
            row.setStyle("-fx-background-color:#252525;-fx-background-radius:4;");
            Label ico = new Label(a[0]); ico.setStyle("-fx-text-fill:" + a[2] + ";-fx-font-size:14px;-fx-min-width:22;");
            Label txt = new Label(a[1]); txt.setStyle("-fx-text-fill:#cccccc;-fx-font-size:13px;");
            row.getChildren().addAll(ico, txt);
            recentActivityBox.getChildren().add(row);
        }
    }

    private void buildProfileCourseList() {
        profileCourseListBox.getChildren().clear();
        for (String[] c : myCourses) {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(12));
            row.setStyle("-fx-background-color:#252525;-fx-background-radius:6;");
            Label no   = new Label(c[0]);  no.setStyle("-fx-text-fill:#f0c040;-fx-font-size:13px;-fx-font-weight:bold;-fx-min-width:80;");
            Label name = new Label(c[1]);  name.setStyle("-fx-text-fill:white;-fx-font-size:13px;"); HBox.setHgrow(name, Priority.ALWAYS);
            Label cr   = new Label(c[2] + " cr"); cr.setStyle("-fx-text-fill:#aaaaaa;-fx-font-size:12px;-fx-min-width:60;");
            Label lt   = new Label(c[3]);  lt.setStyle("-fx-text-fill:#bbbbbb;-fx-font-size:12px;-fx-min-width:60;");
            Label sec  = new Label("Sec " + c[4]); sec.setStyle("-fx-text-fill:#aaaaaa;-fx-font-size:12px;");
            row.getChildren().addAll(no, name, cr, lt, sec);
            profileCourseListBox.getChildren().add(row);
        }
    }

    // ── NAVIGATION ────────────────────────────────────────────────────────

    @FXML private void showHome()          { switchTo(homeSection); }
    @FXML private void showProfile()       { switchTo(profileSection); }
    @FXML private void showCourses()       { buildCoursesSection();      showAnchor(coursesSection); }
    @FXML private void showAttendance()    { buildAttendanceSection();   showAnchor(attendanceSection); }
    @FXML private void showMarks()         { buildMarksSection();        showAnchor(marksSection); }
    @FXML private void showAssignments()   { buildAssignmentsSection();  showAnchor(assignmentsSection); }
    @FXML private void showAnnouncements() { buildAnnouncementsSection(); showAnchor(announcementsSection); }
    @FXML private void showStudents()      { buildStudentsSection();     showAnchor(studentsSection); }

    private void switchTo(Region node) { hideAll(); node.setVisible(true); node.setManaged(true); }
    private void showAnchor(ScrollPane p) { hideAll(); p.setVisible(true); p.setManaged(true); }
    private void hideAll() {
        for (Region r : new Region[]{homeSection, profileSection, coursesSection, attendanceSection,
                marksSection, assignmentsSection, announcementsSection, studentsSection}) {
            r.setVisible(false); r.setManaged(false);
        }
    }

    @FXML private void handleLogout(ActionEvent e) {
        try { Stage st = (Stage) rootPane.getScene().getWindow();
              NavigationService.getInstance().openLogin(st); }
        catch (Exception ex) { ex.printStackTrace(); }
    }

    // ── HELPERS ───────────────────────────────────────────────────────────

    private int countDistinctStudents() {
        Set<String> ids = new HashSet<>();
        for (String[] s : myStudents) ids.add(s[0]);
        return ids.size();
    }

    private String calculateAverageAttendance() {
        if (myStudents.isEmpty()) return "0%";
        double total = 0; int count = 0;
        for (String[] s : myStudents) {
            try { total += Double.parseDouble(s[2].replace("%", "")); count++; } catch (Exception ignored) {}
        }
        return count == 0 ? "0%" : Math.round(total / count) + "%";
    }

    private String expandDept(String code) {
        Map<String, String> m = new HashMap<>();
        m.put("CSE",  "Department of Computer Science and Engineering");
        m.put("EEE",  "Department of Electrical and Electronic Engineering");
        m.put("CE",   "Department of Civil Engineering");
        m.put("ME",   "Department of Mechanical Engineering");
        m.put("ARCH", "Department of Architecture");
        return m.getOrDefault(code, "Department of " + code);
    }

    private String abbreviateDept(String full) {
        if (full.length() <= 20) return full;
        return full.replace("Department of ", "Dept. of ");
    }

    private String[] letterGrade(double pct) {
        if (pct >= 80) return new String[]{"A+", "4.00"};
        if (pct >= 75) return new String[]{"A",  "3.75"};
        if (pct >= 70) return new String[]{"A-", "3.50"};
        if (pct >= 65) return new String[]{"B+", "3.25"};
        if (pct >= 60) return new String[]{"B",  "3.00"};
        if (pct >= 55) return new String[]{"B-", "2.75"};
        if (pct >= 50) return new String[]{"C+", "2.50"};
        if (pct >= 45) return new String[]{"C",  "2.25"};
        if (pct >= 40) return new String[]{"D",  "2.00"};
        return new String[]{"F", "0.00"};
    }

    private String gradeColor(String g) {
        if (g == null) return "#aaaaaa";
        switch (g) {
            case "A+": case "A": case "A-": return "#66cc88";
            case "B+": case "B": case "B-": return "#f0c040";
            case "C+": case "C":            return "#ffaa44";
            case "D":                       return "#ff8844";
            case "F":                       return "#ff4444";
            default:                        return "#aaaaaa";
        }
    }

    private double parseD(String s) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return 0.0; }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    // ── UI FACTORY ────────────────────────────────────────────────────────

    private Label lbl(String t) {
        Label l = new Label(t); l.setStyle("-fx-text-fill:#aaaaaa;-fx-font-size:13px;"); return l;
    }
    private Label val(String t) {
        Label l = new Label(t); l.setStyle("-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:bold;-fx-min-width:50;-fx-alignment:CENTER;"); return l;
    }
    private Label miniLbl(String t) {
        Label l = new Label(t); l.setStyle("-fx-text-fill:#888;-fx-font-size:10px;-fx-min-width:30;"); return l;
    }
    private Label hdrLbl(String t, double w) {
        Label l = new Label(t); l.setStyle("-fx-text-fill:#f0c040;-fx-font-size:12px;-fx-font-weight:bold;");
        l.setMinWidth(w); return l;
    }
    private Label noDataLabel(String t) {
        Label l = new Label(t); l.setStyle("-fx-text-fill:#aaaaaa;-fx-font-size:13px;-fx-padding:10 0;"); return l;
    }

    private Button btn(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + color + ";-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:4;-fx-padding:8 16;-fx-cursor:hand;");
        return b;
    }

    private HBox chip(String label, String value) {
        HBox chip = new HBox(6); chip.setAlignment(Pos.CENTER_LEFT);
        chip.setPadding(new Insets(6, 12, 6, 12));
        chip.setStyle("-fx-background-color:#252525;-fx-background-radius:4;");
        Label l = new Label(label + ":"); l.setStyle("-fx-text-fill:#aaaaaa;-fx-font-size:12px;");
        Label v = new Label(value);       v.setStyle("-fx-text-fill:white;-fx-font-size:12px;-fx-font-weight:bold;");
        chip.getChildren().addAll(l, v); return chip;
    }

    private VBox card(String title) {
        VBox c = new VBox(12); c.setPadding(new Insets(18));
        c.setStyle("-fx-background-color:#2d2d2d;-fx-background-radius:8;-fx-border-color:#3a3a3a;-fx-border-radius:8;");
        Label t = new Label(title); t.setStyle("-fx-text-fill:#f0c040;-fx-font-size:15px;-fx-font-weight:bold;"); t.setWrapText(true);
        c.getChildren().add(t); return c;
    }

    private TextField textField(String prompt, double width) {
        TextField f = new TextField(); f.setPromptText(prompt);
        f.setStyle("-fx-background-color:#2a2a2a;-fx-border-color:#444;-fx-border-radius:4;-fx-background-radius:4;-fx-text-fill:white;-fx-pref-width:" + width + ";-fx-padding:8 12;");
        return f;
    }

    private TextField numField(String val, double width) {
        TextField f = new TextField(val.equals("0.00") ? "0" : val.replaceAll("\\.00$", ""));
        f.setStyle("-fx-background-color:#2a2a2a;-fx-border-color:#444;-fx-border-radius:4;-fx-background-radius:4;-fx-text-fill:white;-fx-pref-width:" + width + ";-fx-padding:4 8;-fx-alignment:CENTER;");
        return f;
    }

    private ComboBox<String> styledCombo(double width) {
        ComboBox<String> b = new ComboBox<>();
        b.setStyle("-fx-background-color:#3a3a3a;-fx-text-fill:white;-fx-pref-width:" + width + ";");
        return b;
    }

    private RadioButton radioBtn(String label, String color, ToggleGroup tg) {
        RadioButton rb = new RadioButton(label); rb.setToggleGroup(tg);
        rb.setStyle("-fx-text-fill:" + color + ";-fx-min-width:80;");
        return rb;
    }

    private TableView<String[]> makeTable(double height) {
        TableView<String[]> t = new TableView<>();
        t.setStyle("-fx-background-color:#1e1e1e;-fx-border-color:#444;-fx-border-radius:6;");
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.setPrefHeight(height); t.setFixedCellSize(38);
        return t;
    }

    private TableColumn<String[], String> strCol(String header, int idx, double minW) {
        TableColumn<String[], String> col = new TableColumn<>(header);
        col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[idx]));
        col.setSortable(false); if (minW > 0) col.setMinWidth(minW); return col;
    }
}
