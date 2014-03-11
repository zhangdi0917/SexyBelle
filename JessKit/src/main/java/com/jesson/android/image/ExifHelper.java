package com.jesson.android.image;

import android.media.ExifInterface;

import java.io.IOException;

public class ExifHelper {

    public static final int ROTATION_0 = 0;
    public static final int ROTATION_90 = 90;
    public static final int ROTATION_180 = 180;
    public static final int ROTATION_270 = 270;

    public static int getRotationFromExif(String filePath) {
        try {
            ExifInterface exif = new ExifInterface(filePath);
            String rotationStr = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            if (rotationStr != null) {
                int rotation = Integer.valueOf(rotationStr);
                switch (rotation) {
                    case 6:
                        return ROTATION_90;
                    case 3:
                        return ROTATION_180;
                    case 8:
                        return ROTATION_270;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            // on X10, the exif lib has crash issue
            e.printStackTrace();
        }
        return ROTATION_0;
    }
}

