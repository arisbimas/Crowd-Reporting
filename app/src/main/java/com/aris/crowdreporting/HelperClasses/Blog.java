package com.aris.crowdreporting.HelperClasses;


import java.util.Date;

public class Blog  extends BlogPostId{

    public String user_id;


    public String image_uri;
    public String desc;
    public String image_thumb;
    public String title;
    public String address;
    public Date timestamp;

    public Blog() {
    }

    public Blog(String user_id, String image_uri, String desc, String image_thumb, Date timestamp, String title, String address) {
        this.user_id = user_id;
        this.image_uri = image_uri;
        this.desc = desc;
        this.image_thumb = image_thumb;
        this.timestamp = timestamp;
        this.title = title;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage_uri() {
        return image_uri;
    }

    public void setImage_uri(String image_uri) {
        this.image_uri = image_uri;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImage_thumb() {
        return image_thumb;
    }

    public void setImage_thumb(String image_thumb) {
        this.image_thumb = image_thumb;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}