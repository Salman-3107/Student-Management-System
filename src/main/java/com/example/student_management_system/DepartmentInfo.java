package com.example.student_management_system;

import java.util.List;

public class DepartmentInfo {
    private final String name;
    private final String shortName;
    private final String description;       // short tagline shown in hero
    private final List<String> imagePaths;
    private final List<String> aboutParagraphs; // replaces documentPaths

    public DepartmentInfo(String name, String shortName, String description,
                          List<String> imagePaths, List<String> aboutParagraphs) {
        this.name = name;
        this.shortName = shortName;
        this.description = description;
        this.imagePaths = imagePaths;
        this.aboutParagraphs = aboutParagraphs;
    }

    public String getName()                    { return name; }
    public String getShortName()               { return shortName; }
    public String getDescription()             { return description; }
    public List<String> getImagePaths()        { return imagePaths; }
    public List<String> getAboutParagraphs()   { return aboutParagraphs; }

    // Keep backward compatibility
    public List<String> getDocumentPaths()     { return List.of(); }
}