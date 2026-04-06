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

import java.sql.*;
import java.util.*;

public class AdminDashboardController {

    @FXML private BorderPane rootPane;
    @FXML private Label sidebarNameLabel, headerAdminLabel;
    @FXML private VBox recentActivityBox, pendingApprovalsBox, quickActionsBox;
    @FXML private VBox studentsContentBox, teachersContentBox, rolesContentBox;
    @FXML private VBox departmentsContentBox, coursesContentBox, semestersContentBox;
    @FXML private VBox feesContentBox, reportsContentBox, settingsContentBox;

    @FXML private ScrollPane homeSection;
    @FXML private ScrollPane studentsSection, teachersSection, rolesSection;
    @FXML private ScrollPane departmentsSection, coursesSection, semestersSection;
    @FXML private ScrollPane feesSection, reportsSection, settingsSection;

    private String adminName = "System Admin";

    private final List<String[]> departmentList = new ArrayList<>();
    private final List<String[]> teacherList    = new ArrayList<>();
    private final List<String[]> studentList    = new ArrayList<>();
    private final List<String[]> courseList     = new ArrayList<>();

    public void setCurrentUser(User user) {
        this.adminName = user.getFullName();
        sidebarNameLabel.setText(adminName);
        headerAdminLabel.setText(adminName);

        loadDepartmentsFromDB();
        loadTeachersFromDB();
        loadStudentsFromDB();
        loadCoursesFromDB();

        buildQuickActions();
        buildRecentActivity();
        buildPendingApprovals();
    }

    // ── DB LOADERS ────────────────────────────────────────────────────────

