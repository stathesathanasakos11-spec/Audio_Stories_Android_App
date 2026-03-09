package com.myprojects.unipiaudiostories.models;

public class User {
    private String username;
    private String email;


    //κενός constructor για τη Firebase
    public User() {
    }


    //constructor της model class User
    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }


    //getters & setters
    public String getUsername() {
        return username;
    }
    public String getEmail() {
        return email;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setEmail(String email) {
        this.email = email;
    }
}
