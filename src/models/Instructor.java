/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mizoo
 */
public class Instructor extends User{
    
    private List<String> createdCourses;
    
    public Instructor(int userId, String username, String email, String rawPassword, boolean isAlreadyHashed) {
        super(userId, username, email, rawPassword, ROLE_INSTRUCTOR, isAlreadyHashed);
        //role instructor is already passed
        
        this.createdCourses= new ArrayList<>();
    }
    
    //Managment methods
    public void addCreatedCourse(String courseId) {
        if (Integer.parseInt(courseId) <= 0) {
            throw new IllegalArgumentException("Course ID must be positive.");
        }
        createdCourses.add(courseId);
    }
    
    public void removeCreatedCourse(int courseId) {
        createdCourses.remove(Integer.valueOf(courseId));
    }
    
    public List<String> getCreatedCourses() {
        return createdCourses;
    }

}
