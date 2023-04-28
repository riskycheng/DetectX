package com.fatfish.chengjian.utils;

import androidx.annotation.NonNull;

public class BoxInfo implements Cloneable{
    private int x;
    private int y;
    private int width;
    private int height;
    private int label;
    private float confidence;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    @NonNull
    @Override
    protected Object clone() throws CloneNotSupportedException {
        BoxInfo clone = (BoxInfo) super.clone();
        clone.setX(this.x);
        clone.setY(this.y);
        clone.setWidth(this.width);
        clone.setHeight(this.height);
        clone.setLabel(this.label);
        clone.setConfidence(this.confidence);
        return clone;
    }
}
