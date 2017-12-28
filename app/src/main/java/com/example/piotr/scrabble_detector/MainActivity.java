package com.example.piotr.scrabble_detector;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {

    ImageView imageView;
    Mat imageMat;
    Bitmap bitmap;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_capture_image);

        Button btnCamera = findViewById(R.id.button);
        btnCamera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 0);
            }
        });

        imageView = findViewById(R.id.imageView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            bitmap = (Bitmap) data.getExtras().get("data");
            if (!OpenCVLoader.initDebug()) {
                Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
                OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
            } else {
                Log.d("OpenCV", "OpenCV library found inside package. Using it!");
                mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            }


        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    imageMat=new Mat(bitmap.getWidth(),bitmap.getHeight(), CvType.CV_8UC1);
                    Utils.bitmapToMat(bitmap, imageMat);
                    try {
                        Imgproc.cvtColor(imageMat,imageMat,Imgproc.COLOR_RGB2GRAY);
                        Imgproc.blur(imageMat,imageMat, new Size(7,7));
                        Imgproc.Canny(imageMat,imageMat,10.0,100.0);
                        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
                                new  Size(5, 5));
                        Imgproc.dilate(imageMat,imageMat,element);

                        List<MatOfPoint> contours = new ArrayList<>();
                        Imgproc.findContours(imageMat, contours, new Mat(),
                                Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
                        Imgproc.drawContours(imageMat, contours, 0,
                                new Scalar(0,0,255),3);

                        Bitmap output_bitmap = Bitmap.createBitmap(imageMat.cols(), imageMat.rows(), Bitmap.Config.RGB_565);
                        Utils.matToBitmap(imageMat, output_bitmap);
                        imageView.setImageBitmap(output_bitmap);

                    } catch (CvException e){
                        Log.d("Exception",e.getMessage());
                    }
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


}
