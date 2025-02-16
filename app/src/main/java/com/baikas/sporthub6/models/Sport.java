package com.baikas.sporthub6.models;

public class Sport {
    String englishName;
    String greekName;
    String greekNameGenitive;

    int sportImageId;
    int sportColor;

    public Sport(String englishName, String greekName,String greekNameGenitive, int sportImageId, int sportColor) {
        this.englishName = englishName;
        this.greekName = greekName;
        this.greekNameGenitive = greekNameGenitive;
        this.sportImageId = sportImageId;
        this.sportColor = sportColor;
    }


    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    public String getGreekNameGenitive() {
        return greekNameGenitive;
    }

    public void setGreekNameGenitive(String greekNameGenitive) {
        this.greekNameGenitive = greekNameGenitive;
    }

    public String getGreekName() {
        return greekName;
    }

    public void setGreekName(String greekName) {
        this.greekName = greekName;
    }

    public int getSportImageId() {
        return sportImageId;
    }

    public void setSportImageId(int sportImageId) {
        this.sportImageId = sportImageId;
    }

    public int getSportColor() {
        return sportColor;
    }

    public void setSportColor(int sportColor) {
        this.sportColor = sportColor;
    }
}
