package com.example.seg2105termproject;

public class Course {

    private String courseName;
    private String courseCode;

    public String getCourseName(){
        return this.courseName;
    }

    public String getCourseCode(){
        return this.courseCode;
    }

    public void setCourseName(String name){
        this.courseName = name;
    }

    public void setCourseCode(String code){
        this.courseCode = code;
    }
}
