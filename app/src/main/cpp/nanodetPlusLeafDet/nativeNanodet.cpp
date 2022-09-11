//
// Created by Jian Cheng on 2022/8/31.
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
#include "nanodet.h"
#include "classNamesColors.h"
using namespace cv;
#define  LOG_TAG    "Jian_nanoDet_JNI"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define ENABLE_GRABCUT

struct object_rect {
    int x;
    int y;
    int width;
    int height;
};

int resize_uniform(cv::Mat &src, cv::Mat &dst, cv::Size dst_size, object_rect &effect_area) {
    int w = src.cols;
    int h = src.rows;
    int dst_w = dst_size.width;
    int dst_h = dst_size.height;
    //std::cout << "src: (" << h << ", " << w << ")" << std::endl;
    dst = cv::Mat(cv::Size(dst_w, dst_h), CV_8UC3, cv::Scalar(0));

    float ratio_src = w * 1.0 / h;
    float ratio_dst = dst_w * 1.0 / dst_h;

    int tmp_w = 0;
    int tmp_h = 0;
    if (ratio_src > ratio_dst) {
        tmp_w = dst_w;
        tmp_h = floor((dst_w * 1.0 / w) * h);
    } else if (ratio_src < ratio_dst) {
        tmp_h = dst_h;
        tmp_w = floor((dst_h * 1.0 / h) * w);
    } else {
        cv::resize(src, dst, dst_size);
        effect_area.x = 0;
        effect_area.y = 0;
        effect_area.width = dst_w;
        effect_area.height = dst_h;
        return 0;
    }

    cv::Mat tmp;
    cv::resize(src, tmp, cv::Size(tmp_w, tmp_h));

    if (tmp_w != dst_w) {
        int index_w = floor((dst_w - tmp_w) / 2.0);
        //std::cout << "index_w: " << index_w << std::endl;
        for (int i = 0; i < dst_h; i++) {
            memcpy(dst.data + i * dst_w * 3 + index_w * 3, tmp.data + i * tmp_w * 3, tmp_w * 3);
        }
        effect_area.x = index_w;
        effect_area.y = 0;
        effect_area.width = tmp_w;
        effect_area.height = tmp_h;
    } else if (tmp_h != dst_h) {
        int index_h = floor((dst_h - tmp_h) / 2.0);
        //std::cout << "index_h: " << index_h << std::endl;
        memcpy(dst.data + index_h * dst_w * 3, tmp.data, tmp_w * tmp_h * 3);
        effect_area.x = 0;
        effect_area.y = index_h;
        effect_area.width = tmp_w;
        effect_area.height = tmp_h;
    } else {
        printf("error\n");
    }
    return 0;
}


Mat grabCutRes(Mat &src, cv::Rect rect)
{
    cv::Mat mask = cv::Mat::zeros(src.rows, src.cols, CV_8UC1);
    cv::Mat bgModel = cv::Mat::zeros(1, 65, CV_64FC1);
    cv::Mat fgModel = cv::Mat::zeros(1, 65, CV_64FC1);

    cv::grabCut(src, mask, rect, bgModel, fgModel, 4, cv::GC_INIT_WITH_RECT);
    cv::Mat mask2 = (mask == 1) + (mask == 3);  // 0 = cv::GC_BGD, 1 = cv::GC_FGD, 2 = cv::PR_BGD, 3 = cv::GC_PR_FGD
    cv::Mat dest;
    src.copyTo(dest, mask2);
    return dest;
}

