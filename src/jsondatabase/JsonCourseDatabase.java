/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jsondatabase;

/**
 *
 * @author Mizoo
 */

import models.Course;
import models.Instructor;
import models.Lesson;
import models.Student;
import models.User;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * JsonCourseDatabase (STRING course/lesson IDs; INT userIds)
 *
 * - Works with Course/Lesson where IDs are strings.
 * - When updating users (Instructor/Student) this class converts String IDs to int
 *   only when necessary (Instructor createdCourses uses int IDs).
 * - It is defensive: if conversion fails (non-numeric IDs), it still keeps course data,
 *   but will skip numeric-only updates to user objects to avoid runtime exceptions.
 */
public class JsonCourseDatabase {

    private final File coursesFile;
    private final Map<String, Course> coursesById = new HashMap<>();
    private final AtomicInteger nextCourseCounter = new AtomicInteger(1);
    private final AtomicInteger nextLessonCounter = new AtomicInteger(1);

    private final JsonUserDatabase userDb; // your existing user DB (expects int userIds in lookup)

    public JsonCourseDatabase(String coursesPath, JsonUserDatabase userDb) {
        this.coursesFile = new File(coursesPath);
        this.userDb = userDb;
        loadCourses();
    }

    // -------------------------
    // Load / Save
    // -------------------------
    private synchronized void loadCourses() {
        coursesById.clear();
        if (!coursesFile.exists()) return;

        try (Reader reader = new InputStreamReader(new FileInputStream(coursesFile), StandardCharsets.UTF_8)) {
            JSONArray arr = new JSONArray(new JSONTokener(reader));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject c = arr.getJSONObject(i);

                String courseId = c.optString("courseId", "");
                String title = c.optString("title", "");
                String description = c.optString("description", "");
                String instructorId = c.optString("instructorId", "");

                Course course = new Course(courseId, title, description, instructorId);

                if (c.has("lessons")) {
                    JSONArray lessonsArr = c.getJSONArray("lessons");
                    for (int j = 0; j < lessonsArr.length(); j++) {
                        JSONObject l = lessonsArr.getJSONObject(j);
                        String lessonId = l.optString("lessonId", "");
                        Lesson lesson = new Lesson(
                                lessonId,
                                l.optString("title", ""),
                                l.optString("content", ""),
                                jsonArrayToStringList(l.optJSONArray("resources"))
                        );
                        course.addLesson(lesson);
                    }
                }

                if (c.has("students")) {
                    JSONArray studentsArr = c.getJSONArray("students");
                    for (int j = 0; j < studentsArr.length(); j++) {
                        // students stored as strings in Course model
                        course.enrollStudent(studentsArr.optString(j));
                    }
                }

                coursesById.put(course.getCourseId(), course);
            }

            // set next counters (try parse numeric string ids; ignore non-numeric)
            int maxCourse = coursesById.keySet().stream()
                    .mapToInt(JsonCourseDatabase::parseIntOrZero)
                    .max().orElse(0);
            nextCourseCounter.set(maxCourse + 1);

            int maxLesson = coursesById.values().stream()
                    .flatMap(x -> x.getLessons().stream())
                    .mapToInt(l -> parseIntOrZero(l.getLessonId()))
                    .max()
                    .orElse(0);
            nextLessonCounter.set(maxLesson + 1);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load courses.json", e);
        }
    }

    private static int parseIntOrZero(String s) {
        if (s == null) return 0;
        try { return Integer.parseInt(s); }
        catch (NumberFormatException ex) { return 0; }
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

    private synchronized void saveCourses() {
        JSONArray arr = new JSONArray();
        for (Course c : coursesById.values()) {
            JSONObject obj = new JSONObject();
            obj.put("courseId", c.getCourseId());
            obj.put("title", c.getTitle());
            obj.put("description", c.getDescription());
            obj.put("instructorId", c.getInstructorId());

            JSONArray lessonsArr = new JSONArray();
            for (Lesson l : c.getLessons()) {
                JSONObject lessonObj = new JSONObject();
                lessonObj.put("lessonId", l.getLessonId());
                lessonObj.put("title", l.getTitle());
                lessonObj.put("content", l.getContent());
                lessonObj.put("resources", l.getResources());
                lessonsArr.put(lessonObj);
            }
            obj.put("lessons", lessonsArr);

            // students are strings in Course model
            obj.put("students", c.getStudents());
            arr.put(obj);
        }

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(coursesFile), StandardCharsets.UTF_8)) {
            writer.write(arr.toString(4));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save courses.json", e);
        }
    }

    // -------------------------
    // Public helpers
    // -------------------------
    public synchronized Optional<Course> getCourseById(String courseId) {
        return Optional.ofNullable(coursesById.get(courseId));
    }

    public synchronized List<Course> getAllCourses() {
        return new ArrayList<>(coursesById.values());
    }

    public synchronized Course saveOrUpdateCourse(Course c) {
        coursesById.put(c.getCourseId(), c);
        saveCourses();
        return c;
    }

    /**
     * Delete course: remove course itself, and update instructor.createdCourses (if instructorId is numeric).
     * For students we remove the courseId string from their enrolled lists (Student uses String lists).
     */
    public synchronized void deleteCourse(String courseId) {
        Course c = coursesById.remove(courseId);
        if (c != null) {
            // update instructor's createdCourses if possible (Instructor uses int ids)
            String instrIdStr = c.getInstructorId();
            Integer instrId = parseIntOrNull(instrIdStr);
            if (instrId != null) {
                userDb.getInstructorById(instrId).ifPresent(inst -> {
                    try {
                        inst.removeCreatedCourse(Integer.parseInt(courseId));
                    } catch (NumberFormatException ignored) {
                        // courseId not numeric — skip removal from instructor
                    }
                    userDb.saveOrUpdateUser(inst);
                });
            }

            // For students: remove string courseId from their enrolledCourse lists
            for (String sid : c.getStudents()) {
                Integer studentIntId = parseIntOrNull(sid);
                if (studentIntId != null) {
                    userDb.getStudentById(studentIntId).ifPresent(st -> {
                        // Student uses String course ID list, so remove String courseId
                        try {
                            st.getEnrolledCourdseIds().remove(courseId);
                            userDb.saveOrUpdateUser(st);
                        } catch (Exception ignored) {}
                    });
                } else {
                    // sid not numeric: try find student by other means? Skip to avoid exceptions
                }
            }

            saveCourses();
        }
    }

    private static Integer parseIntOrNull(String s) {
        if (s == null) return null;
        try { return Integer.valueOf(Integer.parseInt(s)); }
        catch (NumberFormatException ex) { return null; }
    }

    // -------------------------
    // ID generation (returns String IDs)
    // -------------------------
    public synchronized String nextCourseId() {
        return String.valueOf(nextCourseCounter.getAndIncrement());
    }

    public synchronized String nextLessonId() {
        return String.valueOf(nextLessonCounter.getAndIncrement());
    }

    // -------------------------
    // Enrollment helpers
    // -------------------------
    /**
     * Enroll student into course.
     * - Course model stores student IDs as Strings.
     * - User lookups in userDb require int userId, so try to parse studentId string to int.
     * - If parsing fails, enrollment will still be stored in Course but we cannot update the Student object.
     */
    public synchronized void enrollStudentInCourse(String courseId, String studentId) {
        Course c = coursesById.get(courseId);
        if (c == null) throw new IllegalArgumentException("Course not found: " + courseId);

        if (!c.isStudentEnrolled(studentId)) {
            c.enrollStudent(studentId);

            Integer studentIntId = parseIntOrNull(studentId);
            if (studentIntId != null) {
                userDb.getStudentById(studentIntId).ifPresent(st -> {
                    try {
                        st.enrollInCourse(courseId); // Student expects String IDs in enrolled list
                        userDb.saveOrUpdateUser(st);
                    } catch (Exception ignored) {}
                });
            }
            saveCourses();
        }
    }

    public synchronized void unenrollStudentFromCourse(String courseId, String studentId) {
        Course c = coursesById.get(courseId);
        if (c == null) return;
        c.getStudents().remove(studentId);

        Integer studentIntId = parseIntOrNull(studentId);
        if (studentIntId != null) {
            userDb.getStudentById(studentIntId).ifPresent(st -> {
                try {
                    st.getEnrolledCourdseIds().remove(courseId);
                    userDb.saveOrUpdateUser(st);
                } catch (Exception ignored) {}
            });
        }
        saveCourses();
    }

    public synchronized List<String> getEnrolledStudentIds(String courseId) {
        Course c = coursesById.get(courseId);
        if (c == null) return Collections.emptyList();
        return new ArrayList<>(c.getStudents());
    }

    public synchronized List<Student> getEnrolledStudentsForCourse(String courseId) {
        List<Student> out = new ArrayList<>();
        Course c = coursesById.get(courseId);
        if (c == null) return out;

        for (String sid : c.getStudents()) {
            Integer studentIntId = parseIntOrNull(sid);
            if (studentIntId != null) {
                userDb.getStudentById(studentIntId).ifPresent(out::add);
            }
        }
        return out;
    }

    // -------------------------
    // Course & Lesson operations
    // -------------------------
    public synchronized Course createCourse(String instructorId, String title, String description) {
        String id = nextCourseId();
        Course c = new Course(id, title, description, instructorId);
        coursesById.put(id, c);

        // try to update instructor createdCourses (Instructor uses int userId)
        Integer instrInt = parseIntOrNull(instructorId);
        if (instrInt != null) {
            userDb.getInstructorById(instrInt).ifPresent(inst -> {
                try {
                    inst.addCreatedCourse(Integer.parseInt(id));
                    userDb.saveOrUpdateUser(inst);
                } catch (NumberFormatException ignored) {}
            });
        }

        saveCourses();
        return c;
    }

    public synchronized Course editCourse(String instructorId, String courseId, String newTitle, String newDescription) {
        Course c = coursesById.get(courseId);
        if (c == null) throw new IllegalArgumentException("Course not found");
        if (!Objects.equals(c.getInstructorId(), instructorId)) throw new SecurityException("Only owner may edit");
        c.setTitle(newTitle);
        c.setDescription(newDescription);
        saveCourses();
        return c;
    }

    public synchronized Lesson addLesson(String instructorId, String courseId, String lessonTitle, String lessonContent) {
        Course c = coursesById.get(courseId);
        if (c == null) throw new IllegalArgumentException("Course not found");
        if (!Objects.equals(c.getInstructorId(), instructorId)) throw new SecurityException("Only owner may add lessons");
        String lid = nextLessonId();
        Lesson lesson = new Lesson(lid, lessonTitle, lessonContent, new ArrayList<>());
        c.addLesson(lesson);
        saveCourses();
        return lesson;
    }

    public synchronized Lesson editLesson(String instructorId, String courseId, String lessonId, String newTitle, String newContent) {
        Course c = coursesById.get(courseId);
        if (c == null) throw new IllegalArgumentException("Course not found");
        if (!Objects.equals(c.getInstructorId(), instructorId)) throw new SecurityException("Only owner may edit lessons");
        Lesson l = c.getLessons().stream().filter(x -> Objects.equals(x.getLessonId(), lessonId)).findFirst().orElse(null);
        if (l == null) throw new IllegalArgumentException("Lesson not found");
        l.setTitle(newTitle);
        l.setContent(newContent);
        saveCourses();
        return l;
    }

    public synchronized void deleteLesson(String instructorId, String courseId, String lessonId) {
        Course c = coursesById.get(courseId);
        if (c == null) throw new IllegalArgumentException("Course not found");
        if (!Objects.equals(c.getInstructorId(), instructorId)) throw new SecurityException("Only owner may delete lessons");
        c.removeLesson(lessonId);
        saveCourses();
    }
}
