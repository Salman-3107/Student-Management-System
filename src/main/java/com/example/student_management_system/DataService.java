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
                   COALESCE(att.present_count, 0) AS present_count,
                   COALESCE(att.total_count, 0) AS total_count,
                   COALESCE(gr.class_test_mark, 0.00) AS class_test_mark,
                   COALESCE(gr.assignment_mark, 0.00) AS assignment_mark,
                   COALESCE(gr.mid_mark, 0.00) AS mid_mark,
                   COALESCE(gr.final_mark, 0.00) AS final_mark,
                   COALESCE(gr.published, FALSE) AS published
            FROM enrollments e
            LEFT JOIN users u
                   ON u.username = e.student_username
            LEFT JOIN grade_records gr
                   ON gr.student_username = e.student_username
                  AND gr.offering_id = e.offering_id
            LEFT JOIN (
                SELECT student_username,
                       offering_id,
                       SUM(CASE WHEN status <> 'ABSENT' THEN 1 ELSE 0 END) AS present_count,
                       COUNT(*) AS total_count
                FROM attendance_records
                WHERE offering_id = ?
                GROUP BY student_username, offering_id
            ) att
                   ON att.student_username = e.student_username
                  AND att.offering_id = e.offering_id
            WHERE e.offering_id = ?
              AND e.status = 'ENROLLED'
            ORDER BY e.student_username
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, offeringId);
            ps.setInt(2, offeringId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int presentCount = rs.getInt("present_count");
                    int totalCount = rs.getInt("total_count");
                    double attendancePct = totalCount == 0 ? 0.0 : (presentCount * 100.0) / totalCount;
                    double attendanceMark = convertAttendancePercentageToMark(attendancePct);
                    double classTestMark = rs.getDouble("class_test_mark");
                    double finalMark = rs.getDouble("final_mark");
                    double totalMark = attendanceMark + classTestMark + finalMark;
                    double totalPct = (totalMark / 300.0) * 100.0;
                    String[] gradeInfo = getLetterGradeInfo(totalPct);

                    rows.add(new String[]{
                            rs.getString("student_username"),
                            rs.getString("full_name"),
                            String.format("%.2f", attendanceMark),
                            String.format("%.2f", classTestMark),
                            String.format("%.2f", rs.getDouble("assignment_mark")),
                            String.format("%.2f", rs.getDouble("mid_mark")),
                            String.format("%.2f", finalMark),
                            String.format("%.2f", totalMark),
                            gradeInfo[0],
                            gradeInfo[1],
                            rs.getBoolean("published") ? "Yes" : "No",
                            String.format("%.0f%%", attendancePct),
                            presentCount + "/" + totalCount
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
        double attendancePct = getAttendancePercentage(studentUsername, offeringId);
        double safeAttendance = convertAttendancePercentageToMark(attendancePct);
        double safeClassTest = clamp(classTest, 0, 60);
        double safeWrittenMain = clamp(writtenMain, 0, 210);
        double total = safeAttendance + safeClassTest + safeWrittenMain;
        double pct = (total / 300.0) * 100.0;

        String[] gradeInfo = getLetterGradeInfo(pct);
        String letter = gradeInfo[0];
        double gradePoint = Double.parseDouble(gradeInfo[1]);

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

    public double getAttendancePercentage(String studentUsername, int offeringId) {
        String sql = """
                SELECT COALESCE(100.0 * SUM(CASE WHEN status <> 'ABSENT' THEN 1 ELSE 0 END) / NULLIF(COUNT(*), 0), 0)
                FROM attendance_records
                WHERE student_username = ?
                  AND offering_id = ?
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, studentUsername);
            ps.setInt(2, offeringId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    public double convertAttendancePercentageToMark(double attendancePct) {
        if (attendancePct >= 90.0) return 30.0;
        if (attendancePct >= 80.0) return 27.0;
        if (attendancePct >= 70.0) return 24.0;
        if (attendancePct >= 60.0) return 21.0;
        if (attendancePct >= 50.0) return 18.0;
        if (attendancePct >= 40.0) return 15.0;
        if (attendancePct >= 30.0) return 12.0;
        if (attendancePct >= 20.0) return 9.0;
        if (attendancePct >= 10.0) return 6.0;
        return 0.0;
    }

    private String[] getLetterGradeInfo(double pct) {
        if (pct >= 80) return new String[]{"A+", "4.00"};
        if (pct >= 75) return new String[]{"A", "3.75"};
        if (pct >= 70) return new String[]{"A-", "3.50"};
        if (pct >= 65) return new String[]{"B+", "3.25"};
        if (pct >= 60) return new String[]{"B", "3.00"};
        if (pct >= 55) return new String[]{"B-", "2.75"};
        if (pct >= 50) return new String[]{"C+", "2.50"};
        if (pct >= 45) return new String[]{"C", "2.25"};
        if (pct >= 40) return new String[]{"D", "2.00"};
        return new String[]{"F", "0.00"};
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
