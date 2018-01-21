package com.example.piotr.scrabble_detector;

import android.app.Application;
import android.graphics.Bitmap;

import org.opencv.android.OpenCVLoader;

import java.util.List;


public class MyApplication extends Application {
    private Bitmap bitmap;
    private Bitmap warpedBitmap;
    private List<Bitmap> slices;

    public MyApplication() {
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }


    public Bitmap getWarpedBitmap() {
        return warpedBitmap;
    }

    public void setWarpedBitmap(Bitmap warpedBitmap) {
        this.warpedBitmap = warpedBitmap;
    }


    public List<Bitmap> getSlices() {
        return slices;
    }

    public void setSlices(List<Bitmap> slices) {
        this.slices = slices;
    }
}
