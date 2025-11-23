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

    // -------------------------------------------------------------------
    // Ensure folder and files exist
    // -------------------------------------------------------------------
    private void ensureDataFilesExist() {
        try {
            File folder = new File(DATA_FOLDER);
            if (!folder.exists()) folder.mkdirs();

            File usersFile = new File(USERS_FILE);
            if (!usersFile.exists()) new PrintWriter(usersFile).println("[]");

            File coursesFile = new File(COURSES_FILE);
            if (!coursesFile.exists()) new PrintWriter(coursesFile).println("[]");

        } catch (Exception e) {
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
            JSONArray arr = new JSONArray(json);

            for (int i = 0; i < arr.length(); i++) {

                JSONObject obj = arr.getJSONObject(i);
                String role = obj.optString("role", "STUDENT");
                int userId = obj.optInt("userId", -1);
                String username = obj.optString("username", "");
                String email = obj.optString("email", "");
                String passwordHash = obj.optString("passwordHash", "");

                // -------------------------------------------------------------
                // STUDENT
                // -------------------------------------------------------------
                if (role.equalsIgnoreCase("STUDENT")) {

                    // enrolled
                    List<String> enrolled = new ArrayList<>();
                    JSONArray eArr = obj.optJSONArray("enrolledCourseIds");
                    if (eArr != null) {
                        for (int j = 0; j < eArr.length(); j++)
                            enrolled.add(eArr.getString(j));
                    }

                    // completed
                    List<String> completed = new ArrayList<>();
                    JSONArray cArr = obj.optJSONArray("completedLessonIds");
                    if (cArr != null) {
                        for (int j = 0; j < cArr.length(); j++)
                            completed.add(cArr.getString(j));
                    }

                    Student s = new Student(
                            enrolled,
                            completed,
                            userId,
                            username,
                            email,
                            passwordHash,
                            "STUDENT",
                            true
                    );

                    // certificates
                    JSONArray certArr = obj.optJSONArray("certificates");
                    if (certArr != null) {
                        for (int j = 0; j < certArr.length(); j++) {
                            JSONObject c = certArr.getJSONObject(j);
                            s.getCertificates().add(new Certificate(
                                    c.optString("certificateId"),
                                    c.optInt("studentId"),
                                    c.optString("courseId"),
                                    c.optString("issueDate")
                            ));
                        }
                    }

                    // quiz attempts
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

                    users.add(s);
                }

                // -------------------------------------------------------------
                // INSTRUCTOR
                // -------------------------------------------------------------
                else if (role.equalsIgnoreCase("INSTRUCTOR")) {
                    users.add(new Instructor(userId, username, email, passwordHash, true));
                }

                // -------------------------------------------------------------
                // ADMIN
                // -------------------------------------------------------------
                else if (role.equalsIgnoreCase("ADMIN")) {
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

                obj.put("enrolledCourseIds", new JSONArray(s.getEnrolledCourseIds()));
                obj.put("completedLessonIds", new JSONArray(s.getCompletedLessonIds()));

                // certificates
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

                // quiz attempts
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

            arr.put(obj);
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(USERS_FILE))) {
            pw.println(arr.toString(4));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public User getUserByEmail(String email) {
        return loadUsers().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst().orElse(null);
    }

    public void addUser(User user) {
        List<User> users = loadUsers();
        users.add(user);
        saveUsers(users);
    }

    public void updateUser(User updated) {
        List<User> list = loadUsers();
        boolean found = false;

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getUserId() == updated.getUserId()) {
                list.set(i, updated);
                found = true;
                break;
            }
        }

        if (!found) list.add(updated);
        saveUsers(list);
    }

    // ===================================================================
    // COURSES — load
    // ===================================================================
    public List<Course> loadCourses() {

        ensureDataFilesExist();
        List<Course> courses = new ArrayList<>();

        try {
            String json = new String(Files.readAllBytes(Paths.get(COURSES_FILE)));
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

                // students
                JSONArray sArr = obj.optJSONArray("students");
                if (sArr != null)
                    for (int j = 0; j < sArr.length(); j++)
                        c.getStudents().add(sArr.getString(j));

                // lessons
                JSONArray lessonsArr = obj.optJSONArray("lessons");
                if (lessonsArr != null) {

                    for (int j = 0; j < lessonsArr.length(); j++) {

                        JSONObject lObj = lessonsArr.getJSONObject(j);
                        Lesson l = new Lesson();

                        l.setLessonId(lObj.optString("lessonId"));
                        l.setTitle(lObj.optString("title"));
                        l.setContent(lObj.optString("content"));

                        // quiz
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

                                    Question question = new Question(
                                            qItem.getString("questionText"),
                                            jsonArrayToList(qItem.getJSONArray("options")),
                                            qItem.getInt("correctOptionIndex")
                                    );

                                    questions.add(question);
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

    private List<String> jsonArrayToList(JSONArray arr) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) list.add(arr.getString(i));
        return list;
    }

    // ===================================================================
    // COURSES — save
    // ===================================================================
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

    public Course getCourseById(String courseId) {
        return loadCourses().stream()
                .filter(c -> c.getCourseId().equals(courseId))
                .findFirst().orElse(null);
    }

    public void addCourse(Course c) {
        List<Course> list = loadCourses();
        list.add(c);
        saveCourses(list);
    }

    public void updateCourse(Course updated) {

        List<Course> list = loadCourses();
        boolean found = false;

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getCourseId().equals(updated.getCourseId())) {
                list.set(i, updated);
                found = true;
                break;
            }
        }

        if (!found) list.add(updated);
        saveCourses(list);
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

        throw new RuntimeException("Student not found: " + studentId);
    }

    public boolean hasStudentPassedLesson(int studentId, String lessonId, int passingPercentage) {

        for (User u : loadUsers()) {
            if (u instanceof Student s && s.getUserId() == studentId) {
                return s.hasPassedLesson(lessonId, passingPercentage);
            }
        }

        return false;
    }

    public boolean isCourseCompleted(Student s, Course c) {

        for (Lesson l : c.getLessons()) {
            if (!s.hasCompletedLesson(l.getLessonId()))
                return false;
        }

        return true;
    }

    public Certificate generateCertificate(Student s, Course c) {

        Certificate cert = new Certificate(
                UUID.randomUUID().toString(),
                s.getUserId(),
                c.getCourseId(),
                java.time.LocalDate.now().toString()
        );

        s.addCertificate(cert);
        updateUser(s);

        return cert;
    }
}