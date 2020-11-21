package com.example.bookgraph;

/**
 * This class is a model for comments
 */
public class Comments {

    public String comments,date,time,username;

    public Comments(){

    }

    public Comments(String comments, String date, String time, String username) {
        this.comments = comments;
        this.date = date;
        this.time = time;
        this.username = username;
    }

    /**
     * Getter for the comment
     * @return
     */
    public String getComments() {
        return comments;
    }

    /**
     * Setter for the comment
     * @param comments
     */
    public void setComments(String comments) {
        this.comments = comments;
    }

    /**
     * Getter for the date
     * @return
     */
    public String getDate() {
        return date;
    }

    /**
     * Setter for the date
     * @param date
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Getter for the time
     * @return
     */
    public String getTime() {
        return time;
    }

    /**
     * Setter for the time
     * @param time
     */
    public void setTime(String time) {
        this.time = time;
    }

    /**
     * Getter for the username
     * @return
     */
    public String getUsername() {
        return username;
    }

    /**
     * Setter for the username
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
    }
}
