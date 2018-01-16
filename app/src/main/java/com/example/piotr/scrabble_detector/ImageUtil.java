package com.example.piotr.scrabble_detector;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by piotr on 1/16/18.
 */

public class ImageUtils {

    private ContentResolver contentResolver;
    public ImageUtils(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    Bitmap createBitmap(Mat imageMat){
        Bitmap outputBitmap = Bitmap.createBitmap(imageMat.cols(), imageMat.rows(),
                Bitmap.Config.RGB_565);
        Utils.matToBitmap(imageMat, outputBitmap);
        return outputBitmap;
    }

    void saveImage(Bitmap bitmap) {
        try {
            String path = Environment.getExternalStorageDirectory().toString();
            File file = new File(path, "output_image.jpg");
            OutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
            fOut.flush();
            fOut.close();
            MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(),
                    file.getName(), file.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
