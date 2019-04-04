package com.aris.crowdreporting;

import java.util.Date;

public class Near extends NearPostId{

    public String user_id;
    public String image_url;
    public String desc;
    public String longitude;
    public String latitude;
    public Date timestamp;

    public String getImage_thumb() {
        return image_thumb;
    }

    public void setImage_thumb(String image_thumb) {
        this.image_thumb = image_thumb;
    }

    public String image_thumb;

    public Near(){

    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Near(String user_id, String image_url, String desc, String longitude, String latitude, Date timestamp, String image_thumb) {
        this.user_id = user_id;
        this.image_url = image_url;
        this.desc = desc;
        this.longitude = longitude;
        this.latitude = latitude;
        this.timestamp = timestamp;
        this.image_thumb = image_thumb;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }


}