    private void loadDepartmentsFromDB() {
        departmentList.clear();
        String sql = "SELECT department, COUNT(*) AS total_courses FROM courses GROUP BY department ORDER BY department";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String dept = rs.getString("department");
                departmentList.add(new String[]{dept, expandDeptName(dept), dept, "Not Assigned", rs.getString("total_courses")});
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadTeachersFromDB() {
        teacherList.clear();
        String sql = """
                SELECT t.username, u.full_name,
                       COALESCE(t.department, 'N/A') AS department,
                       COALESCE(t.designation, 'Teacher') AS designation,
                       'N/A' AS specialization, t.status
                FROM teachers t LEFT JOIN users u ON u.username = t.username
                ORDER BY t.username
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                teacherList.add(new String[]{
                        rs.getString("username"), rs.getString("full_name"),
                        rs.getString("department"), rs.getString("designation"),
                        rs.getString("specialization"), normalizeStatus(rs.getString("status"))
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadStudentsFromDB() {
        studentList.clear();
        String sql = """
                SELECT s.username,
                       COALESCE(s.name, u.full_name, s.username) AS full_name,
                       COALESCE(s.department, 'N/A') AS department,
                       CONCAT('L', COALESCE(s.semester_no, 1), 'T1') AS level_term,
                       s.status, '৳ 0' AS dues
                FROM students s LEFT JOIN users u ON u.username = s.username
                ORDER BY s.username
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                studentList.add(new String[]{
                        rs.getString("username"), rs.getString("full_name"),
                        rs.getString("department"), rs.getString("level_term"),
                        normalizeStatus(rs.getString("status")), rs.getString("dues")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadCoursesFromDB() {
        courseList.clear();
        String sql = """
                SELECT c.course_code, c.course_title, c.department, c.credit_hours,
                       CONCAT('L', c.level, 'T', c.term) AS level_term,
                       COALESCE(co.teacher_username, '—') AS assigned_to
                FROM courses c LEFT JOIN course_offerings co ON co.course_id = c.id
                ORDER BY c.department, c.level, c.term, c.course_code
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                courseList.add(new String[]{
                        rs.getString("course_code"), rs.getString("course_title"),
                        rs.getString("department"), String.format("%.2f", rs.getDouble("credit_hours")),
                        rs.getString("level_term"), rs.getString("assigned_to")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── HOME SECTION ──────────────────────────────────────────────────────

    private void buildQuickActions() {
        quickActionsBox.getChildren().clear();
        HBox row = new HBox(12); row.setAlignment(Pos.CENTER_LEFT);
        String[][] actions = {
                {"➕ Add Student", "#cc0000"}, {"➕ Add Teacher", "#2a7ac8"},
                {"📚 Add Course", "#2aa87a"}, {"🔗 Assign Course to Teacher", "#8a2ac8"},
                {"👤 Enroll Student", "#c8862a"}
        };
        for (String[] a : actions) {
            Button b = btn(a[0], a[1]);
            switch (a[0]) {
                case "➕ Add Student":            b.setOnAction(e -> openAddStudentDialog()); break;
                case "➕ Add Teacher":            b.setOnAction(e -> openAddTeacherDialog()); break;
                case "📚 Add Course":             b.setOnAction(e -> openAddCourseDialog()); break;
                case "🔗 Assign Course to Teacher": b.setOnAction(e -> openAssignCourseDialog()); break;
                case "👤 Enroll Student":         b.setOnAction(e -> openEnrollStudentDialog()); break;
            }
            row.getChildren().add(b);
        }
        quickActionsBox.getChildren().add(row);
    }

    private void buildRecentActivity() {
        recentActivityBox.getChildren().clear();
        String[][] acts = {
                {"✔", "Admin logged in successfully", "#66cc88"},
                {"👨‍🎓", studentList.size() + " students loaded from database", "#66cc88"},
                {"👩‍🏫", teacherList.size() + " teacher(s) loaded from database", "#66cc88"},
                {"📚", courseList.size() + " courses loaded from database", "#f0c040"},
                {"🏛", departmentList.size() + " departments found", "#aaaaaa"}
        };
        for (String[] a : acts) {
            HBox row = new HBox(10); row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8, 12, 8, 12));
            row.setStyle("-fx-background-color:#252525;-fx-background-radius:4;");
            Label ico = new Label(a[0]); ico.setStyle("-fx-text-fill:" + a[2] + ";-fx-font-size:14px;-fx-min-width:22;");
            Label txt = new Label(a[1]); txt.setStyle("-fx-text-fill:#cccccc;-fx-font-size:13px;");
            row.getChildren().addAll(ico, txt);
            recentActivityBox.getChildren().add(row);
        }
    }

    private void buildPendingApprovals() {
        pendingApprovalsBox.getChildren().clear();
        String[][] pend = {
                {"Section Change", "Demo placeholder"},
                {"Course Add/Drop", "Demo placeholder"},
                {"Fee Waiver", "Demo placeholder"},
                {"Attendance Condonation", "Demo placeholder"},
                {"Transcript Request", "Demo placeholder"}
        };
        for (String[] p : pend) {
            HBox row = new HBox(10); row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8, 12, 8, 12));
            row.setStyle("-fx-background-color:#252525;-fx-background-radius:4;");
            VBox info = new VBox(3); HBox.setHgrow(info, Priority.ALWAYS);
            Label type = new Label(p[0]); type.setStyle("-fx-text-fill:#f0c040;-fx-font-size:12px;-fx-font-weight:bold;");
            Label name = new Label(p[1]); name.setStyle("-fx-text-fill:#cccccc;-fx-font-size:12px;");
            info.getChildren().addAll(type, name);
            row.getChildren().addAll(info, smallBtn("Approve", "#2aa87a"), smallBtn("Deny", "#cc0000"));
            pendingApprovalsBox.getChildren().add(row);
        }
    }

    // ── ADD STUDENT DIALOG ────────────────────────────────────────────────

    private void openAddStudentDialog() {
        Dialog<ButtonType> dlg = styledDialog("Add New Student");
        GridPane grid = dialogGrid();

        TextField usernameF    = field("e.g. 2405001");
        TextField fullNameF    = field("Full Name");
        TextField departmentF  = field("e.g. CSE");
        TextField semesterF    = field("1");
        PasswordField passF    = new PasswordField(); styleField(passF, "Password (default = student ID)");

        addRow(grid, 0, "Student ID (username):", usernameF);
        addRow(grid, 1, "Full Name:", fullNameF);
        addRow(grid, 2, "Department:", departmentF);
        addRow(grid, 3, "Semester No:", semesterF);
        addRow(grid, 4, "Password:", passF);

        dlg.getDialogPane().setContent(grid);
        dlg.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                String uid  = usernameF.getText().trim();
                String name = fullNameF.getText().trim();
                String dept = departmentF.getText().trim();
                String sem  = semesterF.getText().trim().isEmpty() ? "1" : semesterF.getText().trim();
                String pass = passF.getText().trim().isEmpty() ? uid : passF.getText().trim();

                if (uid.isEmpty() || name.isEmpty()) { showAlert(Alert.AlertType.WARNING, "Validation", "Student ID and Name are required."); return; }

                try (Connection con = DBConnection.getConnection()) {
                    // insert into users
                    PreparedStatement ps1 = con.prepareStatement("INSERT INTO users (username, full_name, password, role) VALUES (?, ?, SHA2(?, 256), 'STUDENT') ON DUPLICATE KEY UPDATE full_name = VALUES(full_name)");
                    ps1.setString(1, uid); ps1.setString(2, name); ps1.setString(3, pass);
                    ps1.executeUpdate();

                    // insert into students (check existing schema)
                    PreparedStatement ps2 = con.prepareStatement(
                            "INSERT INTO students (username, name, department, semester_no, status) VALUES (?, ?, ?, ?, 'ACTIVE') ON DUPLICATE KEY UPDATE name = VALUES(name), department = VALUES(department)");
                    ps2.setString(1, uid); ps2.setString(2, name); ps2.setString(3, dept);
                    ps2.setInt(4, Integer.parseInt(sem));
                    ps2.executeUpdate();

                    loadStudentsFromDB(); buildRecentActivity();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Student " + uid + " added successfully.");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to add student: " + ex.getMessage());
                }
            }
        });
    }

    // ── ADD TEACHER DIALOG ────────────────────────────────────────────────

    private void openAddTeacherDialog() {
        Dialog<ButtonType> dlg = styledDialog("Add New Teacher");
        GridPane grid = dialogGrid();

        TextField usernameF   = field("e.g. t_rahman");
        TextField fullNameF   = field("Full Name");
        TextField departmentF = field("e.g. CSE");
        TextField desigF      = field("e.g. Assistant Professor");
        PasswordField passF   = new PasswordField(); styleField(passF, "Password");

        addRow(grid, 0, "Username:", usernameF);
        addRow(grid, 1, "Full Name:", fullNameF);
        addRow(grid, 2, "Department:", departmentF);
        addRow(grid, 3, "Designation:", desigF);
        addRow(grid, 4, "Password:", passF);
        dlg.getDialogPane().setContent(grid);

        dlg.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                String uid  = usernameF.getText().trim();
                String name = fullNameF.getText().trim();
                String dept = departmentF.getText().trim();
                String desig = desigF.getText().trim();
                String pass = passF.getText().trim().isEmpty() ? uid : passF.getText().trim();
                if (uid.isEmpty() || name.isEmpty()) { showAlert(Alert.AlertType.WARNING, "Validation", "Username and Name are required."); return; }

                try (Connection con = DBConnection.getConnection()) {
                    PreparedStatement ps1 = con.prepareStatement("INSERT INTO users (username, full_name, password, role) VALUES (?, ?, SHA2(?, 256), 'TEACHER') ON DUPLICATE KEY UPDATE full_name = VALUES(full_name)");
                    ps1.setString(1, uid); ps1.setString(2, name); ps1.setString(3, pass);
                    ps1.executeUpdate();

                    PreparedStatement ps2 = con.prepareStatement(
                            "INSERT INTO teachers (username, department, designation, status) VALUES (?, ?, ?, 'ACTIVE') ON DUPLICATE KEY UPDATE department = VALUES(department), designation = VALUES(designation)");
                    ps2.setString(1, uid); ps2.setString(2, dept); ps2.setString(3, desig);
                    ps2.executeUpdate();

                    loadTeachersFromDB(); buildRecentActivity();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Teacher " + uid + " added successfully.");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to add teacher: " + ex.getMessage());
                }
            }
        });
    }

    // ── ADD COURSE DIALOG ─────────────────────────────────────────────────

    private void openAddCourseDialog() {
        Dialog<ButtonType> dlg = styledDialog("Add New Course");
        GridPane grid = dialogGrid();

        TextField codeF    = field("e.g. CSE 101");
        TextField titleF   = field("Course Title");
        TextField deptF    = field("e.g. CSE");
        TextField creditF  = field("3.00");
        TextField levelF   = field("1");
        TextField termF    = field("1");

        addRow(grid, 0, "Course Code:", codeF);
        addRow(grid, 1, "Course Title:", titleF);
        addRow(grid, 2, "Department:", deptF);
        addRow(grid, 3, "Credit Hours:", creditF);
        addRow(grid, 4, "Level:", levelF);
        addRow(grid, 5, "Term:", termF);
        dlg.getDialogPane().setContent(grid);

        dlg.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                String code  = codeF.getText().trim();
                String title = titleF.getText().trim();
                String dept  = deptF.getText().trim();
                if (code.isEmpty() || title.isEmpty()) { showAlert(Alert.AlertType.WARNING, "Validation", "Course code and title are required."); return; }
                try (Connection con = DBConnection.getConnection();
                     PreparedStatement ps = con.prepareStatement(
                             "INSERT INTO courses (course_code, course_title, department, credit_hours, level, term) VALUES (?, ?, ?, ?, ?, ?)")) {
                    ps.setString(1, code); ps.setString(2, title); ps.setString(3, dept);
                    ps.setDouble(4, Double.parseDouble(creditF.getText().trim().isEmpty() ? "3.0" : creditF.getText().trim()));
                    ps.setInt(5, Integer.parseInt(levelF.getText().trim().isEmpty() ? "1" : levelF.getText().trim()));
                    ps.setInt(6, Integer.parseInt(termF.getText().trim().isEmpty() ? "1" : termF.getText().trim()));
                    ps.executeUpdate();
                    loadCoursesFromDB(); buildRecentActivity();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Course '" + code + "' added.");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to add course: " + ex.getMessage());
                }
            }
        });
    }

    // ── ASSIGN COURSE TO TEACHER ─────────────────────────────────────────

    private void openAssignCourseDialog() {
        Dialog<ButtonType> dlg = styledDialog("Assign Course to Teacher");
        GridPane grid = dialogGrid();

        // course picker
        ComboBox<String> courseBox = new ComboBox<>();
        courseBox.setStyle("-fx-background-color:#3a3a3a;-fx-text-fill:white;-fx-pref-width:300;");
        List<String[]> courses = new ArrayList<>();
        String sql1 = "SELECT id, course_code, course_title, department, level, term FROM courses ORDER BY course_code";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql1); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                courses.add(new String[]{rs.getString("id"), rs.getString("course_code"), rs.getString("course_title"),
                        rs.getString("department"), rs.getString("level"), rs.getString("term")});
                courseBox.getItems().add(rs.getString("course_code") + " — " + rs.getString("course_title") +
                        " (L" + rs.getString("level") + "T" + rs.getString("term") + ")");
            }
        } catch (Exception ex) { ex.printStackTrace(); }

        ComboBox<String> teacherBox = new ComboBox<>();
        teacherBox.setStyle("-fx-background-color:#3a3a3a;-fx-text-fill:white;-fx-pref-width:300;");
        List<String> teacherUsernames = new ArrayList<>();
        for (String[] t : teacherList) {
            teacherBox.getItems().add(t[0] + " — " + t[1]);
            teacherUsernames.add(t[0]);
        }

        TextField sectionF = field("A");
        TextField levelF   = field("");
        TextField termF    = field("");

        courseBox.setOnAction(e -> {
            int idx = courseBox.getSelectionModel().getSelectedIndex();
            if (idx >= 0 && idx < courses.size()) {
                levelF.setText(courses.get(idx)[4]);
                termF.setText(courses.get(idx)[5]);
            }
        });
        if (!courseBox.getItems().isEmpty()) courseBox.getSelectionModel().selectFirst();

        addRow(grid, 0, "Course:", courseBox);
        addRow(grid, 1, "Teacher:", teacherBox);
        addRow(grid, 2, "Section:", sectionF);
        addRow(grid, 3, "Level:", levelF);
        addRow(grid, 4, "Term:", termF);
        dlg.getDialogPane().setContent(grid);

        dlg.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                int cIdx = courseBox.getSelectionModel().getSelectedIndex();
                int tIdx = teacherBox.getSelectionModel().getSelectedIndex();
                if (cIdx < 0 || tIdx < 0) { showAlert(Alert.AlertType.WARNING, "Validation", "Select a course and a teacher."); return; }
                String courseId   = courses.get(cIdx)[0];
                String tUser      = teacherUsernames.get(tIdx);
                String department = courses.get(cIdx)[3];
                String section    = sectionF.getText().trim().isEmpty() ? "A" : sectionF.getText().trim();
                int level = Integer.parseInt(levelF.getText().trim().isEmpty() ? "1" : levelF.getText().trim());
                int term  = Integer.parseInt(termF.getText().trim().isEmpty() ? "1" : termF.getText().trim());

                try (Connection con = DBConnection.getConnection();
                     PreparedStatement ps = con.prepareStatement(
                             "INSERT INTO course_offerings (course_id, teacher_username, section_name, department, level, term, status) " +
                             "VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE') ON DUPLICATE KEY UPDATE teacher_username = VALUES(teacher_username)")) {
                    ps.setInt(1, Integer.parseInt(courseId));
                    ps.setString(2, tUser); ps.setString(3, section);
                    ps.setString(4, department);
                    ps.setInt(5, level); ps.setInt(6, term);
                    ps.executeUpdate();
                    loadCoursesFromDB();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Course assigned to " + tUser + " successfully.");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to assign: " + ex.getMessage());
                }
            }
        });
    }

    // ── ENROLL STUDENT ────────────────────────────────────────────────────

    private void openEnrollStudentDialog() {
        Dialog<ButtonType> dlg = styledDialog("Enroll Student in Course");
        GridPane grid = dialogGrid();

        TextField studentF = field("Student ID / username");

        ComboBox<String> offeringBox = new ComboBox<>();
        offeringBox.setStyle("-fx-background-color:#3a3a3a;-fx-text-fill:white;-fx-pref-width:300;");
        List<String> offeringIds = new ArrayList<>();
        String sql2 = """
                SELECT co.id, c.course_code, c.course_title, co.section_name, co.level, co.term, co.teacher_username
                FROM course_offerings co JOIN courses c ON co.course_id = c.id
                WHERE co.status = 'ACTIVE'
                ORDER BY c.course_code, co.section_name
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql2); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                offeringIds.add(rs.getString("id"));
                offeringBox.getItems().add(rs.getString("course_code") + " " + rs.getString("course_title") +
                        " — Sec " + rs.getString("section_name") +
                        " (L" + rs.getString("level") + "T" + rs.getString("term") + ") [" + rs.getString("teacher_username") + "]");
            }
        } catch (Exception ex) { ex.printStackTrace(); }

        addRow(grid, 0, "Student ID:", studentF);
        addRow(grid, 1, "Course Offering:", offeringBox);
        dlg.getDialogPane().setContent(grid);

        dlg.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                String sid = studentF.getText().trim();
                int oIdx = offeringBox.getSelectionModel().getSelectedIndex();
                if (sid.isEmpty() || oIdx < 0) { showAlert(Alert.AlertType.WARNING, "Validation", "Enter student ID and select a course."); return; }
                String oid = offeringIds.get(oIdx);
                try (Connection con = DBConnection.getConnection();
                     PreparedStatement ps = con.prepareStatement(
                             "INSERT INTO enrollments (student_username, offering_id, status) VALUES (?, ?, 'ENROLLED') ON DUPLICATE KEY UPDATE status = 'ENROLLED'")) {
                    ps.setString(1, sid); ps.setInt(2, Integer.parseInt(oid));
                    ps.executeUpdate();
                    showAlert(Alert.AlertType.INFORMATION, "Enrolled", "Student " + sid + " enrolled successfully.");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Enrollment failed: " + ex.getMessage());
                }
            }
        });
    }

    // ── MANAGE STUDENTS ───────────────────────────────────────────────────

    private void buildStudentsSection() {
        studentsContentBox.getChildren().clear();

        VBox topBar = card("Student Records");
        TextField search = textField("Search by ID, name or department...", 320);
        ComboBox<String> deptFilter = new ComboBox<>();
        deptFilter.setStyle("-fx-background-color:#3a3a3a;-fx-text-fill:white;-fx-pref-width:180;");
        deptFilter.getItems().add("All Departments");
        for (String[] d : departmentList) deptFilter.getItems().add(d[2]);
        deptFilter.getSelectionModel().selectFirst();
        Button addBtn = btn("+ Add Student", "#cc0000");
        addBtn.setOnAction(e -> openAddStudentDialog());

        HBox searchRow = new HBox(12, search, deptFilter, addBtn);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        topBar.getChildren().add(searchRow);
        studentsContentBox.getChildren().add(topBar);

        TableView<String[]> tbl = makeTable(380);
        TableColumn<String[], String> colStatus = strCol("Status", 4, 90);
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle("-fx-text-fill:" + (item.equals("Active") ? "#66cc88" : "#ff6666") + ";-fx-font-weight:bold;-fx-alignment:CENTER;");
            }
        });
        tbl.getColumns().addAll(strCol("Student ID", 0, 110), strCol("Name", 1, 0),
                strCol("Dept", 2, 70), strCol("Level/Term", 3, 100), colStatus, strCol("Dues", 5, 100));

        ObservableList<String[]> allStudents = FXCollections.observableArrayList(studentList);
        tbl.setItems(allStudents);

        // filter
        Runnable applyFilter = () -> {
            String q    = search.getText().toLowerCase().trim();
            String dept = deptFilter.getSelectionModel().getSelectedItem();
            tbl.setItems(FXCollections.observableArrayList(
                    studentList.stream().filter(s ->
                            (q.isEmpty() || s[0].toLowerCase().contains(q) || s[1].toLowerCase().contains(q)) &&
                            (dept == null || dept.equals("All Departments") || s[2].equalsIgnoreCase(dept))
                    ).collect(java.util.stream.Collectors.toList())
            ));
        };
        search.textProperty().addListener((obs, o, n) -> applyFilter.run());
        deptFilter.setOnAction(e -> applyFilter.run());

        HBox actRow = new HBox(10);
        actRow.getChildren().addAll(
                btn("Edit Selected", "#2a7ac8"),
                btn("Deactivate", "#c8862a"),
                btn("Reset Password", "#3a3a3a")
        );

        // Edit selected
        ((Button) actRow.getChildren().get(0)).setOnAction(e -> {
            String[] sel = tbl.getSelectionModel().getSelectedItem();
            if (sel == null) { showAlert(Alert.AlertType.WARNING, "Selection", "Please select a student."); return; }
            openEditStudentDialog(sel[0], sel[1]);
        });

        // Deactivate
        ((Button) actRow.getChildren().get(1)).setOnAction(e -> {
            String[] sel = tbl.getSelectionModel().getSelectedItem();
            if (sel == null) { showAlert(Alert.AlertType.WARNING, "Selection", "Please select a student."); return; }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Deactivate student " + sel[0] + "?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(b -> {
                if (b == ButtonType.YES) {
                    try (Connection con = DBConnection.getConnection();
                         PreparedStatement ps = con.prepareStatement("UPDATE students SET status = 'INACTIVE' WHERE username = ?")) {
                        ps.setString(1, sel[0]); ps.executeUpdate();
                        loadStudentsFromDB(); buildStudentsSection();
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            });
        });

        VBox tableCard = new VBox(12);
        tableCard.setPadding(new Insets(18));
        tableCard.setStyle("-fx-background-color:#2d2d2d;-fx-background-radius:8;-fx-border-color:#3a3a3a;-fx-border-radius:8;");
        tableCard.getChildren().addAll(tbl, actRow);
        studentsContentBox.getChildren().add(tableCard);
    }

    private void openEditStudentDialog(String uid, String currentName) {
        Dialog<ButtonType> dlg = styledDialog("Edit Student — " + uid);
        GridPane grid = dialogGrid();
        TextField nameF = field(currentName);
        TextField deptF = field("");
        TextField semF  = field("");
        addRow(grid, 0, "Full Name:", nameF);
        addRow(grid, 1, "Department:", deptF);
        addRow(grid, 2, "Semester No:", semF);
        dlg.getDialogPane().setContent(grid);
        dlg.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try (Connection con = DBConnection.getConnection()) {
                    if (!nameF.getText().trim().isEmpty()) {
                        PreparedStatement ps = con.prepareStatement("UPDATE users SET full_name = ? WHERE username = ?");
                        ps.setString(1, nameF.getText().trim()); ps.setString(2, uid); ps.executeUpdate();
                    }
                    if (!deptF.getText().trim().isEmpty()) {
                        PreparedStatement ps2 = con.prepareStatement("UPDATE students SET department = ? WHERE username = ?");
                        ps2.setString(1, deptF.getText().trim()); ps2.setString(2, uid); ps2.executeUpdate();
                    }
                    loadStudentsFromDB(); buildStudentsSection();
                    showAlert(Alert.AlertType.INFORMATION, "Updated", "Student updated.");
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });
    }

    // ── MANAGE TEACHERS ───────────────────────────────────────────────────

    private void buildTeachersSection() {
        teachersContentBox.getChildren().clear();

        VBox topBar = card("Faculty Records");
        TextField search = textField("Search by ID, name or department...", 320);
        Button addBtn = btn("+ Add Teacher", "#cc0000");
        addBtn.setOnAction(e -> openAddTeacherDialog());
        HBox sr = new HBox(12, search, addBtn);
        sr.setAlignment(Pos.CENTER_LEFT);
        topBar.getChildren().add(sr);
        teachersContentBox.getChildren().add(topBar);

        TableView<String[]> tbl = makeTable(320);
        TableColumn<String[], String> colStatus = strCol("Status", 5, 90);
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle("-fx-text-fill:" + (item.equals("Active") ? "#66cc88" : "#ffaa44") + ";-fx-font-weight:bold;-fx-alignment:CENTER;");
            }
        });
        tbl.getColumns().addAll(strCol("ID", 0, 100), strCol("Name", 1, 0),
                strCol("Dept", 2, 70), strCol("Designation", 3, 160), colStatus);

        ObservableList<String[]> all = FXCollections.observableArrayList(teacherList);
        tbl.setItems(all);
        search.textProperty().addListener((obs, o, n) -> {
            String q = n.toLowerCase().trim();
            tbl.setItems(FXCollections.observableArrayList(
                    teacherList.stream().filter(t -> q.isEmpty() ||
                            t[0].toLowerCase().contains(q) || t[1].toLowerCase().contains(q) || t[2].toLowerCase().contains(q)
                    ).collect(java.util.stream.Collectors.toList())
            ));
        });

        Button assignBtn = btn("Assign Courses", "#2aa87a");
        assignBtn.setOnAction(e -> openAssignCourseDialog());

        HBox actRow = new HBox(10);
        actRow.getChildren().addAll(btn("Edit Selected", "#2a7ac8"), assignBtn, btn("Reset Password", "#3a3a3a"));

        ((Button) actRow.getChildren().get(0)).setOnAction(e -> {
            String[] sel = tbl.getSelectionModel().getSelectedItem();
            if (sel == null) { showAlert(Alert.AlertType.WARNING, "Selection", "Select a teacher first."); return; }
            openEditTeacherDialog(sel[0], sel[1], sel[2], sel[3]);
        });

        VBox tableCard = new VBox(12);
        tableCard.setPadding(new Insets(18));
        tableCard.setStyle("-fx-background-color:#2d2d2d;-fx-background-radius:8;-fx-border-color:#3a3a3a;-fx-border-radius:8;");
        tableCard.getChildren().addAll(tbl, actRow);
        teachersContentBox.getChildren().add(tableCard);
    }

    private void openEditTeacherDialog(String uid, String name, String dept, String desig) {
        Dialog<ButtonType> dlg = styledDialog("Edit Teacher — " + uid);
        GridPane grid = dialogGrid();
        TextField nameF  = field(name); TextField deptF  = field(dept); TextField desigF = field(desig);
        addRow(grid, 0, "Full Name:", nameF); addRow(grid, 1, "Department:", deptF); addRow(grid, 2, "Designation:", desigF);
        dlg.getDialogPane().setContent(grid);
        dlg.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try (Connection con = DBConnection.getConnection()) {
                    PreparedStatement ps1 = con.prepareStatement("UPDATE users SET full_name = ? WHERE username = ?");
                    ps1.setString(1, nameF.getText().trim()); ps1.setString(2, uid); ps1.executeUpdate();
                    PreparedStatement ps2 = con.prepareStatement("UPDATE teachers SET department = ?, designation = ? WHERE username = ?");
                    ps2.setString(1, deptF.getText().trim()); ps2.setString(2, desigF.getText().trim()); ps2.setString(3, uid); ps2.executeUpdate();
                    loadTeachersFromDB(); buildTeachersSection();
                    showAlert(Alert.AlertType.INFORMATION, "Updated", "Teacher updated.");
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });
    }

    // ── COURSES SECTION ───────────────────────────────────────────────────

    private void buildCoursesSection() {
        coursesContentBox.getChildren().clear();

        VBox topBar = card("Course Registry");
        ComboBox<String> deptBox = new ComboBox<>();
        deptBox.setStyle("-fx-background-color:#3a3a3a;-fx-text-fill:white;-fx-pref-width:260;");
        deptBox.getItems().add("All Departments");
        for (String[] d : departmentList) deptBox.getItems().add(d[0]);
        deptBox.getSelectionModel().selectFirst();
        Button addCrsBtn = btn("+ Add Course", "#cc0000");
        addCrsBtn.setOnAction(e -> openAddCourseDialog());
        Button assignBtn = btn("🔗 Assign to Teacher", "#8a2ac8");
        assignBtn.setOnAction(e -> openAssignCourseDialog());
        Button enrollBtn = btn("👤 Enroll Student", "#c8862a");
        enrollBtn.setOnAction(e -> openEnrollStudentDialog());

        HBox sr = new HBox(12, lbl("Filter:"), deptBox, addCrsBtn, assignBtn, enrollBtn);
        sr.setAlignment(Pos.CENTER_LEFT);
        topBar.getChildren().add(sr);
        coursesContentBox.getChildren().add(topBar);

        TableView<String[]> tbl = makeTable(320);
        tbl.getColumns().addAll(
                strCol("Course No", 0, 110), strCol("Title", 1, 0),
                strCol("Dept", 2, 70), strCol("Credits", 3, 80),
                strCol("Term", 4, 80), strCol("Assigned To", 5, 160));

        ObservableList<String[]> all = FXCollections.observableArrayList(courseList);
        tbl.setItems(all);
        deptBox.setOnAction(e -> {
            String sel = deptBox.getSelectionModel().getSelectedItem();
            tbl.setItems(FXCollections.observableArrayList(
                    courseList.stream().filter(c -> sel == null || sel.equals("All Departments") || c[2].equalsIgnoreCase(sel))
                    .collect(java.util.stream.Collectors.toList())
            ));
        });

        coursesContentBox.getChildren().add(tbl);
    }

    // ── DEPARTMENTS ───────────────────────────────────────────────────────

    private void buildDepartmentsSection() {
        departmentsContentBox.getChildren().clear();
        VBox topBar = card("University Departments");
        departmentsContentBox.getChildren().add(topBar);
        TableView<String[]> tbl = makeTable(420);
        tbl.getColumns().addAll(strCol("Code", 0, 70), strCol("Department Name", 1, 0),
                strCol("Abbrev.", 2, 90), strCol("Head", 3, 160), strCol("Courses", 4, 90));
        tbl.setItems(FXCollections.observableArrayList(departmentList));
        departmentsContentBox.getChildren().add(tbl);
    }

    // ── ROLES ─────────────────────────────────────────────────────────────

    private void buildRolesSection() {
        rolesContentBox.getChildren().clear();
        String[][] roles = {
                {"Super Admin", "Full system access — all modules", "#cc0000", "1"},
                {"Academic Admin", "Courses, semesters, timetables", "#2a7ac8", "3"},
                {"Teacher", "Course-level academic operations", "#8a2ac8", String.valueOf(teacherList.size())},
                {"Student", "View personal academic data", "#555555", String.valueOf(studentList.size())}
        };
        for (String[] r : roles) {
            HBox row = new HBox(15); row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(16));
            row.setStyle("-fx-background-color:#2d2d2d;-fx-background-radius:8;-fx-border-color:transparent transparent transparent " + r[2] + ";-fx-border-width:0 0 0 4;");
            Label name  = new Label(r[0]); name.setStyle("-fx-text-fill:" + r[2] + ";-fx-font-size:14px;-fx-font-weight:bold;-fx-min-width:160;");
            Label desc  = new Label(r[1]); desc.setStyle("-fx-text-fill:#cccccc;-fx-font-size:13px;"); HBox.setHgrow(desc, Priority.ALWAYS);
            Label count = new Label(r[3] + " users"); count.setStyle("-fx-text-fill:#aaaaaa;-fx-font-size:12px;-fx-min-width:80;");
            row.getChildren().addAll(name, desc, count, smallBtn("Edit", "#3a3a3a"));
            rolesContentBox.getChildren().add(row);
        }
    }

    // ── SEMESTERS ─────────────────────────────────────────────────────────

    private void buildSemestersSection() {
        semestersContentBox.getChildren().clear();
        VBox card = card("Academic Sessions");
        // Attempt to load from DB
        String sql = "SELECT * FROM semesters ORDER BY start_date DESC LIMIT 10";
        List<String[]> rows = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new String[]{rs.getString("session_name"), rs.getString("level_term"),
                        rs.getString("start_date"), rs.getString("end_date"), rs.getString("status"), "—"});
            }
        } catch (Exception e) {
            // fallback to static data
            rows = Arrays.asList(
                    new String[]{"2024-25", "Level 1, Term 2", "Jan 2026", "Apr 2026", "Active", "✔"},
                    new String[]{"2024-25", "Level 2, Term 1", "Jan 2026", "Apr 2026", "Active", "✔"},
                    new String[]{"2024-25", "Level 3, Term 2", "Jan 2026", "Apr 2026", "Active", "✔"},
                    new String[]{"2024-25", "Level 4, Term 2", "Jan 2026", "Apr 2026", "Active", "✔"}
            );
        }
        TableView<String[]> tbl = makeTable(260);
        TableColumn<String[], String> colStat = strCol("Status", 4, 100);
        colStat.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle("-fx-text-fill:" + (item.equals("Active") ? "#66cc88" : "#aaaaaa") + ";-fx-font-weight:bold;-fx-alignment:CENTER;");
            }
        });
        tbl.getColumns().addAll(strCol("Session", 0, 90), strCol("Level/Term", 1, 140),
                strCol("Start", 2, 100), strCol("End", 3, 100), colStat);
        tbl.setItems(FXCollections.observableArrayList(rows));

        HBox row = new HBox(10);
        row.getChildren().addAll(btn("+ New Semester", "#cc0000"), btn("Publish Timetable", "#2a7ac8"), btn("Lock Results", "#c8862a"));
        card.getChildren().addAll(tbl, row);
        semestersContentBox.getChildren().add(card);
    }

    // ── FEES ──────────────────────────────────────────────────────────────

    private void buildFeesSection() {
        feesContentBox.getChildren().clear();
        HBox summary = new HBox(15);
        summary.getChildren().addAll(
                feeCard("Total Students", String.valueOf(studentList.size()), "#66cc88"),
                feeCard("Teachers", String.valueOf(teacherList.size()), "#2a7ac8"),
                feeCard("Courses", String.valueOf(courseList.size()), "#f0c040"),
                feeCard("Departments", String.valueOf(departmentList.size()), "#ffaa44"));
        feesContentBox.getChildren().add(summary);
        VBox placeholder = card("Student Dues Overview");
        placeholder.getChildren().add(lbl("Fee module — connect to a fees/payments table to enable."));
        feesContentBox.getChildren().add(placeholder);
    }

    // ── REPORTS ────────────────────────────────────────────────────────────

    private void buildReportsSection() {
        reportsContentBox.getChildren().clear();
        String[][] reportTypes = {
                {"📊", "Enrollment Report", "Total students per department, level, and semester", "#2a7ac8"},
                {"📋", "Attendance Summary", "Class-wise and student-wise attendance statistics", "#2aa87a"},
                {"🎓", "Academic Performance", "Pass/fail rates, CGPA distributions by department", "#cc0000"},
                {"👩‍🏫", "Teacher Workload", "Course load and student count per faculty", "#8a2ac8"}
        };
        for (String[] r : reportTypes) {
            HBox row = new HBox(16); row.setAlignment(Pos.CENTER_LEFT); row.setPadding(new Insets(16));
            row.setStyle("-fx-background-color:#2d2d2d;-fx-background-radius:8;-fx-border-color:transparent transparent transparent " + r[3] + ";-fx-border-width:0 0 0 4;");
            Label ico = new Label(r[0]); ico.setStyle("-fx-text-fill:" + r[3] + ";-fx-font-size:22px;-fx-min-width:34;");
            VBox info = new VBox(4); HBox.setHgrow(info, Priority.ALWAYS);
            Label nm = new Label(r[1]); nm.setStyle("-fx-text-fill:white;-fx-font-size:14px;-fx-font-weight:bold;");
            Label desc = new Label(r[2]); desc.setStyle("-fx-text-fill:#aaaaaa;-fx-font-size:12px;");
            info.getChildren().addAll(nm, desc);
            row.getChildren().addAll(ico, info, btn("Generate", r[3]));
            reportsContentBox.getChildren().add(row);
        }
    }

    // ── SETTINGS ───────────────────────────────────────────────────────────

    private void buildSettingsSection() {
        settingsContentBox.getChildren().clear();
        String[][] settings = {
                {"🔔", "Notification Settings", "Configure email/SMS notifications"},
                {"🔐", "Security Policy", "Password requirements, session timeout"},
                {"🗄", "Database Backup", "Schedule automatic backups"},
                {"🏛", "University Info", "Edit BUET name, logo and contact info"}
        };
        for (String[] s : settings) {
            HBox row = new HBox(16); row.setAlignment(Pos.CENTER_LEFT); row.setPadding(new Insets(16));
            row.setStyle("-fx-background-color:#2d2d2d;-fx-background-radius:8;");
            Label ico = new Label(s[0]); ico.setStyle("-fx-text-fill:#f0c040;-fx-font-size:22px;-fx-min-width:34;");
            VBox info = new VBox(4); HBox.setHgrow(info, Priority.ALWAYS);
            Label nm = new Label(s[1]); nm.setStyle("-fx-text-fill:white;-fx-font-size:14px;-fx-font-weight:bold;");
            Label desc = new Label(s[2]); desc.setStyle("-fx-text-fill:#aaaaaa;-fx-font-size:12px;");
            info.getChildren().addAll(nm, desc);
            row.getChildren().addAll(ico, info, btn("Configure", "#3a3a3a"));
            settingsContentBox.getChildren().add(row);
        }
    }

    // ── NAVIGATION ────────────────────────────────────────────────────────

    @FXML private void showHome()        { hideAll(); homeSection.setVisible(true); homeSection.setManaged(true); }
    @FXML private void showStudents()    { buildStudentsSection();    showAnchor(studentsSection); }
    @FXML private void showTeachers()    { buildTeachersSection();    showAnchor(teachersSection); }
    @FXML private void showRoles()       { buildRolesSection();       showAnchor(rolesSection); }
    @FXML private void showDepartments() { buildDepartmentsSection(); showAnchor(departmentsSection); }
    @FXML private void showCourses()     { buildCoursesSection();     showAnchor(coursesSection); }
    @FXML private void showSemesters()   { buildSemestersSection();   showAnchor(semestersSection); }
    @FXML private void showFees()        { buildFeesSection();        showAnchor(feesSection); }
    @FXML private void showReports()     { buildReportsSection();     showAnchor(reportsSection); }
    @FXML private void showSettings()    { buildSettingsSection();    showAnchor(settingsSection); }

    private void showAnchor(ScrollPane pane) { hideAll(); pane.setVisible(true); pane.setManaged(true); }
    private void hideAll() {
        for (Region r : new Region[]{homeSection, studentsSection, teachersSection, rolesSection,
                departmentsSection, coursesSection, semestersSection, feesSection, reportsSection, settingsSection}) {
            r.setVisible(false); r.setManaged(false);
        }
    }

    @FXML private void handleLogout(ActionEvent e) {
        try { Stage stage = (Stage) rootPane.getScene().getWindow();
              NavigationService.getInstance().openLogin(stage); }
        catch (Exception ex) { ex.printStackTrace(); }
    }

    // ── HELPERS ───────────────────────────────────────────────────────────

    private String normalizeStatus(String s) {
        if (s == null || s.isBlank()) return "Active";
        String t = s.trim().toLowerCase();
        return Character.toUpperCase(t.charAt(0)) + t.substring(1);
    }

    private String expandDeptName(String code) {
        switch (code) {
            case "CSE": return "Computer Science & Engineering";
            case "EEE": return "Electrical & Electronic Engineering";
            case "CE":  return "Civil Engineering";
            case "ARCH":return "Architecture";
            case "BME": return "Biomedical Engineering";
            case "ChE": return "Chemical Engineering";
            case "IPE": return "Industrial & Production Engineering";
            case "ME":  return "Mechanical Engineering";
            case "MME": return "Materials & Metallurgical Engineering";
            case "NAME":return "Naval Architecture & Marine Engineering";
            case "URP": return "Urban & Regional Planning";
            case "WRE": return "Water Resources Engineering";
            case "NCE": return "Nanomaterials & Ceramic Engineering";
            default:    return code;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    // ── DIALOG HELPERS ────────────────────────────────────────────────────

    private Dialog<ButtonType> styledDialog(String title) {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle(title);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dlg.getDialogPane().setStyle("-fx-background-color:#2d2d2d;");
        return dlg;
    }

    private GridPane dialogGrid() {
        GridPane g = new GridPane(); g.setHgap(12); g.setVgap(10); g.setPadding(new Insets(20));
        return g;
    }

    private TextField field(String prompt) {
        TextField f = new TextField(prompt.startsWith("e.g") ? "" : prompt);
        f.setPromptText(prompt.startsWith("e.g") ? prompt : "");
        f.setStyle("-fx-background-color:#2a2a2a;-fx-border-color:#444;-fx-border-radius:4;-fx-background-radius:4;-fx-text-fill:white;-fx-pref-width:280;-fx-padding:8 12;");
        return f;
    }

    private void styleField(TextField f, String prompt) {
        f.setPromptText(prompt);
        f.setStyle("-fx-background-color:#2a2a2a;-fx-border-color:#444;-fx-border-radius:4;-fx-background-radius:4;-fx-text-fill:white;-fx-pref-width:280;-fx-padding:8 12;");
    }

    private void addRow(GridPane g, int row, String label, javafx.scene.Node field) {
        Label l = new Label(label); l.setStyle("-fx-text-fill:#cccccc;-fx-font-size:13px;");
        g.add(l, 0, row); g.add(field, 1, row);
    }

    // ── UI FACTORY ────────────────────────────────────────────────────────

    private Label lbl(String t) { Label l = new Label(t); l.setStyle("-fx-text-fill:#aaaaaa;-fx-font-size:13px;"); return l; }

    private Button btn(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + color + ";-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:4;-fx-padding:8 16;-fx-cursor:hand;");
        return b;
    }

    private Button smallBtn(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + color + ";-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:4;-fx-padding:6 12;-fx-cursor:hand;-fx-font-size:12px;");
        return b;
    }

    private VBox feeCard(String label, String value, String color) {
        VBox c = new VBox(6); c.setAlignment(Pos.CENTER_LEFT); c.setPadding(new Insets(16));
        HBox.setHgrow(c, Priority.ALWAYS);
        c.setStyle("-fx-background-color:#2d2d2d;-fx-background-radius:8;-fx-border-color:" + color + ";-fx-border-width:0 0 3 0;-fx-border-radius:8;");
        Label l = new Label(label); l.setStyle("-fx-text-fill:#aaaaaa;-fx-font-size:12px;");
        Label v = new Label(value); v.setStyle("-fx-text-fill:" + color + ";-fx-font-size:18px;-fx-font-weight:bold;");
        c.getChildren().addAll(l, v); return c;
    }

    private VBox card(String title) {
        VBox c = new VBox(12); c.setPadding(new Insets(18));
        c.setStyle("-fx-background-color:#2d2d2d;-fx-background-radius:8;-fx-border-color:#3a3a3a;-fx-border-radius:8;");
        Label t = new Label(title); t.setStyle("-fx-text-fill:#f0c040;-fx-font-size:15px;-fx-font-weight:bold;");
        c.getChildren().add(t); return c;
    }

    private TextField textField(String prompt, double width) {
        TextField f = new TextField(); f.setPromptText(prompt);
        f.setStyle("-fx-background-color:#2a2a2a;-fx-border-color:#444;-fx-border-radius:4;-fx-background-radius:4;-fx-text-fill:white;-fx-pref-width:" + width + ";-fx-padding:8 12;");
        return f;
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
