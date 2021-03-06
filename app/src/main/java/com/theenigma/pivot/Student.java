package com.theenigma.pivot;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Student {
    public static Student student = new Student();
    private String email;
    private String name;
    private String stream;
    private String semester;
    private String regNo;
    private String userType;
    private String buffer;
    private String downloadURL;

    public Student() {
    }
    //OBJECT AND ENTRIES ABSTRACTION
    public List<List<String>> fetchList() {
        List<List<String>> list = new ArrayList<>();

        for (Field f : getClass().getDeclaredFields()) {
            List<String> l = new ArrayList<>();
            l.add(f.getName());
            try { l.add((String) f.get(this)); } catch(Exception e) { l.add("undef"); }
        }
        return list;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStream() {
        return stream;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getBuffer() {
        return buffer;
    }

    public void setBuffer(String buffer) {
        this.buffer = buffer;
    }

    public String getDownloadURL() {
        return downloadURL;
    }

    public void setDownloadURL(String downloadURL) {
        this.downloadURL = downloadURL;
    }
}
