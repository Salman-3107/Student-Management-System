package com.example.student_management_system;

/**
 * Represents a student's grade record for a course.
 * This version matches the database structure in grade_records.
 */
public class Grade {

    private final Course course;

    private final double attendanceMark;
    private final double classTestMark;
    private final double assignmentMark;
    private final double midMark;
    private final double finalMark;

    private final double totalMark;
    private final String letterGrade;
    private final double gradePoint;
    private final boolean published;

    public Grade(
            Course course,
            double attendanceMark,
            double classTestMark,
            double assignmentMark,
            double midMark,
            double finalMark,
            double totalMark,
            String letterGrade,
            double gradePoint,
            boolean published
    ) {
        this.course = course;
        this.attendanceMark = attendanceMark;
        this.classTestMark = classTestMark;
        this.assignmentMark = assignmentMark;
        this.midMark = midMark;
        this.finalMark = finalMark;
        this.totalMark = totalMark;
        this.letterGrade = letterGrade == null ? "0.00" : letterGrade;
        this.gradePoint = gradePoint;
        this.published = published;
    }

    public Course getCourse() {
        return course;
    }

    public double getAttendanceMark() {
        return attendanceMark;
    }

    public double getClassTestMark() {
        return classTestMark;
    }

    public double getAssignmentMark() {
        return assignmentMark;
    }

    public double getMidMark() {
        return midMark;
    }

    public double getFinalMark() {
        return finalMark;
    }

    public double getTotalMark() {
        return totalMark;
    }

    public String getLetterGrade() {
        return letterGrade;
    }

    public double getGradePoint() {
        return gradePoint;
    }

    public boolean isPublished() {
        return published;
    }
}