package jsondatabase;

import models.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

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

                    Student s = new Student(new ArrayList<>(), new ArrayList<>(), userId, username, email, passwordHash, role);
                    s.getEnrolledCourdseIds().addAll(enrolled);
                    s.getCompletedLesssonIds().addAll(completed);
                    users.add(s);

                } else if ("INSTRUCTOR".equals(role)) {
                    Instructor ins = new Instructor(userId, username, email, passwordHash);
                    users.add(ins);
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

                Course c = new Course(courseId, title, description, instructorId);

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
}