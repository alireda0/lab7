package models;

import java.util.ArrayList;
import java.util.List;

public class Course {

    private String courseId;              // changed from int → String
    private String title;
    private String description;
    private String instructorId;          // changed from int → String
    private List<Lesson> lessons;         // List of lesson objects
    private List<String> students;        // changed from List<Integer> → List<String>

    // -------------------- Constructors --------------------

    public Course() {
        this.lessons = new ArrayList<>();
        this.students = new ArrayList<>();
    }

    public Course(String courseId, String title, String description, String instructorId) {
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.instructorId = instructorId;
        this.lessons = new ArrayList<>();
        this.students = new ArrayList<>();
    }

    // -------------------- Getters --------------------

    public String getCourseId() {
        return courseId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getInstructorId() {
        return instructorId;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }

    public List<String> getStudents() {
        return students;
    }

    // -------------------- Setters --------------------

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public void setInstructorId(String instructorId) {
        this.instructorId = instructorId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // -------------------- Course Operations --------------------

    // Add a lesson
    public void addLesson(Lesson lesson) {
        lessons.add(lesson);
    }

    // Remove a lesson by ID (String)
    public void removeLesson(String lessonId) {
        lessons.removeIf(l -> l.getLessonId().equals(lessonId));
    }

    // Edit a lesson (by replacing the old one)
    public void updateLesson(Lesson updatedLesson) {
        for (int i = 0; i < lessons.size(); i++) {
            if (lessons.get(i).getLessonId().equals(updatedLesson.getLessonId())) {
                lessons.set(i, updatedLesson);
                break;
            }
        }
    }

    // Add student to the course
    public void enrollStudent(String studentId) {
        if (!students.contains(studentId)) {
            students.add(studentId);
        }
    }

    // Check if a student is enrolled
    public boolean isStudentEnrolled(String studentId) {
        return students.contains(studentId);
    }
}
