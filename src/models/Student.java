package models;
import java.util.*;
import org.json.JSONTokener;

public class Student extends User{
    private List<String> enrolledCourseIds;
    private List<String> completedLessonIds;
    private Map<String, List<QuizAttempt>> quizAttemptsByLesson;
    private List<Certificate> certificates;

    
   public Student(List<String> enrolledCourseIds,
               List<String> completedLessonIds,
               int userId,
               String username,
               String email,
               String rawPassword,
               String role,
               boolean isAlreadyHashed) {

    super(userId, username, email, rawPassword, role, isAlreadyHashed);

    this.enrolledCourseIds = enrolledCourseIds != null ? enrolledCourseIds : new ArrayList<>();
    this.completedLessonIds = completedLessonIds != null ? completedLessonIds : new ArrayList<>();
    this.quizAttemptsByLesson = new HashMap<>();
    this.certificates = new ArrayList<>();
}
   
    public List<String> getEnrolledCourseIds() {
        return enrolledCourseIds;
    }

    public void setEnrolledCourseIds(List<String> enrolledCourseIds) {
        this.enrolledCourseIds = enrolledCourseIds;
    }

    public List<String> getCompletedLessonIds() {
        return completedLessonIds;
    }

    public void setCompletedLessonIds(List<String> completedLessonIds) {
        this.completedLessonIds = completedLessonIds;
    }

    public List<Certificate> getCertificates() {
        return certificates;
    }

    public void setCertificates(List<Certificate> certificates) {
        this.certificates = certificates;
    }

    public Map<String, List<QuizAttempt>> getQuizAttemptsByLesson() { return quizAttemptsByLesson; }

    public void addQuizAttempt(String lessonId, QuizAttempt attempt) {
        if (lessonId == null || attempt == null) return;
        quizAttemptsByLesson.computeIfAbsent(lessonId, k -> new ArrayList<>()).add(attempt);
    }

    public List<QuizAttempt> getAttemptsForLesson(String lessonId) {
        return quizAttemptsByLesson.getOrDefault(lessonId, new ArrayList<>());
    }

    public boolean hasPassedLesson(String lessonId, int passingPercentage) {
        List<QuizAttempt> attempts = getAttemptsForLesson(lessonId);
        for (QuizAttempt a : attempts) {
            if (a.getScore() >= passingPercentage) return true;
        }
        return false;
    }

    public List<String> getCompletedLesssonIds() {   // also keeping your spelling
    return completedLessonIds;
    }
    public List<String> getEnrolledCourdseIds() {    // Keeping your misspelled name to avoid breaking code
    return enrolledCourseIds;
    }

    public List<QuizAttempt> getAttemptsForCourseLesson(String courseId, String lid) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    public boolean hasCompletedLesson(String lessonId) {
    return completedLessonIds.contains(lessonId);
    }

   public void markLessonCompleted(String lessonId) {
    if (!completedLessonIds.contains(lessonId)) {
        completedLessonIds.add(lessonId);
    }
    }

    public void enrollInCourse(String courseId) {
    if (!enrolledCourseIds.contains(courseId)) {
        enrolledCourseIds.add(courseId);
    }
   }
    
    public void addCertificate(Certificate c) {
    certificates.add(c);
    }

}
