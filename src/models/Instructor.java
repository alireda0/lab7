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
    
    private List<Integer> createdCourses;
    
    public Instructor(int userId, String username, String email, String rawPassword) {
        super(userId, username, email, rawPassword, ROLE_INSTRUCTOR);
        //role instructor is already passed
        
        this.createdCourses= new ArrayList<>();
    }
    
    //Managment methods
    public void addCreatedCourse(int courseId) {
        if (courseId <= 0) {
            throw new IllegalArgumentException("Course ID must be positive.");
        }
        createdCourses.add(courseId);
    }
    
    public void removeCreatedCourse(int courseId) {
        createdCourses.remove(Integer.valueOf(courseId));
    }
    
    public List<Integer> getCreatedCourses() {
        return createdCourses;
    }

}
