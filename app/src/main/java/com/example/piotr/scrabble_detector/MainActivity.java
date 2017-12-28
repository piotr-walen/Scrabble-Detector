package com.example.piotr.scrabble_detector;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends Activity {

    ImageView imageView;
    Mat imageMat;
    Bitmap bitmap;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private String mCurrentPhotoPath;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_capture_image);

        Button btnCamera = findViewById(R.id.button);
        btnCamera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(intent, 0);

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        Log.i("IO", "IOException");
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }


            }
        });

        imageView = findViewById(R.id.imageView);
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(mCurrentPhotoPath));

                if (!OpenCVLoader.initDebug()) {
                    Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
                    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
                } else {
                    Log.d("OpenCV", "OpenCV library found inside package. Using it!");
                    mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
                }

            } catch (IOException e) {
                e.printStackTrace();
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
                    try {
                        imageProcessing();
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


    private void imageProcessing(){
        imageMat=new Mat(bitmap.getWidth(),bitmap.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap, imageMat);

        Imgproc.cvtColor(imageMat,imageMat,Imgproc.COLOR_RGB2GRAY);
        Imgproc.blur(imageMat,imageMat, new Size(7,7));
        Imgproc.Canny(imageMat,imageMat,10.0,100.0);
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
        Imgproc.dilate(imageMat,imageMat,element);
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(imageMat, contours, new Mat(), Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
        Imgproc.drawContours(imageMat, contours, 0, new Scalar(0,0,255),3);

        MatOfPoint contour = contours.get(0);
        MatOfPoint2f contour2f = new MatOfPoint2f();
        contour.convertTo(contour2f,CvType.CV_32FC2);
        double epsilon = 0.01 * Imgproc.arcLength(contour2f,true);
        MatOfPoint2f approxContour2f = new MatOfPoint2f();
        Imgproc.approxPolyDP(contour2f,approxContour2f,epsilon,true);
        MatOfPoint approxContour = new MatOfPoint();
        approxContour2f.convertTo(approxContour,CvType.CV_32S);
        List<MatOfPoint> approxContours = new ArrayList<>();
        approxContours.add(approxContour);
        Imgproc.drawContours(imageMat, approxContours, 0, new Scalar(0,0,255),3);


        Bitmap output_bitmap = Bitmap.createBitmap(imageMat.cols(), imageMat.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(imageMat, output_bitmap);
        imageView.setImageBitmap(output_bitmap);
    }

}
