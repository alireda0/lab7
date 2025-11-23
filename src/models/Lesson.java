package models;

import java.util.List;

public class Lesson {

    private String lessonId;      
    private String title;
    private String content;
    private List<String> resources;  // Optional (URLs, file paths, etc.)
    private Quiz quiz;
    
    
    // ----------------- Constructors -----------------

    public Lesson() {}

    public Lesson(String lessonId, String title, String content, List<String> resources) {
        this.lessonId = lessonId;
        this.title = title;
        this.content = content;
        this.resources = resources;
    }

    // ----------------- Getters -----------------

    public String getLessonId() {
        return lessonId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public List<String> getResources() {
        return resources;
    }

    // ----------------- Setters -----------------

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setResources(List<String> resources) {
        this.resources = resources;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }
    
}
