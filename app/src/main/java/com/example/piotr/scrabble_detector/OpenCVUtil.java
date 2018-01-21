package com.example.piotr.scrabble_detector;

import android.content.Context;
import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;

class OpenCVUtil {

    private Context context;
    private BaseLoaderCallback mLoaderCallback;

    OpenCVUtil(Context context) {
        this.context = context;
        mLoaderCallback = initializeLoaderCallback();
    }

    private BaseLoaderCallback initializeLoaderCallback(){
        return new BaseLoaderCallback(context) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS: {
                        Log.i("OpenCV", "OpenCV loaded successfully");

                    }
                    break;
                    default: {
                        super.onManagerConnected(status);
                    }
                    break;
                }
            }
        };
    }

    void loadOpenCV() {
        if (!org.opencv.android.OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. " +
                    "Using OpenCV Manager for initialization");
            org.opencv.android.OpenCVLoader.initAsync(org.opencv.android.OpenCVLoader.OPENCV_VERSION_3_0_0,
                    context, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

}
