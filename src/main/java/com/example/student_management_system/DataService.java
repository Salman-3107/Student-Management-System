package com.example.student_management_system;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DataService {

    private static DataService instance;

    private DataService() {
    }

    public static DataService getInstance() {
        if (instance == null) {
            instance = new DataService();
        }
        return instance;
    }

    // ─────────────────────────────────────────
    // STUDENT COURSES
    // ─────────────────────────────────────────

    public List<Course> getCoursesForStudent(String username) {
        List<Course> courses = new ArrayList<>();

        String sql = """
                SELECT c.course_code,
                       c.course_title,
                       c.credit_hours,
                       co.section_name,
                       CONCAT('L', co.level, 'T', co.term) AS lt,
                       COALESCE(co.teacher_username, 'N/A') AS teacher_username
                FROM enrollments e
                JOIN course_offerings co ON e.offering_id = co.id
                JOIN courses c ON co.course_id = c.id
                WHERE e.student_username = ?
                  AND e.status = 'ENROLLED'
                ORDER BY co.level, co.term, c.course_code
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    courses.add(new Course(
                            rs.getString("course_code"),
                            rs.getString("course_title"),
                            rs.getString("teacher_username"),
                            (int) Math.round(rs.getDouble("credit_hours")),
                            rs.getString("lt") + " / Sec " + rs.getString("section_name")
                    ));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return courses;
    }

    // ─────────────────────────────────────────
    // STUDENT GRADES
    // ─────────────────────────────────────────

    public List<Grade> getGradesForStudent(String username) {
        List<Grade> grades = new ArrayList<>();

        String sql = """
                SELECT c.course_code,
                       c.course_title,
                       c.credit_hours,
                       COALESCE(co.teacher_username, 'N/A') AS teacher_username,
                       COALESCE(gr.attendance_mark, 0.00) AS attendance_mark,
                       COALESCE(gr.class_test_mark, 0.00) AS class_test_mark,
                       COALESCE(gr.assignment_mark, 0.00) AS assignment_mark,
                       COALESCE(gr.mid_mark, 0.00) AS mid_mark,
                       COALESCE(gr.final_mark, 0.00) AS final_mark,
                       COALESCE(gr.total_mark, 0.00) AS total_mark,
                       COALESCE(gr.letter_grade, '0.00') AS letter_grade,
                       COALESCE(gr.grade_point, 0.00) AS grade_point,
                       COALESCE(gr.published, FALSE) AS published
                FROM grade_records gr
                JOIN course_offerings co ON gr.offering_id = co.id
                JOIN courses c ON co.course_id = c.id
                WHERE gr.student_username = ?
                ORDER BY co.level, co.term, c.course_code
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Course course = new Course(
                            rs.getString("course_code"),
                            rs.getString("course_title"),
                            rs.getString("teacher_username"),
                            (int) Math.round(rs.getDouble("credit_hours")),
                            ""
                    );

                    grades.add(new Grade(
                            course,
                            rs.getDouble("attendance_mark"),
                            rs.getDouble("class_test_mark"),
                            rs.getDouble("assignment_mark"),
                            rs.getDouble("mid_mark"),
                            rs.getDouble("final_mark"),
                            rs.getDouble("total_mark"),
                            rs.getString("letter_grade"),
                            rs.getDouble("grade_point"),
                            rs.getBoolean("published")
                    ));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return grades;
    }

    public List<String[]> getTeacherOfferings(String teacherUsername) {
        List<String[]> offerings = new ArrayList<>();

        String sql = """
            SELECT co.id,
                   c.course_code,
                   c.course_title,
                   CONCAT('L', co.level, 'T', co.term) AS lt,
                   co.section_name
            FROM course_offerings co
            JOIN courses c ON co.course_id = c.id
            WHERE co.teacher_username = ?
              AND co.status = 'ACTIVE'
            ORDER BY co.level, co.term, co.section_name, c.course_code
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, teacherUsername);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    offerings.add(new String[]{
                            rs.getString("id"),
                            rs.getString("course_code"),
                            rs.getString("course_title"),
                            rs.getString("lt"),
                            rs.getString("section_name")
                    });
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return offerings;
    }

    public List<String[]> getStudentsForOfferingWithGrades(int offeringId) {
        List<String[]> rows = new ArrayList<>();

        String sql = """
            SELECT e.student_username,
                   COALESCE(u.full_name, e.student_username) AS full_name,
                   COALESCE(gr.attendance_mark, 0.00) AS attendance_mark,
                   COALESCE(gr.class_test_mark, 0.00) AS class_test_mark,
                   COALESCE(gr.assignment_mark, 0.00) AS assignment_mark,
                   COALESCE(gr.mid_mark, 0.00) AS mid_mark,
                   COALESCE(gr.final_mark, 0.00) AS final_mark,
                   COALESCE(gr.total_mark, 0.00) AS total_mark,
                   COALESCE(gr.letter_grade, '0.00') AS letter_grade,
                   COALESCE(gr.grade_point, 0.00) AS grade_point,
                   COALESCE(gr.published, FALSE) AS published
            FROM enrollments e
            LEFT JOIN users u
                   ON u.username = e.student_username
            LEFT JOIN grade_records gr
                   ON gr.student_username = e.student_username
                  AND gr.offering_id = e.offering_id
            WHERE e.offering_id = ?
              AND e.status = 'ENROLLED'
            ORDER BY e.student_username
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, offeringId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new String[]{
                            rs.getString("student_username"),
                            rs.getString("full_name"),
                            String.format("%.2f", rs.getDouble("attendance_mark")),
                            String.format("%.2f", rs.getDouble("class_test_mark")),
                            String.format("%.2f", rs.getDouble("assignment_mark")),
                            String.format("%.2f", rs.getDouble("mid_mark")),
                            String.format("%.2f", rs.getDouble("final_mark")),
                            String.format("%.2f", rs.getDouble("total_mark")),
                            rs.getString("letter_grade"),
                            String.format("%.2f", rs.getDouble("grade_point")),
                            rs.getBoolean("published") ? "Yes" : "No"
                    });
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rows;
    }

    public void saveOrUpdateGrade(
            String studentUsername,
            int offeringId,
            double attendance,
            double classTest,
            double writtenMain
    ) {
        double safeAttendance = clamp(attendance, 0, 30);
        double safeClassTest = clamp(classTest, 0, 60);
        double safeWrittenMain = clamp(writtenMain, 0, 210);
        double total = safeAttendance + safeClassTest + safeWrittenMain;
        double pct = (total / 300.0) * 100.0;

        double gradePoint;
        String letter;

        if (pct >= 80) { letter = "A+"; gradePoint = 4.00; }
        else if (pct >= 75) { letter = "A"; gradePoint = 3.75; }
        else if (pct >= 70) { letter = "A-"; gradePoint = 3.50; }
        else if (pct >= 65) { letter = "B+"; gradePoint = 3.25; }
        else if (pct >= 60) { letter = "B"; gradePoint = 3.00; }
        else if (pct >= 55) { letter = "B-"; gradePoint = 2.75; }
        else if (pct >= 50) { letter = "C+"; gradePoint = 2.50; }
        else if (pct >= 45) { letter = "C"; gradePoint = 2.25; }
        else if (pct >= 40) { letter = "D"; gradePoint = 2.00; }
        else { letter = "F"; gradePoint = 0.00; }

        String sql = """
            INSERT INTO grade_records
            (student_username, offering_id, attendance_mark, class_test_mark,
             assignment_mark, mid_mark, final_mark, total_mark,
             letter_grade, grade_point, published)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, FALSE)
            ON DUPLICATE KEY UPDATE
                attendance_mark = VALUES(attendance_mark),
                class_test_mark = VALUES(class_test_mark),
                assignment_mark = VALUES(assignment_mark),
                mid_mark = VALUES(mid_mark),
                final_mark = VALUES(final_mark),
                total_mark = VALUES(total_mark),
                letter_grade = VALUES(letter_grade),
                grade_point = VALUES(grade_point),
                published = FALSE
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, studentUsername);
            ps.setInt(2, offeringId);
            ps.setDouble(3, safeAttendance);
            ps.setDouble(4, safeClassTest);
            ps.setDouble(5, 0.0);
            ps.setDouble(6, 0.0);
            ps.setDouble(7, safeWrittenMain);
            ps.setDouble(8, total);
            ps.setString(9, letter);
            ps.setDouble(10, gradePoint);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    // ─────────────────────────────────────────
    // STUDENT ATTENDANCE
    // ─────────────────────────────────────────

    public List<AttendanceRecord> getAttendance(String username) {
        List<AttendanceRecord> list = new ArrayList<>();

        String sql = """
                SELECT c.course_code,
                       c.course_title,
                       c.credit_hours,
                       COALESCE(co.teacher_username, 'N/A') AS teacher_username,
                       ar.class_date,
                       ar.status
                FROM attendance_records ar
                JOIN course_offerings co ON ar.offering_id = co.id
                JOIN courses c ON co.course_id = c.id
                WHERE ar.student_username = ?
                ORDER BY ar.class_date DESC, c.course_code
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Course course = new Course(
                            rs.getString("course_code"),
                            rs.getString("course_title"),
                            rs.getString("teacher_username"),
                            (int) Math.round(rs.getDouble("credit_hours")),
                            ""
                    );

                    list.add(new AttendanceRecord(
                            course,
                            rs.getDate("class_date").toLocalDate(),
                            AttendanceRecord.Status.valueOf(rs.getString("status"))
                    ));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ─────────────────────────────────────────
    // ADMIN STATS
    // ─────────────────────────────────────────

    public int getTotalStudents() {
        return getCount("SELECT COUNT(*) FROM students");
    }

    public int getTotalTeachers() {
        return getCount("SELECT COUNT(*) FROM teachers");
    }

    public int getTotalCourses() {
        return getCount("SELECT COUNT(*) FROM courses");
    }

    public int getTotalDepartments() {
        return getCount("SELECT COUNT(DISTINCT department) FROM courses");
    }

    // ─────────────────────────────────────────
    // TEACHER STATS
    // ─────────────────────────────────────────

    public int getPendingGradingCount(String teacherUsername) {
        String sql = """
                SELECT COUNT(*)
                FROM grade_records gr
                JOIN course_offerings co ON gr.offering_id = co.id
                WHERE co.teacher_username = ?
                  AND COALESCE(gr.published, FALSE) = FALSE
                """;
        return getCount(sql, teacherUsername);
    }

    public int getTodayClassCount(String teacherUsername) {
        String sql = """
                SELECT COUNT(*)
                FROM course_offerings
                WHERE teacher_username = ?
                  AND status = 'ACTIVE'
                """;
        return getCount(sql, teacherUsername);
    }

    public int getTotalStudentsForTeacher(String teacherUsername) {
        String sql = """
                SELECT COUNT(DISTINCT e.student_username)
                FROM enrollments e
                JOIN course_offerings co ON e.offering_id = co.id
                WHERE co.teacher_username = ?
                  AND e.status = 'ENROLLED'
                """;
        return getCount(sql, teacherUsername);
    }

    // ─────────────────────────────────────────
    // INTERNAL HELPERS
    // ─────────────────────────────────────────

    private int getCount(String sql) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getCount(String sql, String param) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, param);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}