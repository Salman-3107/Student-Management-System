 package com.example.student_management_system;

import java.time.LocalDate;

/**
 * Represents a single attendance record.
 */
public class AttendanceRecord {

    public enum Status {
        PRESENT, ABSENT, LATE
    }

    private final Course course;
    private final LocalDate date;
    private final Status status;

    public AttendanceRecord(Course course, LocalDate date, Status status) {
        this.course = course;
        this.date = date;
        this.status = status;
    }

    public Course    getCourse() { return course; }
    public LocalDate getDate()   { return date; }
    public Status    getStatus() { return status; }

    public String getStatusLabel() {
        switch (status) {
            case PRESENT: return "Present";
            case ABSENT:  return "Absent";
            case LATE:    return "Late";
            default:      return "Unknown";
        }
    }
}
