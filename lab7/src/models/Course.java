package models;

import java.util.ArrayList;
import java.util.List;

public class Course {

    private int courseId;
    private String title;
    private String description;
    private int instructorId;
    private List<Lesson> lessons;       // List of lesson objects
    private List<Integer> students;     // List of student IDs

    // -------------------- Constructors --------------------

    public Course() {
        this.lessons = new ArrayList<>();
        this.students = new ArrayList<>();
    }

    public Course(int courseId, String title, String description, int instructorId) {
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.instructorId = instructorId;
        this.lessons = new ArrayList<>();
        this.students = new ArrayList<>();
    }

    // -------------------- Getters --------------------

    public int getCourseId() {
        return courseId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getInstructorId() {
        return instructorId;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }

    public List<Integer> getStudents() {
        return students;
    }

    // -------------------- Setters --------------------

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

    // Remove a lesson by lessonId
    public void removeLesson(int lessonId) {
        lessons.removeIf(l -> l.getLessonId() == lessonId);
    }

    // Edit a lesson (by replacing the old one)
    public void updateLesson(Lesson updatedLesson) {
        for (int i = 0; i < lessons.size(); i++) {
            if (lessons.get(i).getLessonId() == updatedLesson.getLessonId()) {
                lessons.set(i, updatedLesson);
                break;
            }
        }
    }

    // Add student to the course
    public void enrollStudent(int studentId) {
        if (!students.contains(studentId)) {
            students.add(studentId);
        }
    }

    // Check if a student is enrolled
    public boolean isStudentEnrolled(int studentId) {
        return students.contains(studentId);
    }

}
