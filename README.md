# BUET Student Management System — Combined Project

A JavaFX desktop application for **Bangladesh University of Engineering and Technology (BUET)** with:
- Beautiful public website UI (homepage, academics, admissions, departments, campus life, research, contact, about)
- Full role-based authentication: **Student**, **Teacher**, **Admin**
- Live MySQL database backend with SHA-256 password hashing
- Shared client-server usage through one central MySQL server
- Offline demo fallback (works without a database, but shared real-time updates require MySQL)

---

## Prerequisites

| Tool | Version |
|------|---------|
| JDK  | 21+ |
| Maven | 3.8+ |
| MySQL | 8.0+ (optional — app runs in demo mode without it) |
| IntelliJ IDEA / any IDE | Any |

---

## Quick Start

### 1. Database Setup (Optional but recommended)

```bash
mysql -u root -p < database_setup.sql
```

This creates the `student_management` database, all tables, and seeds demo users.

### Client-Server / Multi-User Setup

To use the system as **one server + many clients**:

1. Run **one MySQL server first** on the teacher/admin PC or a dedicated machine.
2. Import `database_setup.sql` into that MySQL server.
3. On **every client PC**, point the app to the **same MySQL server IP**.
4. When a teacher updates grades or publishes them, other logged-in users can press the new **Refresh** button to load the latest data.

Example shared DB URL:

```bash
SMS_DB_URL=jdbc:mysql://192.168.0.100:3306/student_management?useSSL=false&serverTimezone=Asia/Dhaka
SMS_DB_USER=root
SMS_DB_PASSWORD=your_password
```

Replace `192.168.0.100` with the IP address of the machine running MySQL.

### 2. Configure DB Connection (if using MySQL)

Edit `src/main/java/com/example/student_management_system/DBConnection.java`:

```java
private static final String DEFAULT_URL      = "jdbc:mysql://localhost:3306/student_management?useSSL=false&serverTimezone=Asia/Dhaka";
private static final String DEFAULT_USER     = "root";
private static final String DEFAULT_PASSWORD = "your_password_here";
```

Or set environment variables:
```
SMS_DB_URL=jdbc:mysql://localhost:3306/student_management
SMS_DB_USER=root
SMS_DB_PASSWORD=your_password
```

### 3. Run the Application

From IntelliJ: Run → `Launcher.main()`

If you use command line Maven on your own PC:

```bash
mvn javafx:run
```

---

## Demo Credentials (No DB Required)

| Role    | Username | Password    |
|---------|----------|-------------|
| Student | student  | student123  |
| Teacher | teacher  | teacher123  |
| Admin   | admin    | admin123    |

These work even without a database (built-in fallback).

### With Database Seeded

| Role    | Username | Password    |
|---------|----------|-------------|
| Student | 2405001  | student123  |
| Student | 2405002  | student123  |
| Teacher | t.rahman | teacher123  |
| Admin   | admin    | admin123    |

---

## Project Structure

```
src/
├── main/
│   ├── java/com/example/student_management_system/
│   │   ├── BUETApp.java                  # JavaFX Application entry point
│   │   ├── Launcher.java                 # Main class (avoids module issues)
│   │   ├── AuthService.java              # Login: DB first, demo fallback
│   │   ├── DBConnection.java             # MySQL connection (env/property configurable)
│   │   ├── DataService.java              # All DB queries
│   │   ├── NavigationService.java        # Scene routing
│   │   ├── User.java                     # User model (id, username, role)
│   │   ├── BasePageController.java       # Shared nav bar handlers
│   │   ├── HomepageController.java       # Public homepage
│   │   ├── LoginController.java          # Login with Remember Me
│   │   ├── StudentDashboardController.java
│   │   ├── TeacherDashboardController.java
│   │   ├── AdminDashboardController.java
│   │   └── (page controllers: Academics, Admissions, Departments, etc.)
│   └── resources/com/example/student_management_system/
│       ├── homepage.fxml + styles.css
│       ├── login.fxml + login-styles.css
│       ├── student_dashboard.fxml + student_dashboard.css
│       ├── teacher_dashboard.fxml + teacher_dashboard.css
│       ├── admin_dashboard.fxml + admin_dashboard.css
│       └── (academics, admissions, departments, campus life, research, contact, about fxml)
└── database_setup.sql                    # Run once to initialize MySQL
```

---

## Features by Role

### Student Dashboard
- Home overview (name, ID, dept, session, level/term, email)
- Personal info panel
- Grades viewer (per course — attendance, class test, assignment, mid, final, letter grade, GPA)
- Fees & dues tracker

### Teacher Dashboard
- Home with class stats
- Profile panel
- Course management
- Attendance recording
- Marks entry (publish/unpublish grades)
- Assignments manager
- Announcements
- Students list

### Admin Dashboard
- User management (students, teachers, roles)
- Department & course management
- Semester management
- Fee management
- Reports
- Settings

---

## Authentication Flow

1. User selects role type (Student / Teacher / Admin)
2. Enters username + password
3. `AuthService` tries MySQL → falls back to built-in demo users
4. Role validated against selected type
5. Correct dashboard opened via `NavigationService`


## What Changed in This Edited Version

- Homepage now shows all departments, and every department button opens its detail page.
- Student grades page now has a **Refresh Results** button and shows only **published** grades.
- Teacher grading now follows the requested **BUET style**: **30 attendance + 60 CT + 210 written main exam = 300**.
- Saving grades keeps them unpublished until the teacher clicks **Publish Grades**.
- Refresh support is documented for multi-client usage with a shared MySQL server.
