package com.example.hp.vguide;

class User {
    private String displayName;


    private String Email;
    private long createdAt;

    public User (){};
    public User(String displayName,String email,long createdAt){
        this.displayName=displayName;
        this.Email=email;
        this.createdAt=createdAt;
    }


    public String getDisplayname() {
        return displayName;
    }

    public String getEmail() {
        return Email;
    }

    public long getCreatedAt() {
        return createdAt;
    }

}

