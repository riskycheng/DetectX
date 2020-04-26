package com.fatfish.chengjian.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.Image;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Script;
import android.renderscript.Type;
import android.util.Log;

import androidx.camera.core.ImageProxy;

import com.jian.yuv_utils.ScriptC_yuv420888;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Vector;

/**
 * description: Element.RGB_888 and Element.RGBA_8888 allocate the same memory, 4 bytes aligned?
 * So theoretically does not support conversion to RGB
 */
public class LocalUtils {
    private static final String TAG = "Jian_Java_" + LocalUtils.class.getSimpleName();

    public static Bitmap YUV_420_888_toRGB(Context mContext, Image image, int width, int height) {
        Log.d(TAG, "YUV_420_888_toRGB enter");
        // Get the three image planes
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        byte[] y = new byte[buffer.remaining()];
        buffer.get(y);

        buffer = planes[1].getBuffer();
        byte[] u = new byte[buffer.remaining()];
        buffer.get(u);

        buffer = planes[2].getBuffer();
        byte[] v = new byte[buffer.remaining()];
        buffer.get(v);

        // get the relevant RowStrides and PixelStrides
        // (we know from documentation that PixelStride is 1 for y)
        int yRowStride = planes[0].getRowStride();
        int uvRowStride = planes[1].getRowStride();  // we know from   documentation that RowStride is the same for u and v.
        int uvPixelStride = planes[1].getPixelStride();  // we know from   documentation that PixelStride is the same for u and v.


        // rs creation just for demo. Create rs just once in onCreate and use it again.
        RenderScript rs = RenderScript.create(mContext);
        //RenderScript rs = MainActivity.rs;
        ScriptC_yuv420888 mYuv420 = new ScriptC_yuv420888(rs);

        // Y,U,V are defined as global allocations, the out-Allocation is the Bitmap.
        // Note also that uAlloc and vAlloc are 1-dimensional while yAlloc is 2-dimensional.
        Type.Builder typeUcharY = new Type.Builder(rs, Element.U8(rs));
        typeUcharY.setX(yRowStride).setY(height);
        Allocation yAlloc = Allocation.createTyped(rs, typeUcharY.create());
        yAlloc.copyFrom(y);
        mYuv420.set_ypsIn(yAlloc);

        Type.Builder typeUcharUV = new Type.Builder(rs, Element.U8(rs));
        // note that the size of the u's and v's are as follows:
        //      (  (width/2)*PixelStride + padding  ) * (height/2)
        // =    (RowStride                          ) * (height/2)
        // but I noted that on the S7 it is 1 less...
        typeUcharUV.setX(u.length);
        Allocation uAlloc = Allocation.createTyped(rs, typeUcharUV.create());
        uAlloc.copyFrom(u);
        mYuv420.set_uIn(uAlloc);

        Allocation vAlloc = Allocation.createTyped(rs, typeUcharUV.create());
        vAlloc.copyFrom(v);
        mYuv420.set_vIn(vAlloc);

        // handover parameters
        mYuv420.set_picWidth(width);
        mYuv420.set_uvRowStride(uvRowStride);
        mYuv420.set_uvPixelStride(uvPixelStride);

        Bitmap outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Allocation outAlloc = Allocation.createFromBitmap(rs, outBitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);

        Script.LaunchOptions lo = new Script.LaunchOptions();
        lo.setX(0, width);  // by this we ignore the y’s padding zone, i.e. the right side of x between width and yRowStride
        lo.setY(0, height);

        mYuv420.forEach_doConvert(outAlloc, lo);
        outAlloc.copyTo(outBitmap);
        Log.d(TAG, "YUV_420_888_toRGB exit");
        return outBitmap;
    }


    public static Bitmap YUV_420_888_toRGB(Context mContext, ImageProxy image, int width, int height) {
        Log.d(TAG, "YUV_420_888_toRGB enter");
        // Get the three image planes
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        byte[] y = new byte[buffer.remaining()];
        buffer.get(y);

        buffer = planes[1].getBuffer();
        byte[] u = new byte[buffer.remaining()];
        buffer.get(u);

        buffer = planes[2].getBuffer();
        byte[] v = new byte[buffer.remaining()];
        buffer.get(v);

        // get the relevant RowStrides and PixelStrides
        // (we know from documentation that PixelStride is 1 for y)
        int yRowStride = planes[0].getRowStride();
        int uvRowStride = planes[1].getRowStride();  // we know from   documentation that RowStride is the same for u and v.
        int uvPixelStride = planes[1].getPixelStride();  // we know from   documentation that PixelStride is the same for u and v.


        // rs creation just for demo. Create rs just once in onCreate and use it again.
        RenderScript rs = RenderScript.create(mContext);
        //RenderScript rs = MainActivity.rs;
        ScriptC_yuv420888 mYuv420 = new ScriptC_yuv420888(rs);

        // Y,U,V are defined as global allocations, the out-Allocation is the Bitmap.
        // Note also that uAlloc and vAlloc are 1-dimensional while yAlloc is 2-dimensional.
        Type.Builder typeUcharY = new Type.Builder(rs, Element.U8(rs));
        typeUcharY.setX(yRowStride).setY(height);
        Allocation yAlloc = Allocation.createTyped(rs, typeUcharY.create());
        yAlloc.copyFrom(y);
        mYuv420.set_ypsIn(yAlloc);

        Type.Builder typeUcharUV = new Type.Builder(rs, Element.U8(rs));
        // note that the size of the u's and v's are as follows:
        //      (  (width/2)*PixelStride + padding  ) * (height/2)
        // =    (RowStride                          ) * (height/2)
        // but I noted that on the S7 it is 1 less...
        typeUcharUV.setX(u.length);
        Allocation uAlloc = Allocation.createTyped(rs, typeUcharUV.create());
        uAlloc.copyFrom(u);
        mYuv420.set_uIn(uAlloc);

        Allocation vAlloc = Allocation.createTyped(rs, typeUcharUV.create());
        vAlloc.copyFrom(v);
        mYuv420.set_vIn(vAlloc);

        // handover parameters
        mYuv420.set_picWidth(width);
        mYuv420.set_uvRowStride(uvRowStride);
        mYuv420.set_uvPixelStride(uvPixelStride);

        Bitmap outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Allocation outAlloc = Allocation.createFromBitmap(rs, outBitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);

        Script.LaunchOptions lo = new Script.LaunchOptions();
        lo.setX(0, width);  // by this we ignore the y’s padding zone, i.e. the right side of x between width and yRowStride
        lo.setY(0, height);

        mYuv420.forEach_doConvert(outAlloc, lo);
        outAlloc.copyTo(outBitmap);
        Log.d(TAG, "YUV_420_888_toRGB exit");
        return outBitmap;
    }

