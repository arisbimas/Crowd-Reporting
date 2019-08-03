package com.aris.crowdreporting.HelperClasses;

public class User {

    public String image;
    public String name;
    public String user_id;
    public String status;

    public  User(){

    }

    public User(String image, String name, String user_id, String status) {
        this.image = image;
        this.name = name;
        this.user_id = user_id;
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
