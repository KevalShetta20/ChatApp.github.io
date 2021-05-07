package com.kevalshetta.ChatApp;

public class Contacts {
    public String fullname, status, profileimage;

    public Contacts() {
    }

    public Contacts(String fullname, String status, String profileimage, String currentuserid) {
        this.fullname = fullname;
        this.status = status;
        this.profileimage = profileimage;
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
}
