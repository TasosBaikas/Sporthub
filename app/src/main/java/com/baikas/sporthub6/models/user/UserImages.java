package com.baikas.sporthub6.models.user;

import androidx.annotation.NonNull;

import com.baikas.sporthub6.models.PhotoDetails;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class UserImages {

    private String userId;
    private List<PhotoDetails> photoDetails;
    public static final int USER_IMAGES_MAX = 20;

    public UserImages(String userId, List<PhotoDetails> photoDetails) {
        this.userId = userId;
        this.photoDetails = photoDetails;
    }

    public UserImages(Map<String,Object> map) {
        this.userId = (String) map.get("userId");

        List<Map<String, Object>> photoDetailsList = (List<Map<String, Object>>) map.get("photoDetails");

        this.photoDetails = photoDetailsList.stream()
                .map((photoDetails) -> new PhotoDetails(photoDetails))
                .collect(Collectors.toList());
    }

    public UserImages(@NonNull UserImages otherUserImages){
        this.userId = otherUserImages.getUserId();

        this.photoDetails = otherUserImages.getPhotoDetails().stream()
                .map((photoDetails) -> new PhotoDetails(photoDetails))
                .collect(Collectors.toList());
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<PhotoDetails> getPhotoDetails() {
        return photoDetails;
    }

    public void setPhotoDetails(List<PhotoDetails> photoDetails) {
        this.photoDetails = photoDetails;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserImages that = (UserImages) o;

        if (!Objects.equals(userId, that.userId)) return false;
        return Objects.equals(photoDetails, that.photoDetails);
    }

    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + (photoDetails != null ? photoDetails.hashCode() : 0);
        return result;
    }
}

