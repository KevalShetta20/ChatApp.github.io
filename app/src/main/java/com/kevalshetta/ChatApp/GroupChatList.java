package com.kevalshetta.ChatApp;

public class GroupChatList {
    String groupid, grouptitle, groupdescription, groupicon, timestamp, createdBy;

    public GroupChatList() {
    }

    public GroupChatList(String groupid, String grouptitle, String groupdescription, String groupicon, String timestamp, String createdBy) {
        this.groupid = groupid;
        this.grouptitle = grouptitle;
        this.groupdescription = groupdescription;
        this.groupicon = groupicon;
        this.timestamp = timestamp;
        this.createdBy = createdBy;
    }

    public String getGroupid() {
        return groupid;
    }

    public void setGroupid(String groupid) {
        this.groupid = groupid;
    }

    public String getGrouptitle() {
        return grouptitle;
    }

    public void setGrouptitle(String grouptitle) {
        this.grouptitle = grouptitle;
    }

    public String getGroupdescription() {
        return groupdescription;
    }

    public void setGroupdescription(String groupdescription) {
        this.groupdescription = groupdescription;
    }

    public String getGroupicon() {
        return groupicon;
    }

    public void setGroupicon(String groupicon) {
        this.groupicon = groupicon;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
