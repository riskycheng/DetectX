# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10)

set(OPENCV_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../opencv/)
include_directories(${OPENCV_DIR}/include/)
add_library(opencv4 SHARED IMPORTED)
set_target_properties(
        opencv4
        PROPERTIES
        IMPORTED_LOCATION ${OPENCV_DIR}/${CMAKE_ANDROID_ARCH_ABI}/libopencv_java4.so)


set(ncnn_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../ncnn-20220420-android-vulkan/${CMAKE_ANDROID_ARCH_ABI}/lib/cmake/ncnn)
find_package(ncnn REQUIRED)


add_library( # Sets the name of the library.
        nanodet
        SHARED
        nativeNanodet.cpp
        nanodet.cpp)



# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# 支持操作Bitmap的库
find_library( # Sets the name of the path variable.
        jnigraphics-lib
        # Specifies the name of the NDK library that
        # you want CMake to locate.
        jnigraphics)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries(nanodet opencv4 ncnn ${jnigraphics-lib} ${log-lib})