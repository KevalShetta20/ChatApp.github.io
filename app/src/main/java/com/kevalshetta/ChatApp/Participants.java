package com.kevalshetta.ChatApp;

public class Participants {
    public String fullname, status, profileimage, currentuserid;

    public Participants() {
    }

    public Participants(String fullname, String status, String profileimage, String currentuserid) {
        this.fullname = fullname;
        this.status = status;
        this.profileimage = profileimage;
        this.currentuserid = currentuserid;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getCurrentuserid() {
        return currentuserid;
    }

    public void setCurrentuserid(String currentuserid) {
        this.currentuserid = currentuserid;
    }
}
