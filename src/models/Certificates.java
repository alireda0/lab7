package models;

public class Certificates {
    private String certificateId;
    private int studentId;
    private String courseId;
    private String issueDate;

    public Certificates(String certificateId, int studentId, String courseId, String issueDate) {
        this.certificateId = certificateId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.issueDate = issueDate;
    }

    public String getCertificateId() { return certificateId; }
    public int getStudentId() { return studentId; }
    public String getCourseId() { return courseId; }
    public String getIssueDate() { return issueDate; }
}
