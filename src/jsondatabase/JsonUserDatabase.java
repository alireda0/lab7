/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jsondatabase;

/**
 *
 * @author Mizoo
 */

import models.Instructor;
import models.Student;
import models.User;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * JsonUserDatabase
 * - Loads/saves users.json
 * - Exposes user helpers for Student/Instructor
 * - Uses your existing model APIs exactly (no changes)
 */
public class JsonUserDatabase {

    private final File usersFile;
    private final Map<Integer, User> usersById = new HashMap<>();

    public JsonUserDatabase(String usersPath) {
        this.usersFile = new File(usersPath);
        loadUsers();
    }

    // ---------- Load / Save ----------
    private synchronized void loadUsers() {
        usersById.clear();
        if (!usersFile.exists()) return;

        try (Reader reader = new InputStreamReader(new FileInputStream(usersFile), StandardCharsets.UTF_8)) {
            JSONArray arr = new JSONArray(new JSONTokener(reader));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                try {
                    String role = obj.optString("role", "").toUpperCase();

                    if (role.equals(User.ROLE_STUDENT)) {
                        List<String> enrolled = jsonArrayToStringList(obj.optJSONArray("enrolledCourseIds"));
                        List<String> completed = jsonArrayToStringList(obj.optJSONArray("completedLessonIds"));

                        // Matches your Student constructor signature exactly:
                        Student s = new Student(
                                enrolled,
                                completed,
                                obj.getInt("userId"),
                                obj.getString("username"),
                                obj.getString("email"),
                                obj.optString("passwordHash", obj.optString("password", "")),
                                role
                        );
                        usersById.put(s.getUserId(), s);

                    } else if (role.equals(User.ROLE_INSTRUCTOR)) {
                        int userId = obj.getInt("userId");
                        String username = obj.getString("username");
                        String email = obj.getString("email");
                        String pw = obj.optString("passwordHash", obj.optString("password", ""));

                        Instructor inst = new Instructor(userId, username, email, pw);

                        JSONArray created = obj.optJSONArray("createdCourses");
                        if (created != null) {
                            for (int j = 0; j < created.length(); j++) {
                                inst.addCreatedCourse(created.getInt(j));
                            }
                        }

                        usersById.put(inst.getUserId(), inst);

                    } else {
                        System.err.println("JsonUserDatabase: skipping unknown role: " + role);
                    }
                } catch (Exception ex) {
                    System.err.println("JsonUserDatabase: failed to parse user entry: " + ex.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load users.json", e);
        }
    }

    private static List<String> jsonArrayToStringList(org.json.JSONArray arr) {
        List<String> out = new ArrayList<>();
        if (arr == null) return out;
        for (int i = 0; i < arr.length(); i++) out.add(arr.optString(i));
        return out;
    }

    private static org.json.JSONArray stringListToJSONArray(List<String> list) {
        org.json.JSONArray arr = new org.json.JSONArray();
        if (list == null) return arr;
        for (String s : list) arr.put(s);
        return arr;
    }

    public synchronized void saveUsers() {
        JSONArray arr = new JSONArray();
        for (User u : usersById.values()) {
            JSONObject obj = new JSONObject();
            obj.put("userId", u.getUserId());
            obj.put("username", u.getUsername());
            obj.put("email", u.getEmail());
            obj.put("passwordHash", u.getPasswordHash());
            obj.put("role", u.getRole());

            if (u instanceof Student) {
                Student s = (Student) u;
                obj.put("enrolledCourseIds", stringListToJSONArray(s.getEnrolledCourdseIds()));
                obj.put("completedLessonIds", stringListToJSONArray(s.getCompletedLesssonIds()));
            } else if (u instanceof Instructor) {
                Instructor inst = (Instructor) u;
                obj.put("createdCourses", inst.getCreatedCourses());
            }
            arr.put(obj);
        }

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(usersFile), StandardCharsets.UTF_8)) {
            writer.write(arr.toString(4));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save users.json", e);
        }
    }

    // ---------- Helpers (no model changes) ----------
    public synchronized Optional<User> getUserById(int userId) {
        return Optional.ofNullable(usersById.get(userId));
    }

    public synchronized Optional<Student> getStudentById(int studentId) {
        User u = usersById.get(studentId);
        if (u instanceof Student) return Optional.of((Student) u);
        return Optional.empty();
    }

    public synchronized Optional<Instructor> getInstructorById(int instructorId) {
        User u = usersById.get(instructorId);
        if (u instanceof Instructor) return Optional.of((Instructor) u);
        return Optional.empty();
    }

    public synchronized void saveOrUpdateUser(User user) {
        usersById.put(user.getUserId(), user);
        saveUsers();
    }

    public synchronized boolean existsEmail(String email) {
        return usersById.values().stream()
                .anyMatch(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(email));
    }

    public synchronized Collection<User> getAllUsers() {
        return Collections.unmodifiableCollection(usersById.values());
    }
}
