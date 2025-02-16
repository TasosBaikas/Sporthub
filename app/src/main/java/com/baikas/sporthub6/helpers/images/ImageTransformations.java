package com.baikas.sporthub6.helpers.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageTransformations {

    public static Bitmap getBitmapFromUri(Uri uri, Context context) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        if (inputStream != null) {
            inputStream.close();
        }

        // Read EXIF Data
        ExifInterface exif = null;
        inputStream = context.getContentResolver().openInputStream(uri);
        if (inputStream != null) {
            exif = new ExifInterface(inputStream);
            inputStream.close();
        }

        int orientation = exif != null ? exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL) : 0;
        Matrix matrix = new Matrix();

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            default:
                return bitmap;
        }

        // Rotate the bitmap
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }


    public static byte[] getByteArrayFromBitmap(Bitmap resizedBitmap,int quality){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.WEBP, quality, baos);
        return baos.toByteArray();
    }

    public static Bitmap resizeBitmap(Bitmap source, int maxWidth, int maxHeight) {//better use compress
        if (maxHeight <= 0 || maxWidth <= 0) {
            return source;
        }

        // Calculate the aspect ratio
        double aspectRatio = (double) source.getHeight() / (double) source.getWidth();
        double targetHeight = maxWidth * aspectRatio;
        if (targetHeight > maxHeight) {
            maxWidth = (int) (maxHeight / aspectRatio);
            targetHeight = maxHeight;
        }

        return Bitmap.createScaledBitmap(source, maxWidth, (int) targetHeight, true);
    }

}
