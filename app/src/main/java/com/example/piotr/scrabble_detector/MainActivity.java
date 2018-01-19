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

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {


    private ImageView imageView;
    private ImageUtil imageUtil;
    private Uri imageUri;
    private Bitmap bitmap;


    private Classifier tileClassifier;
    private static final int INPUT_SIZE = 64;
    private static final String INPUT_NAME = "conv2d_1_input";
    private static final String OUTPUT_NAME = "dense_2/Softmax";

    private static final String MODEL_FILE = "file:///android_asset/frozen_tile_classifier.pb";
    private static final String LABEL_FILE = "file:///android_asset/tile_labels.txt";


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageUtil = new ImageUtil(getContentResolver());

        setContentView(R.layout.activity_capture_image);
        Button btnCamera = findViewById(R.id.camera_button);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File photo = new File(Environment.getExternalStorageDirectory(),
                        "source_image.jpg");
                imageUri = Uri.fromFile(photo);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, Request.IMAGE_CAPTURE);
            }
        });
        Button btnGallery = findViewById(R.id.gallery_button);
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, Request.LOAD_IMAGE);
            }
        });
        imageView = findViewById(R.id.imageView);

        loadModel();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if(requestCode ==  Request.IMAGE_CAPTURE){
                try {
                    Log.i("Camera", "Opening camera");
                    imageUtil.loadBitmapFromCamera(imageUri);
                    launchOpenCV();
                } catch (IOException e) {
                    Log.e("IO", "Failed to load image from camera " + e.toString());
                }
            }
            if(requestCode == Request.LOAD_IMAGE) {
                Log.i("Gallery", "Opening gallery");
                imageUri = data.getData();
                bitmap = imageUtil.loadBitmapFromGallery(imageUri);
                launchOpenCV();
            }
        }
    }

    private void launchOpenCV() {
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. " +
                    "Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0,
                    this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private static final int INPUT_IMAGE_SIZE = 64;
    private static final Size size = new Size(INPUT_IMAGE_SIZE, INPUT_IMAGE_SIZE);
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    try {
//                        Mat sourceMat = imageUtil.createMat(imageLoader.getBitmap());
//                        Mat outputMat = ImageProcessing.processBitmap(sourceMat);
//                        Bitmap outputBitmap = imageUtil.createBitmap(outputMat);
//                        imageUtil.saveImage(outputBitmap, "output_image");

                        List<Bitmap> bitmaps = imageProcessing();
                        List<Classifier.Recognition> recognitions = recognize(bitmaps,15*3);
                        Log.i("Recognition", buildResultMatrix(recognitions));

                    } catch (CvException e) {
                        Log.d("Exception", e.getMessage());
                    }
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };


    private List<Bitmap> imageProcessing(){
        imageView.setImageBitmap(bitmap);
        Mat outputMat = imageUtil.createMat(bitmap);
        List<Mat> slices = ImageProcessing.sliceMat(outputMat, size);
        List<Bitmap> bitmaps = new ArrayList<>();
        for (int i = 0; i<slices.size(); i++) {
            Bitmap bitmap = imageUtil.createBitmap(slices.get(i));
            bitmaps.add(bitmap);
        }

        return bitmaps;
    }

    private List<Classifier.Recognition> recognize(List<Bitmap> bitmaps, int batchSize) {
        //int batchSize = 15*5;
        List<Classifier.Recognition> recognitions = new ArrayList<>();
        for(int i = 0; i<15*15/batchSize; i++){
            List<Bitmap> batchOfBitmaps = bitmaps.subList(i*batchSize,i*batchSize+batchSize);
            List<Classifier.Recognition> batchRecognition = tileClassifier.recognizeImages(batchOfBitmaps);
            recognitions.addAll(batchRecognition);
        }
        return recognitions;
    }

    private String buildResultMatrix(List<Classifier.Recognition> recognitions){
        String[][] results = new String[15][15];
        for (int i = 0; i<15*15; i++) {
            results[i/15][i%15] = recognitions.get(i).getId();
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i<15; i++){
            for(int j = 0; j<15; j++){
                stringBuilder.append(results[i][j]);
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    private void loadModel() {
        Log.i("TensorFlow", "loading model");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    tileClassifier = TileClassifier.create(
                            getAssets(),
                            MODEL_FILE,
                            LABEL_FILE,
                            INPUT_SIZE,
                            INPUT_NAME,
                            OUTPUT_NAME);
                } catch (final Exception e) {
                    String error = "Error initializing classifiers! ";
                    Log.e("TensorFlow", error + e.toString());
                    throw new RuntimeException(error, e);
                }
            }
        }).start();
    }


}
