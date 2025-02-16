package com.baikas.sporthub6.models;

import com.baikas.sporthub6.interfaces.ObjectWithId;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.Map;

public class PhotoDetails implements ObjectWithId, Serializable {

    private String filePath;
    private String photoDownloadUrl;
    private long priority;
    private long createdAtUTC;

    public PhotoDetails() {
    }


    public PhotoDetails(String filePath, String photoDownloadUrl, long priority, long createdAtUTC) {
        this.filePath = filePath;
        this.photoDownloadUrl = photoDownloadUrl;
        this.priority = priority;
        this.createdAtUTC = createdAtUTC;
    }

    public PhotoDetails(Map<String,Object> map) {
        this.filePath = (String) map.get("filePath");
        this.photoDownloadUrl = (String) map.get("photoDownloadUrl");

        if (map.get("priority") instanceof Integer){
            this.priority = ((Integer) map.get("priority")).longValue();
        }else
            this.priority = (Long) map.get("priority");

        if (map.get("createdAtUTC") instanceof Integer){
            this.createdAtUTC = ((Integer) map.get("createdAtUTC")).longValue();
        }else
            this.createdAtUTC = (Long) map.get("createdAtUTC");

    }

    public PhotoDetails(PhotoDetails photoDetails) {
        this.filePath = photoDetails.getFilePath();
        this.photoDownloadUrl = photoDetails.getPhotoDownloadUrl();

        this.priority = photoDetails.getPriority();
        this.createdAtUTC = photoDetails.getCreatedAtUTC();
    }

    public String getPhotoDownloadUrl() {
        return photoDownloadUrl;
    }

    public void setPhotoDownloadUrl(String photoDownloadUrl) {
        this.photoDownloadUrl = photoDownloadUrl;
    }

    public long getPriority() {
        return priority;
    }

    public void setPriority(long priority) {
        this.priority = priority;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getCreatedAtUTC() {
        return createdAtUTC;
    }

    public void setCreatedAtUTC(long createdAtUTC) {
        this.createdAtUTC = createdAtUTC;
    }

    @Override
    @Exclude
    public String getId() {
        return photoDownloadUrl;
    }
}
