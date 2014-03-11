package com.jesson.android.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by zhangdi on 14-2-19.
 */
public class ImageUtils {

    private static final long MAX_MEMORY_SIZE = 720 * 1028 * 4;

    private static final long MAX_WIDTH = 2048;

    private static final int MAX_HEIGHT = 2048;

    /**
     * convert Bitmap to byte array
     */
    public static byte[] bitmap2Byte(Bitmap b) {
        if (b == null) {
            return null;
        }
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, o);
        return o.toByteArray();
    }

    /**
     * 从指定路径加载制定宽高的图片
     *
     * @param path
     * @param width
     * @param height
     * @return
     */
    public static Bitmap loadScaledBitmap(String path, int width, int height) {
        if (width <= 0 || height <= 0 || TextUtils.isEmpty(path) || !(new File(path).exists())) {
            return null;
        }

        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bmp = BitmapFactory.decodeFile(path, opts);
        if (bmp != null) {
            Bitmap destBmp = Bitmap.createScaledBitmap(bmp, width, height, false);
            if (!(bmp.getWidth() == destBmp.getWidth() && bmp.getHeight() == destBmp.getHeight())) {
                bmp.recycle();
                bmp = null;
            }
            return destBmp;
        }
        return null;
    }

    /**
     * 保存图片到本地
     *
     * @param bitmap
     * @param path
     */
    public static void compressBitmap(Bitmap bitmap, String path) {
        if (!TextUtils.isEmpty(path) && bitmap != null) {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }

            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isBitmapData(byte[] data) {
        if (data == null) {
            return false;
        }

        try {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, opt);
            if (opt.outWidth > 0 && opt.outHeight > 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isBitmapData(String path) {
        if (!TextUtils.isEmpty(path)) {
            try {
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inPurgeable = true;
                opt.inJustDecodeBounds = true;

                BitmapFactory.decodeFile(path, opt);
                if (opt.outWidth > 0 && opt.outHeight > 0) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static Bitmap loadBitmap(String path) {
        return loadBitmap(path, ExifHelper.getRotationFromExif(path));
    }

    public static Bitmap loadBitmap(String path, int rotation) {
        return loadBitmap(path, rotation, MAX_MEMORY_SIZE);
    }

    public static Bitmap loadBitmap(String path, int rotation, long maxMemorySize) {
        if (TextUtils.isEmpty(path) || !(new File(path).exists())) {
            return null;
        }

        BitmapFactory.Options options = createBitmapFactoryOptions(path, maxMemorySize);
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);

        if (rotation != 0 && bitmap != null) {
            Matrix matrix = new Matrix();
            matrix.postRotate((float) rotation);
            Bitmap tmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (tmp != null) {
                bitmap.recycle();
                bitmap = tmp;
            }
        }

        return bitmap;
    }

    public static BitmapFactory.Options createBitmapFactoryOptions(String path, long maxMemorySize) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPurgeable = true;
        opt.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path, opt);
        int width = opt.outWidth;
        int height = opt.outHeight;

        BitmapFactory.Options ret = new BitmapFactory.Options();
        ret.inPurgeable = true;
        ret.inInputShareable = true;
        ret.inSampleSize = makeSample(width, height, maxMemorySize);

        return ret;
    }

    private static int makeSample(int width, int height, long maxMemorySize) {
        long fileMemorySize = width * height * 4;
        int sample = 1;
        if (fileMemorySize <= maxMemorySize) {
            sample = 1;
        } else if (fileMemorySize <= maxMemorySize * 4) {
            sample = 2;
        } else {
            long times = fileMemorySize / maxMemorySize;
            sample = (int) (Math.log(times) / Math.log(2.0)) + 1;
        }

        int inSampleScale = (int) (Math.log(sample) / Math.log(2.0));
        inSampleScale = (int) Math.scalb(1, inSampleScale);
        int inSampleWidth = width / inSampleScale;
        int inSampleHeight = height / inSampleScale;
        // 对图像宽高进行判断，若宽高大于2048，进一步压缩
        while (inSampleWidth >= MAX_WIDTH || inSampleHeight >= MAX_HEIGHT) {
            inSampleWidth = inSampleWidth / 2;
            inSampleHeight = inSampleHeight / 2;
            sample = sample * 2;
        }

        return sample;
    }

    public static void main(String[] argv) {
        int sample = makeSample(5000, 100, 960 * 960 * 4);

        System.out.print(sample);
    }

}