    public static Bitmap rotateBitmap(Bitmap origin, float alpha) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(alpha);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }

    public static Bitmap resizeImage(Bitmap bitmap, int w, int h) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scaleWidth = ((float) w) / width;
        float scaleHeight = ((float) h) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, width,
                height, matrix, true);
    }


    public static Bitmap centralCropAndResizeImage(Bitmap bitmap, int dstWidth, int dstHeight, boolean reachBoarder, boolean keepAspectRatio) {
        int srcWidth = bitmap.getWidth();
        int srcHeight = bitmap.getHeight();

        if (!keepAspectRatio) {
            return resizeImage(bitmap, dstWidth, dstHeight);
        }

        float dstAspectRatio = dstWidth * 1.0f / dstHeight;
        float ratio_width = 1.0f;
        float ratio_height = 1.0f;
        int dstWidthInOrg = 0;
        int dstHeightInOrg = 0;

        if (dstWidth >= dstHeight) {
            if (!reachBoarder)
                dstWidthInOrg = dstWidth > srcWidth ? srcWidth : dstWidth;
            else
                dstWidthInOrg = srcWidth;
            dstHeightInOrg = (int) (dstWidthInOrg / dstAspectRatio);
        } else {
            if (!reachBoarder)
                dstHeightInOrg = dstHeight > srcHeight ? srcHeight : dstHeight;
            else
                dstHeightInOrg = srcHeight;
            dstWidthInOrg = (int) (dstHeightInOrg * dstAspectRatio);
        }
        ratio_width = dstWidth * 1.0f / dstWidthInOrg;
        ratio_height = dstHeight * 1.0f / dstHeightInOrg;

        //cal the starting point with center point as the anchor
        int startingX = (srcWidth - dstWidthInOrg) / 2;
        int startingY = (srcHeight - dstHeightInOrg) / 2;

        //create the matrix
        Matrix matrix = new Matrix();
        matrix.postScale(ratio_width, ratio_height);

        return Bitmap.createBitmap(bitmap, startingX, startingY, dstWidthInOrg, dstHeightInOrg, matrix, true);
    }


    public static void saveBitmap(Bitmap bitmap, String savedPath) {
        File f = new File(savedPath);
        if (f.exists()) {
            boolean result = f.delete();
            if (result)
                Log.d(TAG, "old image deleted");
            else
                Log.d(TAG, "savedPath not found, will save it here");
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static Boolean CopyAssetsFile(Context context, String filename, String des) {
        Boolean isSuccess = true;
        AssetManager assetManager = context.getAssets();
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(filename);
            String newFileName = des + "/" + filename;
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            e.printStackTrace();
            isSuccess = false;
        }
        return isSuccess;
    }


    public static Boolean CopyAssetsDir(Context context, String src, String des) {
        Boolean isSuccess = true;
        String[] files;
        try {
            files = context.getResources().getAssets().list(src);
        } catch (IOException e1) {
            return false;
        }

        if (files.length == 0) {
            isSuccess = CopyAssetsFile(context, src, des);
            if (!isSuccess)
                return isSuccess;
        } else {
            File srcfile = new File(des + "/" + src);
            if (!srcfile.exists()) {
                if (srcfile.mkdir()) {//对于目录自行创建
                    for (int i = 0; i < files.length; i++) {
                        isSuccess = CopyAssetsDir(context, src + "/" + files[i], des);
                        if (!isSuccess)
                            return isSuccess;
                    }
                } else {
                    return false;
                }
            }

        }
        return isSuccess;
    }

    public static boolean checkFileExist(String filePath) {
        try {
            File file = new File(filePath);
            return file.exists();
        } catch (Exception e) {
            return false;
        }
    }


}