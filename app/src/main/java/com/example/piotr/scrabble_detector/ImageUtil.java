package com.example.piotr.scrabble_detector;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

class ImageUtil {

    private ContentResolver contentResolver;

    ImageUtil(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }



    void saveImage(Bitmap bitmap, String fileName) {
        try {
            String path = Environment.getExternalStorageDirectory().toString();
            File file = new File(path, fileName + ".jpg" );
            OutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
            fOut.flush();
            fOut.close();
            MediaStore.Images.Media.insertImage(contentResolver, file.getAbsolutePath(),
                    file.getName(), file.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Bitmap loadBitmapFromCamera (Uri imageUri) throws IOException {
        contentResolver.notifyChange(imageUri, null);
        Bitmap bitmap;
        bitmap = android.provider.MediaStore.Images.Media
                .getBitmap(contentResolver, imageUri);
        bitmap = ExifUtil.rotateBitmap(imageUri.getPath(), bitmap);
        return bitmap;
    }

    Bitmap loadBitmapFromGallery(Uri imageUri) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = contentResolver.query(imageUri,
                filePathColumn, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String mCurrentPhotoPath = cursor.getString(columnIndex);
            cursor.close();
            Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
            return ExifUtil.rotateBitmap(mCurrentPhotoPath, bitmap);
        } else return null;

    }


}
