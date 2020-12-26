package com.example.headspace;

class Meditation {

    private String mCategory;
    private String mDay;
    private int mVideoId;
    private int mResourceId;
    private boolean hasVideo;
    private boolean mCompleted;

    Meditation(String category, String day, int resourceId){
        mCategory = category;
        mDay = day;
        mResourceId = resourceId;
        hasVideo = false;
        mCompleted = false;
    }
    Meditation(String category, String day, int resourceId, int videoId){
        mCategory = category;
        mDay = day;
        mResourceId = resourceId;
        mVideoId = videoId;
        hasVideo = true;
        mCompleted = false;
    }


    int getResourceId() {
        return mResourceId;
    }

    String getCategory() {
        return mCategory;
    }

    String getDay() {
        return mDay;
    }

    boolean isCompleted() {
        return mCompleted;
    }

    int getVideoId() {
        return mVideoId;
    }

    void setCompleted() {
        mCompleted = true;
    }


    boolean hasVideo() {
        return hasVideo;
    }
}
