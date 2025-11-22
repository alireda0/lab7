package jsondatabase;

import models.Student;
import models.Lesson;
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

    // -------- Ensure folder and files exist --------
    private void ensureDataFilesExist() {
        try {
            File folder = new File(DATA_FOLDER);
            if (!folder.exists()) folder.mkdirs();

            File usersFile = new File(USERS_FILE); 
            if (!usersFile.exists()) {
                try (PrintWriter pw = new PrintWriter(new FileWriter(usersFile))) {
                    pw.println("[]"); // empty JSON array
                }
            }

            File coursesFile = new File(COURSES_FILE);
            if (!coursesFile.exists()) {
                try (PrintWriter pw = new PrintWriter(new FileWriter(coursesFile))) {
                    pw.println("[]"); // empty JSON array
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ------------------ USERS -------------------
 public List<User> loadUsers() { 
    ensureDataFilesExist();
    List<User> users = new ArrayList<>();

    try {
        String content = new String(Files.readAllBytes(Paths.get(USERS_FILE)));
        JSONArray array = new JSONArray(content);

        for (int i = 0; i < array.length(); i++) {

            JSONObject obj = array.getJSONObject(i);
            String role = obj.getString("role");
            int userId = obj.getInt("userId");
            String username = obj.getString("username");
            String email = obj.getString("email");
            String passwordHash = obj.getString("passwordHash");

            // ============================
            // STUDENT
            // ============================
            if ("STUDENT".equals(role)) {

                JSONArray enrolledArray = obj.optJSONArray("enrolledCourseIds");
                JSONArray completedArray = obj.optJSONArray("completedLessonIds");

                List<String> enrolled = new ArrayList<>();
                List<String> completed = new ArrayList<>();

                if (enrolledArray != null) {
                    for (int j = 0; j < enrolledArray.length(); j++)
                        enrolled.add(enrolledArray.getString(j));
                }

                if (completedArray != null) {
                    for (int j = 0; j < completedArray.length(); j++)
                        completed.add(completedArray.getString(j));
                }

                // NOTE: true => passwordHash is already hashed
                Student s = new Student(
                        new ArrayList<>(), 
                        new ArrayList<>(),
                        userId, username, email, passwordHash,
                        role,
                        true
                );
                
                JSONArray certsArr = obj.optJSONArray("certificates");
                if (certsArr != null) {
                for (int j = 0; j < certsArr.length(); j++) {
                JSONObject cObj = certsArr.getJSONObject(j);
                Certificate cert = new Certificate(
                cObj.getString("certificateId"),
                cObj.getInt("studentId"),
                cObj.getString("courseId"),
                cObj.getString("issueDate")
                );
                s.getCertificates().add(cert);
    }
}

                
                s.getEnrolledCourseIds().addAll(enrolled);
                s.getCompletedLessonIds().addAll(completed);

                users.add(s);
            }

            // ============================
            // INSTRUCTOR
            // ============================
            else if ("INSTRUCTOR".equals(role)) {

                Instructor ins = new Instructor(
                        userId, username, email, passwordHash,
                        true   // ← password already hashed
                );

                users.add(ins);
            }

            // ============================
            // ADMIN
            // ============================
            else if ("ADMIN".equals(role)) {

                Admin admin = new Admin(
                        userId, username, email, passwordHash,
                        true   // ← password already hashed
                );

                users.add(admin);
            }
        }

    } catch (IOException e) {
        e.printStackTrace();
    }

    return users;
}




    public void saveUsers(List<User> users) {
        ensureDataFilesExist();
        JSONArray array = new JSONArray();

        for (User u : users) {
            JSONObject obj = new JSONObject();
            obj.put("userId", u.getUserId());
            obj.put("username", u.getUsername());
            obj.put("email", u.getEmail());
            obj.put("passwordHash", u.getPasswordHash());
            obj.put("role", u.getRole());

            if (u instanceof Student s) {
                obj.put("enrolledCourseIds", new JSONArray(s.getEnrolledCourdseIds()));
                obj.put("completedLessonIds", new JSONArray(s.getCompletedLesssonIds()));
            }

            array.put(obj);
            
            if (u instanceof Student s) {
            obj.put("enrolledCourseIds", new JSONArray(s.getEnrolledCourdseIds()));
            obj.put("completedLessonIds", new JSONArray(s.getCompletedLesssonIds()));

        // quiz attempts
        JSONArray attemptsArr = new JSONArray();
            for (Map.Entry<String, List<QuizAttempt>> e : s.getQuizAttemptsByLesson().entrySet()) {
                for (QuizAttempt a : e.getValue()) {
                    JSONObject at = new JSONObject();
                    at.put("lessonId", e.getKey());
                    at.put("timestamp", a.getTimestamp());
                    at.put("score", a.getScore());
                    at.put("correctCount", a.getCorrectCount());
                    at.put("totalQuestions", a.getTotalQuestions());
                    attemptsArr.put(at);
        }
    }
    obj.put("quizAttempts", attemptsArr);
}


        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(USERS_FILE))) {
            pw.println(array.toString(4));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public User getUserByEmail(String email) {
        for (User u : loadUsers()) {
            if (u.getEmail().equals(email)) return u;
        }
        return null;
    }

    public void addUser(User user) {
        List<User> users = loadUsers();
        users.add(user);
        saveUsers(users);
    }

    public void updateUser(User updatedUser) {
        List<User> users = loadUsers();
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserId() == updatedUser.getUserId()) {
                users.set(i, updatedUser);
                break;
            }
        }
        saveUsers(users);
    }

    // ------------------ COURSES -------------------
    
    public List<Course> loadCourses() {
        ensureDataFilesExist();
        List<Course> courses = new ArrayList<>();

        try {
            String content = new String(Files.readAllBytes(Paths.get(COURSES_FILE)));
            JSONArray array = new JSONArray(content);

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);

                String courseId = obj.getString("courseId");
                String title = obj.getString("title");
                String description = obj.getString("description");
                String instructorId = obj.getString("instructorId");
                String status = obj.optString("status", "PENDING");
                   
                Course c = new Course(courseId, title, description, instructorId, status);

                JSONArray studentsArray = obj.optJSONArray("students");
                if (studentsArray != null) {
                    for (int j = 0; j < studentsArray.length(); j++)
                        c.getStudents().add(studentsArray.getString(j));
                }

                JSONArray lessonsArray = obj.optJSONArray("lessons");
                if (lessonsArray != null) {
                    for (int j = 0; j < lessonsArray.length(); j++) {
                        JSONObject lObj = lessonsArray.getJSONObject(j);
                        Lesson l = new Lesson();
                        l.setLessonId(lObj.getString("lessonId"));
                        l.setTitle(lObj.getString("title"));
                        l.setContent(lObj.getString("content"));
                        c.getLessons().add(l);
                    }
                }

                courses.add(c);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return courses;
    }

    
    public void saveCourses(List<Course> courses) {
        ensureDataFilesExist();
        JSONArray array = new JSONArray();

        for (Course c : courses) {
            JSONObject obj = new JSONObject();
            obj.put("courseId", c.getCourseId());
            obj.put("title", c.getTitle());
            obj.put("description", c.getDescription());
            obj.put("instructorId", c.getInstructorId());
            obj.put("students", new JSONArray(c.getStudents()));
            obj.put("status", c.getStatus());
            
            JSONArray lessonsArray = new JSONArray();
            for (Lesson l : c.getLessons()) {
                JSONObject lObj = new JSONObject();
                lObj.put("lessonId", l.getLessonId());
                lObj.put("title", l.getTitle());
                lObj.put("content", l.getContent());
                lessonsArray.put(lObj);
            }
            obj.put("lessons", lessonsArray);

            array.put(obj);
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(COURSES_FILE))) {
            pw.println(array.toString(4));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public Course getCourseById(String courseId) {
        for (Course c : loadCourses()) {
            if (c.getCourseId().equals(courseId)) return c;
        }
        return null;
    }

    public void addCourse(Course course) {
        List<Course> courses = loadCourses();
        courses.add(course);
        saveCourses(courses);
    }

    public void updateCourse(Course updatedCourse) {
        List<Course> courses = loadCourses();
        for (int i = 0; i < courses.size(); i++) {
            if (courses.get(i).getCourseId().equals(updatedCourse.getCourseId())) {
                courses.set(i, updatedCourse);
                break;
            }
        }
        saveCourses(courses);
    }

    public List<Course> getVisibleCoursesForStudents() {
    return loadCourses().stream()
            .filter(c -> "APPROVED".equalsIgnoreCase(c.getStatus()))
            .collect(Collectors.toList());
}

    // 2) Approve / Reject Course (admin actions)
    public void approveCourse(String courseId) {
        List<Course> courses = loadCourses();
        for (Course c : courses) {
            if (c.getCourseId().equals(courseId)) {
                c.setStatus("APPROVED");
                break;
        }
    }
        saveCourses(courses);
    }

        public void rejectCourse(String courseId) {
        List<Course> courses = loadCourses();
            for (Course c : courses) {
                if (c.getCourseId().equals(courseId)) {
                c.setStatus("REJECTED");
                break;
            }
        }
        saveCourses(courses);
    }

// 4) computeCourseStatistics: return a simple Map<String, JSONObject> where key = lessonId,
// value contains averageScore and completionPercentage
    public Map<String, JSONObject> computeCourseStatistics(String courseId) {
        Map<String, JSONObject> out = new LinkedHashMap<>();
        List<Course> courses = loadCourses();
        Course target = null;
            for (Course c : courses) if (c.getCourseId().equals(courseId)) { target = c; break; }
                if (target == null) return out;

            List<String> lessonIds = target.getLessons().stream().map(Lesson::getLessonId).collect(Collectors.toList());
            List<Student> allStudents = loadUsers().stream()
            .filter(u -> u instanceof Student)
            .map(u -> (Student) u)
            .collect(Collectors.toList());

            for (String lid : lessonIds) {
            // gather all attempts scores for this lesson from all students
            List<Integer> scores = new ArrayList<>();
            int completedCount = 0;
            for (Student s : allStudents) {
            List<QuizAttempt> attempts = s.getAttemptsForCourseLesson(courseId, lid);
            for (QuizAttempt a : attempts) scores.add(a.getScore());
            if (s.hasCompletedLesson(lid)) completedCount++;
        }
        double avg = scores.isEmpty() ? 0.0 : scores.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double completionPct = allStudents.isEmpty() ? 0.0 : (completedCount * 100.0 / allStudents.size());

        JSONObject stat = new JSONObject();
        stat.put("lessonId", lid);
        stat.put("averageScore", avg);
        stat.put("completionPercentage", completionPct);
        stat.put("attemptCount", scores.size());
        out.put(lid, stat);
        }

        return out;
    }
    
        public void recordQuizAttempt(int studentId, String lessonId, QuizAttempt attempt) {
            List<User> users = loadUsers();
            for (User u : users) {
            if (u.getUserId() == studentId && u instanceof Student s) {
            s.addQuizAttempt(lessonId, attempt);
            saveUsers(users);
            return;
        }
            }
        throw new IllegalArgumentException("Student not found: " + studentId);
        }

        // Check if student passed lesson
        public boolean hasStudentPassedLesson(int studentId, String lessonId, int passingPercentage) {
            List<User> users = loadUsers();
                for (User u : users) {
                    if (u.getUserId() == studentId && u instanceof Student s) {
                    return s.hasPassedLesson(lessonId, passingPercentage);
            }
        }
        return false;
        }
        
       public boolean isCourseCompleted(Student s, Course c) {
            // All lessons must be completed
            for (Lesson lesson : c.getLessons()) {
                if (!s.hasCompletedLesson(lesson.getLessonId())) {
                return false;
            }
        }
        return true;
        }
       
        public Certificate generateCertificate(Student s, Course c) {
        String certId = UUID.randomUUID().toString();
        String date = java.time.LocalDate.now().toString();

        Certificate cert = new Certificate(certId, s.getUserId(), c.getCourseId(), date);
        s.addCertificate(cert);

       updateUser(s); // save to users.json

       return cert;
       }


}