void draw_bboxes(cv::Mat &image, const std::vector<BoxInfo> &bboxes, object_rect effect_roi) {
    int src_w = image.cols;
    int src_h = image.rows;
    int dst_w = effect_roi.width;
    int dst_h = effect_roi.height;
    float width_ratio = (float) src_w / (float) dst_w;
    float height_ratio = (float) src_h / (float) dst_h;

    for (const auto & box : bboxes) {
        cv::Scalar color = cv::Scalar(color_list[box.label][0], color_list[box.label][1],
                                      color_list[box.label][2]);
        cv::Rect rect = cv::Rect(cv::Point(int((box.x1 - (float)effect_roi.x) * width_ratio),
                                           int((box.y1 - (float)effect_roi.y) * height_ratio)),
                                 cv::Point(int((box.x2 - (float)effect_roi.x) * width_ratio),
                                           int((box.y2 - (float)effect_roi.y) * height_ratio)));
       float scaleFactor = 1.2f;
       int newWidth = rect.width * scaleFactor;
       int newHeight = rect.height * scaleFactor;
       int centerX = rect.x + rect.width / 2;
       int centerY = rect.y + rect.height / 2;
       int newX = max(0, (centerX - newWidth / 2));
       int newY = min(image.rows - 1, (centerY - newHeight / 2));
       // assign back
       rect.x = min(image.cols - 1, max(0, newX));
       rect.y = min(image.rows - 1, max(0, newY));
       rect.width = min(image.cols - 1, max(0, newWidth));
       rect.height = min(image.rows - 1, max(0, newHeight));
#ifdef ENABLE_GRABCUT
        if (box.label == 1) {
            auto tmpMat = image(rect); // the cropped ROI for segmentation
            cvtColor(tmpMat, tmpMat, COLOR_RGBA2BGR);
            auto tmpMatSeg = grabCutRes(tmpMat, cv::Rect(0, 0, tmpMat.cols - 1, tmpMat.rows - 1));
            //small_image.copyTo(big_image(cv::Rect(x,y,small_image.cols, small_image.rows)));
            cvtColor(tmpMatSeg, tmpMatSeg, COLOR_BGR2RGBA);
            tmpMatSeg.copyTo(image(rect));
        }
#endif
        cv::rectangle(image, rect, color);

        char text[256];
        sprintf(text, "%s %.1f%%", class_names[box.label], box.score * 100);

        int baseLine = 0;
        cv::Size label_size = cv::getTextSize(text, cv::FONT_HERSHEY_SIMPLEX, 0.4, 1, &baseLine);

        int x = (int)((box.x1 - (float)effect_roi.x) * width_ratio);
        int y = (int)((box.y1 - (float)effect_roi.y) * height_ratio) - label_size.height - baseLine;
        if (y < 0)
            y = 0;
        if (x + label_size.width > image.cols)
            x = image.cols - label_size.width;

        cv::putText(image, text, cv::Point(x, y + label_size.height),
                    cv::FONT_HERSHEY_SIMPLEX, 0.4, color);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_fatfish_chengjian_utils_JNIManager_nanoDetLeaf_1Init(JNIEnv *env, jobject thiz,
                                                          jstring modelParamPath,
                                                          jstring modelBinPath) {

    LOGI("entering %s", __FUNCTION__);
    const char *paramPath = strdup(env->GetStringUTFChars(modelParamPath, nullptr));
    const char *binPath = strdup(env->GetStringUTFChars(modelBinPath, nullptr));
    LOGI("loading from %s, %s", paramPath, binPath);
    NanoDet::detector = new NanoDet(paramPath, binPath, false);
    LOGI("exiting %s", __FUNCTION__);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_fatfish_chengjian_utils_JNIManager_nanoDetLeaf_1Detect(JNIEnv *env, jobject thiz,
                                                            jobject inputBitmap) {
    LOGI("entering %s", __FUNCTION__);
    uint32_t *_inputBitmap;
    AndroidBitmapInfo bmapInfo;
    AndroidBitmap_getInfo(env, inputBitmap, &bmapInfo);
    AndroidBitmap_lockPixels(env, inputBitmap, (void **) &_inputBitmap);
    auto *imagePtr = (uint8_t *) _inputBitmap;

    //get image info
    auto width = bmapInfo.width;
    auto height = bmapInfo.height;

    int model_height = NanoDet::detector->input_size[0];
    int model_width = NanoDet::detector->input_size[1];

    Mat image = Mat((int)height, (int)width, CV_8UC4);
    image.data = imagePtr;

    Mat tmpMat;
    cvtColor(image, tmpMat, COLOR_RGBA2BGR);

    object_rect effect_roi{};
    cv::Mat resized_img;
    resize_uniform(tmpMat, resized_img, cv::Size(model_width, model_height), effect_roi);
    auto results = NanoDet::detector->detect(resized_img, 0.4, 0.5);
    draw_bboxes(image, results, effect_roi);
    AndroidBitmap_unlockPixels(env, inputBitmap);
    tmpMat.release();
    LOGI("exiting %s", __FUNCTION__);
}