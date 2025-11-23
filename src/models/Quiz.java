package models;

import java.util.ArrayList;
import java.util.List;

public class Quiz {

    private List<Question> questions;
    private int passingPercentage;  
    private int maxAttempts;

    public Quiz() {
        this.questions = new ArrayList<>();
        this.passingPercentage = 60;
        this.maxAttempts = 0; // 0 = unlimited
    }

    public Quiz(List<Question> questions, int passingPercentage, int maxAttempts) {
        this.questions = questions;
        this.passingPercentage = passingPercentage;
        this.maxAttempts = maxAttempts;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public int getPassingPercentage() {
        return passingPercentage;
    }

    public void setPassingPercentage(int passingPercentage) {
        this.passingPercentage = passingPercentage;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public int totalQuestions() {
        return questions != null ? questions.size() : 0;
    }
}