package com.aris.crowdreporting.HelperClasses;

import java.util.Date;

public class Chatlist {

    public String id;
    public Date timestamp;

    public Chatlist() {
    }

    public Chatlist(String id, Date timestamp) {
        this.id = id;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
