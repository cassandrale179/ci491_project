package com.example.caregiver.model;

public class User {

    public String name;
    public String email;
    public String notes;
    public String role;
    public String id;

    public User(String id, String name){
        this.id = id;
        this.name = name;
    }

    public User(String name, String email, String role){
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public User(String id, String name, String email, String role,  String notes){
        this.name = name;
        this.email = email;
        this.notes = notes;
        this.role = role;
        this.id = id;
    }
}

