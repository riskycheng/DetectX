//
// Created by chengjian on 4/25/2020.
//
#include <jni.h>
#include <opencv2/dnn.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/highgui.hpp>
#include <string.h>
#include <android/log.h>
#include <fstream>
#include <sstream>

#define  LOG_TAG    "Jian_JNI"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

using namespace cv;
using namespace dnn;

static Net g_DNNet;
//global handle reference

extern "C"
JNIEXPORT void JNICALL
Java_com_fatfish_chengjian_utils_JNIManager_DNN_1initTensorFlowNet(JNIEnv *env, jclass thiz,
                                                                   jstring prototxt_path,
                                                                   jstring caffe_path) {
    LOGI("entering %s", __FUNCTION__);
    const char *modelPath = strdup(env->GetStringUTFChars(prototxt_path, nullptr));
    const char *weightPath = strdup(env->GetStringUTFChars(caffe_path, nullptr));
    LOGI("will try to load tensorflow model from:\n\tmodel path: %s,\n\tweight path: %s",
            modelPath, weightPath);
    //read model
    g_DNNet = readNetFromCaffe(modelPath, weightPath);
    if (g_DNNet.empty()) {
        LOGE("failed to load DNN");
        LOGI("exiting %s", __FUNCTION__);
        return;
    } else {
        LOGI("DNN loaded successfully");
    }
    LOGI("exiting %s", __FUNCTION__);
}