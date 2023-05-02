package com.fatfish.chengjian.utils;

import android.graphics.Bitmap;

import java.util.Vector;

public class DoorDetectionLogInstance {
    private String timeStamp;
    private String imagePath;
    private Bitmap bitmap;
    private BoxInfo[] doors;
    private boolean anyDoorOpen;

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public BoxInfo[] getDoors() {
        return doors;
    }

    public void setDoors(BoxInfo[] doors) {
        this.doors = doors;
    }

    public boolean isAnyDoorOpen() {
        return anyDoorOpen;
    }

    public void setAnyDoorOpen(boolean anyDoorOpen) {
        this.anyDoorOpen = anyDoorOpen;
    }


    @Override
    public String toString() {

        StringBuilder doorsContent = new StringBuilder();
        for (BoxInfo door : this.doors)
            doorsContent.append(door.toString());

        return "DoorDetectionLogInstance{" +
                "timeStamp='" + timeStamp + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", doors='" + doorsContent + '\'' +
                ", anyDoorOpen=" + anyDoorOpen +
                '}';
    }
}
