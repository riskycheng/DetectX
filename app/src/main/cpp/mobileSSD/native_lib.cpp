//
// Created by chengjian on 4/25/2020.
//
#include <jni.h>
#include "../opencv/include/opencv2/imgproc.hpp"
#include "../opencv/include/opencv2/highgui.hpp"
#include "../opencv/include/opencv2/core/mat.hpp"
#include "../opencv/include/opencv2/dnn.hpp"
#include <android/log.h>
#include <fstream>
#include <sstream>
#include <android/bitmap.h>


#define  LOG_TAG    "Jian_JNI"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

using namespace cv;
using namespace dnn;

int IN_WIDTH = 300;
int IN_HEIGHT = 300;
float WH_RATIO = (float) IN_WIDTH / IN_HEIGHT;
double IN_SCALE_FACTOR = 0.007843;
double MEAN_VAL = 127.5;
double THRESHOLD = 0.2;

const char *mobileSSDClasses[] =
        {"background", "aeroplane", "bicycle", "bird", "boat", "bottle", "bus", "car", "cat",
         "chair", "cow", "diningtable", "dog", "horse", "motorbike", "person", "pottedplant",
         "sheep", "sofa", "train", "tvmonitor"};

static Net g_DNNet;
//global handle reference

extern "C"
JNIEXPORT void JNICALL
Java_com_fatfish_chengjian_utils_JNIManager_DNN_1initCaffeNet(JNIEnv *env, jobject thiz,
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
    g_DNNet.setPreferableTarget(DNN_TARGET_OPENCL_FP16); // it actually does not work for android
    LOGI("exiting %s", __FUNCTION__);
}


extern "C"
JNIEXPORT void JNICALL
Java_com_fatfish_chengjian_utils_JNIManager_DNN_1execute(JNIEnv *env, jobject clazz,
                                                         jobject inputBitmap) {
    LOGI("entering %s", __FUNCTION__);
    uint32_t *_inputBitmap;
    AndroidBitmapInfo bmapInfo;
    AndroidBitmap_getInfo(env, inputBitmap, &bmapInfo);
    AndroidBitmap_lockPixels(env, inputBitmap, (void **) &_inputBitmap);
    auto *image = (uint8_t *) _inputBitmap;

    //get image info
    int width = bmapInfo.width;
    int height = bmapInfo.height;
    int format = bmapInfo.format;

    Mat inputMat_ = Mat(height, width, CV_8UC4);
    inputMat_.data = image;

    Mat inputMat;
    cvtColor(inputMat_, inputMat, COLOR_BGRA2RGB);

    //forward image through network
    Mat blob = blobFromImage(inputMat, 1.0 / 127.5, Size(IN_WIDTH, IN_HEIGHT),
                             Scalar(MEAN_VAL, MEAN_VAL, MEAN_VAL), false, false);

    g_DNNet.setInput(blob, "data");
    Mat detections = g_DNNet.forward("detection_out");

    Mat detectionMat(detections.size[2], detections.size[3], CV_32F, detections.ptr<float>());

    for (int i = 0; i < detectionMat.rows; i++) {

        float confidence = detectionMat.at<float>(i, 2);
        if (confidence > THRESHOLD) {
            size_t objectClass = (size_t) (detectionMat.at<float>(i, 1));
            LOGI("new detections[%d].conf=%f, class=%s", i, confidence,
                 mobileSSDClasses[objectClass]);
            //coordinates
            int topLeft_x = static_cast<int>(detectionMat.at<float>(i, 3) * width);
            int topLeft_y = static_cast<int>(detectionMat.at<float>(i, 4) * height);
            int bottomLeft_x = static_cast<int>(detectionMat.at<float>(i, 5) * width);
            int bottomLeft_y = static_cast<int>(detectionMat.at<float>(i, 6) * height);
            rectangle(inputMat_,
                      Rect(Point(topLeft_x, topLeft_y), Point(bottomLeft_x, bottomLeft_y)),
                      Scalar(0, 255, 255), 2);
            putText(inputMat_, mobileSSDClasses[objectClass], Point(topLeft_x, topLeft_y), 1, 1.8,
                    Scalar(255, 0, 0), 2);
        }
    }
    AndroidBitmap_unlockPixels(env, inputBitmap);
    inputMat.release();
    LOGI("exiting %s", __FUNCTION__);
}