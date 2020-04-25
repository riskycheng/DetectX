package com.fatfish.chengjian.utils;

public class JNIManager {
    static {
        System.loadLibrary("native-lib");
    }

    public static native void init();
}
