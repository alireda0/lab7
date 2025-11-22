package models;

import java.util.ArrayList;
import java.util.List;

public class Course {
    private String courseId;
    private String title;
    private String description;
    private String instructorId;
    private List<Lesson> lessons;
    private List<String> students;
    private String status; // "PENDING", "APPROVED", "REJECTED"

    public Course() {
        this.lessons = new ArrayList<>();
        this.students = new ArrayList<>();
        this.status = "PENDING";
    }

    public Course(String courseId, String title, String description, String instructorId, String status) {
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.instructorId = instructorId;
        this.lessons = new ArrayList<>();
        this.students = new ArrayList<>();
        this.status = status == null ? "PENDING" : status;
    }



    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(String instructorId) {
        this.instructorId = instructorId;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }

    public void setLessons(List<Lesson> lessons) {
        this.lessons = lessons;
    }

    public List<String> getStudents() {
        return students;
    }

    public void setStudents(List<String> students) {
        this.students = students;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    
    // existing ops
    public void addLesson(Lesson lesson) { lessons.add(lesson); }
    public void removeLesson(String lessonId) { lessons.removeIf(l -> l.getLessonId().equals(lessonId)); }
    public void updateLesson(Lesson updatedLesson) {
        for (int i = 0; i < lessons.size(); i++) {
            if (lessons.get(i).getLessonId().equals(updatedLesson.getLessonId())) {
                lessons.set(i, updatedLesson);
                break;
            }
        }
    }
    public void enrollStudent(String studentId) { if (!students.contains(studentId)) students.add(studentId); }
    public boolean isStudentEnrolled(String studentId) { return students.contains(studentId); }
}
