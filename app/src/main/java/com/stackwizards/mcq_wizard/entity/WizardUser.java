package com.stackwizards.mcq_wizard.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WizardUser {
    String username;
    String email;
    String bio;
    int role = 0;
    int age;

    ArrayList<Questionaire> questionaires;

    Map<String, Integer> notes ;

    public WizardUser() {
        notes = new HashMap<>();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public ArrayList<Questionaire> getQuestionaires() {
        return questionaires;
    }

    public void setQuestionaires(ArrayList<Questionaire> questionaires) {
        this.questionaires = questionaires;
    }

    public Map<String, Integer> getNotes() {
        return notes;
    }

    public void setNotes(Map<String, Integer> notes) {
        this.notes = notes;
    }
}
