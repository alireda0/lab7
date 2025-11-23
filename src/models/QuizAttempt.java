package models;

public class QuizAttempt {

    private long timestamp;
    private int score;
    private int correctCount;
    private int totalQuestions;
    private String lessonId;

    public QuizAttempt() {}

    public QuizAttempt(String lessonId, long timestamp, int score, int correctCount, int totalQuestions) {
        this.lessonId = lessonId;
        this.timestamp = timestamp;
        this.score = score;
        this.correctCount = correctCount;
        this.totalQuestions = totalQuestions;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }
}