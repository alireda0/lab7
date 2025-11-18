package test;

import jsondatabase.JsonDatabaseManager;
import models.*;

import java.util.List;

public class TestDatabaseJson {

    public static void main(String[] args) {

        JsonDatabaseManager db = new JsonDatabaseManager();

        // -------- Load existing users --------
        List<User> users = db.loadUsers();
        System.out.println("Existing Users:");
        for (User u : users) {
            System.out.println(u.getUserId() + " - " + u.getUsername() + " (" + u.getRole() + ")");
        }

        // -------- Load existing courses --------
        List<Course> courses = db.loadCourses();
        System.out.println("\nExisting Courses:");
        for (Course c : courses) {
            System.out.println(c.getCourseId() + " - " + c.getTitle() + " (Instructor: " + c.getInstructorId() + ")");
        }

        // -------- Add a new student --------
        Student newStudent = new Student(
                List.of(), List.of(),
                4, "Charlie", "charlie@example.com", "mypassword", User.ROLE_STUDENT
        );
        db.addUser(newStudent);
        System.out.println("\nAdded new student: " + newStudent.getUsername());

        // -------- Enroll the new student in a course --------
        Course course = db.getCourseById("C101");
        if (course != null) {
            newStudent.enrollInCourse(course.getCourseId());
            course.enrollStudent(String.valueOf(newStudent.getUserId()));
            System.out.println("Enrolled " + newStudent.getUsername() + " in course " + course.getTitle());
        }

        // -------- Mark a lesson completed --------
        if (course != null && !course.getLessons().isEmpty()) {
            String lessonId = course.getLessons().get(0).getLessonId();
            newStudent.markLessonCompleted(lessonId);
            System.out.println("Marked lesson " + lessonId + " completed for " + newStudent.getUsername());
        }

        // -------- Save updates --------
        db.updateUser(newStudent);
        if (course != null) db.updateCourse(course);

        System.out.println("\nData saved successfully!");

        // -------- Reload users and courses to verify --------
        System.out.println("\nReloading Users and Courses from JSON...");
        List<User> updatedUsers = db.loadUsers();
        List<Course> updatedCourses = db.loadCourses();

        System.out.println("\nUsers after update:");
        for (User u : updatedUsers) {
            System.out.println(u.getUserId() + " - " + u.getUsername() + " (" + u.getRole() + ")");
            if (u instanceof Student s) {
                System.out.println("  Enrolled Courses: " + s.getEnrolledCourdseIds());
                System.out.println("  Completed Lessons: " + s.getCompletedLesssonIds());
            }
        }

        System.out.println("\nCourses after update:");
        for (Course c : updatedCourses) {
            System.out.println(c.getCourseId() + " - " + c.getTitle());
            System.out.println("  Students: " + c.getStudents());
            System.out.println("  Lessons:");
            for (Lesson l : c.getLessons()) {
                System.out.println("    " + l.getLessonId() + " - " + l.getTitle());
            }
        }
    }
}