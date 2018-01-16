package com.example.piotr.scrabble_detector;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

class ImageUtil {

    private ContentResolver contentResolver;
    ImageUtil(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    Mat createMat(Bitmap bitmap){
        Mat imageMat = new Mat();
        Utils.bitmapToMat(bitmap, imageMat);
        Imgproc.cvtColor(imageMat,imageMat,Imgproc.COLOR_BGRA2BGR);

        return imageMat;
    }

    Bitmap createBitmap(Mat imageMat){
        Bitmap outputBitmap = Bitmap.createBitmap(imageMat.cols(), imageMat.rows(),
                Bitmap.Config.RGB_565);
        Utils.matToBitmap(imageMat, outputBitmap);
        return outputBitmap;
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


}
