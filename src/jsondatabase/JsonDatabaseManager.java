package jsondatabase;

import models.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class JsonDatabaseManager {

    private static final String DATA_FOLDER = "data";
    private static final String USERS_FILE = DATA_FOLDER + "/users.json";
    private static final String COURSES_FILE = DATA_FOLDER + "/courses.json";

    public JsonDatabaseManager() {
        ensureDataFilesExist();
    }

    // ===================================================================
    // Helpers: ensure data folder/files
    // ===================================================================
    
    // -------------------------------------------------------------------
    // Ensure folder and files exist
    // -------------------------------------------------------------------
    
    private void ensureDataFilesExist() {
        try {
            File folder = new File(DATA_FOLDER);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File usersFile = new File(USERS_FILE);
            if (!usersFile.exists()) {
                usersFile.createNewFile();
                try (PrintWriter pw = new PrintWriter(new FileWriter(usersFile))) {
                    pw.println("[]");
                }
            }

            File coursesFile = new File(COURSES_FILE);
            if (!coursesFile.exists()) {
                coursesFile.createNewFile();
                try (PrintWriter pw = new PrintWriter(new FileWriter(coursesFile))) {
                    pw.println("[]");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ===================================================================
    // USERS — load
    // ===================================================================
    public List<User> loadUsers() {

        ensureDataFilesExist();
        List<User> users = new ArrayList<>();

        try {
            String json = new String(Files.readAllBytes(Paths.get(USERS_FILE)));
            if (json.trim().isEmpty()) {
                return users;
            }

            JSONArray arr = new JSONArray(json);

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                
                String role = obj.optString("role", "STUDENT");
                int userId = obj.optInt("userId", -1);
                String username = obj.optString("username", "");
                String email = obj.optString("email", "");
                String passwordHash = obj.optString("passwordHash", "");

                if (role.equalsIgnoreCase("STUDENT")) {
                    
                    // --- FIX 1: Key name must match saveUsers ("enrolledCourseIds") ---
                    List<String> enrolled = new ArrayList<>();
                    JSONArray enrolledArr = obj.optJSONArray("enrolledCourseIds"); 
                    if (enrolledArr != null) {
                        for (int j = 0; j < enrolledArr.length(); j++) {
                            enrolled.add(enrolledArr.getString(j));
                        }
                    }

                    List<String> completed = new ArrayList<>();
                    JSONArray completedArr = obj.optJSONArray("completedLessonIds");
                    if (completedArr != null) {
                        for (int j = 0; j < completedArr.length(); j++) {
                            completed.add(completedArr.getString(j));
                        }
                    }

                    // Create Student
                    Student s = new Student(enrolled, completed, userId, username, email, passwordHash, "STUDENT", true);

                    // Load Quiz Attempts
                    JSONArray attemptsArr = obj.optJSONArray("quizAttempts");
                    if (attemptsArr != null) {
                        for (int j = 0; j < attemptsArr.length(); j++) {
                            JSONObject qaObj = attemptsArr.getJSONObject(j);
                            String lessonId = qaObj.getString("lessonId");

                            QuizAttempt attempt = new QuizAttempt(
                                    lessonId,
                                    qaObj.optLong("timestamp"),
                                    qaObj.optInt("score"),
                                    qaObj.optInt("correctCount"),
                                    qaObj.optInt("totalQuestions")
                            );
                            s.addQuizAttempt(lessonId, attempt);
                        }
                    }

                    // Load Certificates
                    JSONArray certArr = obj.optJSONArray("certificates");
                    if (certArr != null) {
                        for (int j = 0; j < certArr.length(); j++) {
                            JSONObject cObj = certArr.getJSONObject(j);
                            Certificate cert = new Certificate(
                                    cObj.optString("certificateId"),
                                    cObj.optInt("studentId"),
                                    cObj.optString("courseId"),
                                    cObj.optString("issueDate")
                            );
                            s.addCertificate(cert);
                        }
                    }

                    users.add(s);
                    
                } else if (role.equalsIgnoreCase("INSTRUCTOR")) {
                    users.add(new Instructor(userId, username, email, passwordHash, true));
                } else if (role.equalsIgnoreCase("ADMIN")) {
                    users.add(new Admin(userId, username, email, passwordHash, true));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }

    // ===================================================================
    // USERS — save
    // ===================================================================
    public void saveUsers(List<User> users) {

        ensureDataFilesExist();
        JSONArray arr = new JSONArray();

        for (User u : users) {
            JSONObject obj = new JSONObject();

            obj.put("userId", u.getUserId());
            obj.put("username", u.getUsername());
            obj.put("email", u.getEmail());
            obj.put("passwordHash", u.getPasswordHash());
            obj.put("role", u.getRole());

            if (u instanceof Student s) {
                // FIX 1: Standardized key name
                obj.put("enrolledCourseIds", new JSONArray(s.getEnrolledCourseIds()));
                obj.put("completedLessonIds", new JSONArray(s.getCompletedLessonIds()));

                // Save Certificates
                JSONArray certArr = new JSONArray();
                for (Certificate c : s.getCertificates()) {
                    JSONObject cObj = new JSONObject();
                    cObj.put("certificateId", c.getCertificateId());
                    cObj.put("studentId", c.getStudentId());
                    cObj.put("courseId", c.getCourseId());
                    cObj.put("issueDate", c.getIssueDate());
                    certArr.put(cObj);
                }
                obj.put("certificates", certArr);

                // Save Quiz Attempts
                JSONArray attemptsArr = new JSONArray();
                for (String lessonId : s.getQuizAttemptsByLesson().keySet()) {
                    for (QuizAttempt a : s.getQuizAttemptsByLesson().get(lessonId)) {
                        JSONObject qa = new JSONObject();
                        qa.put("lessonId", lessonId);
                        qa.put("timestamp", a.getTimestamp());
                        qa.put("score", a.getScore());
                        qa.put("correctCount", a.getCorrectCount());
                        qa.put("totalQuestions", a.getTotalQuestions());
                        attemptsArr.put(qa);
                    }
                }
                obj.put("quizAttempts", attemptsArr);
            }

            // Add object to array
            arr.put(obj);
        }

        // Write file ONCE after loop
        try (PrintWriter pw = new PrintWriter(new FileWriter(USERS_FILE))) {
            pw.println(arr.toString(4));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===================================================================
    // QUIZ SYSTEM LOGIC
    // ===================================================================
    
    /**
     * Records an attempt AND marks lesson as completed if passed.
     */
    public void recordQuizAttempt(int studentId, String lessonId, QuizAttempt attempt, int passingPercentage) {
        System.out.println("Recording attempt for Student: " + studentId + ", Lesson: " + lessonId);

        List<User> users = loadUsers(); 
        boolean found = false;

        for (User u : users) {
            if (u instanceof Student s && s.getUserId() == studentId) {
                // 1. Add the attempt
                s.addQuizAttempt(lessonId, attempt);
                
                // 2. FIX 2: Check Pass Condition & Update Completion
                if (attempt.getScore() >= passingPercentage) {
                    if (!s.hasCompletedLesson(lessonId)) {
                        s.markLessonCompleted(lessonId);
                        System.out.println("Lesson " + lessonId + " marked as COMPLETED.");
                    }
                }
                
                found = true;
                break;
            }
        }

        if (found) {
            saveUsers(users); // Save changes (Attempts + Completion status)
            System.out.println("Database updated successfully.");
        } else {
            System.err.println("Student not found! Quiz result NOT saved.");
        }
    }

    // ===================================================================
    // COURSES — load (No changes needed here, kept standard)
    // ===================================================================
    public List<Course> loadCourses() {
        ensureDataFilesExist();
        List<Course> courses = new ArrayList<>();
        try {
            String json = new String(Files.readAllBytes(Paths.get(COURSES_FILE)));
            if (json.trim().isEmpty()) return courses;

            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                Course c = new Course(
                        obj.optString("courseId"),
                        obj.optString("title"),
                        obj.optString("description"),
                        obj.optString("instructorId"),
                        obj.optString("status", "PENDING")
                );

                // Load Students
                JSONArray sArr = obj.optJSONArray("students");
                if (sArr != null)
                    for (int j = 0; j < sArr.length(); j++)
                        c.getStudents().add(sArr.getString(j));

                // Load Lessons & Quizzes
                JSONArray lessonsArr = obj.optJSONArray("lessons");
                if (lessonsArr != null) {
                    for (int j = 0; j < lessonsArr.length(); j++) {
                        JSONObject lObj = lessonsArr.getJSONObject(j);
                        Lesson l = new Lesson(
                            lObj.optString("lessonId"),
                            lObj.optString("title"),
                            lObj.optString("content"),
                            new ArrayList<>()
                        );

                        // Quiz Loading
                        if (lObj.has("quiz")) {
                            JSONObject qObj = lObj.getJSONObject("quiz");
                            Quiz quiz = new Quiz();
                            quiz.setPassingPercentage(qObj.optInt("passingPercentage", 60));
                            quiz.setMaxAttempts(qObj.optInt("maxAttempts", 0));

                            List<Question> questions = new ArrayList<>();
                            JSONArray qArr = qObj.optJSONArray("questions");
                            if (qArr != null) {
                                for (int k = 0; k < qArr.length(); k++) {
                                    JSONObject qItem = qArr.getJSONObject(k);
                                    List<String> opts = new ArrayList<>();
                                    JSONArray optArr = qItem.getJSONArray("options");
                                    for(int m=0; m<optArr.length(); m++) opts.add(optArr.getString(m));
                                    
                                    questions.add(new Question(
                                            qItem.getString("questionText"),
                                            opts,
                                            qItem.getInt("correctOptionIndex")
                                    ));
                                }
                            }
                            quiz.setQuestions(questions);
                            l.setQuiz(quiz);
                        }
                        c.getLessons().add(l);
                    }
                }
                courses.add(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return courses;
    }

    public void saveCourses(List<Course> courses) {
        ensureDataFilesExist();
        JSONArray arr = new JSONArray();
        for (Course c : courses) {
            JSONObject obj = new JSONObject();
            obj.put("courseId", c.getCourseId());
            obj.put("title", c.getTitle());
            obj.put("description", c.getDescription());
            obj.put("instructorId", c.getInstructorId());
            obj.put("status", c.getStatus());
            obj.put("students", new JSONArray(c.getStudents()));

            JSONArray lessonsArr = new JSONArray();
            for (Lesson l : c.getLessons()) {
                JSONObject lObj = new JSONObject();
                lObj.put("lessonId", l.getLessonId());
                lObj.put("title", l.getTitle());
                lObj.put("content", l.getContent());
                
                if (l.getQuiz() != null) {
                    Quiz q = l.getQuiz();
                    JSONObject qObj = new JSONObject();
                    qObj.put("passingPercentage", q.getPassingPercentage());
                    qObj.put("maxAttempts", q.getMaxAttempts());
                    
                    JSONArray qArr = new JSONArray();
                    for (Question qs : q.getQuestions()) {
                        JSONObject qItem = new JSONObject();
                        qItem.put("questionText", qs.getQuestionText());
                        qItem.put("options", new JSONArray(qs.getOptions()));
                        qItem.put("correctOptionIndex", qs.getCorrectOptionIndex());
                        qArr.put(qItem);
                    }
                    qObj.put("questions", qArr);
                    lObj.put("quiz", qObj);
                }
                lessonsArr.put(lObj);
            }
            obj.put("lessons", lessonsArr);
            arr.put(obj);
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(COURSES_FILE))) {
            pw.println(arr.toString(4));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // CRUD Helpers
    public User getUserByEmail(String email) {
        return loadUsers().stream().filter(u -> u.getEmail().equalsIgnoreCase(email)).findFirst().orElse(null);
    }
    
    public Course getCourseById(String courseId) {
        return loadCourses().stream().filter(c -> c.getCourseId().equals(courseId)).findFirst().orElse(null);
    }

    public void addCourse(Course c) {
        List<Course> list = loadCourses();
        list.add(c);
        saveCourses(list);
    }

    public void updateCourse(Course updatedCourse) {
    List<Course> courses = loadCourses(); // reload fresh list from JSON
    boolean found = false;

    for (int i = 0; i < courses.size(); i++) {
        if (courses.get(i).getCourseId().equals(updatedCourse.getCourseId())) {
            courses.set(i, updatedCourse); // replace old course
            found = true;
            break;
        }
    }

    if (found) {
        saveCourses(courses); // persist changes to JSON
    } else {
        System.out.println("Course not found in DB!");
    }
    }

    public List<Course> getVisibleCoursesForStudents() {
        return loadCourses().stream()
                .filter(c -> c.getStatus().equalsIgnoreCase("APPROVED"))
                .collect(Collectors.toList());
    }

    public void approveCourse(String courseId) {
        Course c = getCourseById(courseId);
        if (c != null) {
            c.setStatus("APPROVED");
            updateCourse(c);
        }
    }

    public void rejectCourse(String courseId) {
        Course c = getCourseById(courseId);
        if (c != null) {
            c.setStatus("REJECTED");
            updateCourse(c);
        }
    }

    // ===================================================================
    // QUIZ SYSTEM
    // ===================================================================
    public void recordQuizAttempt(int studentId, String lessonId, QuizAttempt attempt) {

        List<User> users = loadUsers();

        for (User u : users) {
            if (u instanceof Student s && s.getUserId() == studentId) {
                s.addQuizAttempt(lessonId, attempt);
                saveUsers(users);
                return;
            }
        }
    }
    
    public void addUser(User user) {
        List<User> users = loadUsers();
        users.add(user);
        saveUsers(users);
    }
    
    public void updateUser(User updated) {
    // 1) If this is a student, auto-attach certificates before saving
    if (updated instanceof Student) {
        ensureCertificatesForStudentInMemory((Student) updated);
    }
    List<User> list = loadUsers();
    boolean found = false;

    for (int i = 0; i < list.size(); i++) {
        if (list.get(i).getUserId() == updated.getUserId()) {
            list.set(i, updated);
            found = true;
            break;
        }
    }

    if (!found) {
        list.add(updated);
    }

    saveUsers(list);
}

    public double getLessonAverageScore(String lessonId) {
        List<User> users = loadUsers();
        double totalScore = 0;
        int studentCount = 0;

        for (User u : users) {
            if (u instanceof Student s) {
                List<QuizAttempt> attempts = s.getAttemptsForLesson(lessonId);
                if (attempts != null && !attempts.isEmpty()) {
                    // Use the student's HIGHEST score for the average
                    int maxScore = 0;
                    for (QuizAttempt qa : attempts) {
                        if (qa.getScore() > maxScore) maxScore = qa.getScore();
                    }
                    totalScore += maxScore;
                    studentCount++;
                }
            }
        }

        return studentCount == 0 ? 0.0 : totalScore / studentCount;
    }
    public double getCourseCompletionRate(String courseId) {
        Course c = getCourseById(courseId);
        if (c == null || c.getLessons().isEmpty()) return 0.0;

        List<User> users = loadUsers();
        int totalEnrolled = 0;
        int totalCompletedLessons = 0;
        int lessonCount = c.getLessons().size();

        for (User u : users) {
            if (u instanceof Student s && s.getEnrolledCourseIds().contains(courseId)) {
                totalEnrolled++;
                // Count how many lessons of THIS course the student has completed
                for (Lesson l : c.getLessons()) {
                    if (s.getCompletedLessonIds().contains(l.getLessonId())) {
                        totalCompletedLessons++;
                    }
                }
            }
        }

        if (totalEnrolled == 0) return 0.0;

        double totalPossible = totalEnrolled * lessonCount;
        return (totalCompletedLessons / totalPossible) * 100.0;
    }
    public Map<String, Double> getCoursePerformanceData(String courseId) {
        Course c = getCourseById(courseId);
        Map<String, Double> data = new LinkedHashMap<>(); // LinkedHashMap keeps order
        
        if (c != null) {
            for (Lesson l : c.getLessons()) {
                double avg = getLessonAverageScore(l.getLessonId());
                data.put(l.getTitle(), avg);
            }
        }
        return data;
    }
    public int getQuizAttemptCount(int studentId, String lessonId) {
    for (User u : loadUsers()) {
        if (u instanceof Student s && s.getUserId() == studentId) {
            return s.getAttemptsForLesson(lessonId).size();
        }
    }
    return 0;
}
   public boolean canTakeQuiz(int studentId, String lessonId, int maxAttempts) {
    if (maxAttempts <= 0) return true; // treat 0 as unlimited
    int used = getQuizAttemptCount(studentId, lessonId);
    return used < maxAttempts;
}
   public boolean isCourseCompleted(Student s, Course c) {
    for (Lesson l : c.getLessons()) {
        if (!s.hasCompletedLesson(l.getLessonId()))
            return false;
    }
    return true;
}

   private void ensureCertificatesForStudentInMemory(Student s) {
    if (s == null) return;

    // 1) Get list of enrolled courseIds
    List<String> enrolled = s.getEnrolledCourseIds();
    if (enrolled == null || enrolled.isEmpty()) {
        return;
    }

    // 2) Build a set of courseIds that already have a certificate
    Set<String> certifiedCourses = s.getCertificates()
            .stream()
            .map(Certificate::getCourseId)
            .collect(Collectors.toSet());

    // 3) Load all courses to check completion
    List<Course> allCourses = loadCourses();

    for (Course c : allCourses) {
        String courseId = c.getCourseId();

        // Student must be enrolled in this course
        if (!enrolled.contains(courseId)) continue;

        // Skip courses with no lessons
        if (c.getLessons() == null || c.getLessons().isEmpty()) continue;

        // Course must be fully completed by this student
        if (!isCourseCompleted(s, c)) continue;

        // If certificate for this course already exists, skip
        if (certifiedCourses.contains(courseId)) continue;

        // 4) Create a new certificate for this course
        Certificate cert = new Certificate(
                java.util.UUID.randomUUID().toString(),
                s.getUserId(),
                courseId,
                java.time.LocalDate.now().toString()
        );

        s.addCertificate(cert);
        certifiedCourses.add(courseId); // so we don't add duplicates in this loop
    }
}
}