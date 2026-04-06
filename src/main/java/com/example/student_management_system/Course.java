package com.example.student_management_system;

/**
 * Represents an academic course.
 * This version keeps compatibility with both old and new code.
 */
public class Course {

    private final String courseCode;
    private final String courseTitle;
    private final String teacherName;
    private final int creditHours;
    private final String schedule;

    public Course(String courseCode, String courseTitle, String teacherName, int creditHours, String schedule) {
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.teacherName = teacherName;
        this.creditHours = creditHours;
        this.schedule = schedule;
    }

    public String getCourseCode() {
        return courseCode;
    }

    // New naming
    public String getCourseTitle() {
        return courseTitle;
    }

    public String getTeacherName() {
        return teacherName;
    }

    // Old compatibility naming
    public String getCourseName() {
        return courseTitle;
    }

    public String getInstructor() {
        return teacherName;
    }

    public int getCreditHours() {
        return creditHours;
    }

    public String getSchedule() {
        return schedule;
    }

    @Override
    public String toString() {
        return courseCode + " - " + courseTitle;
    }
}