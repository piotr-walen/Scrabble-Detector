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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {


    private ImageView imageView;
    private ImageLoader imageLoader;
    private ImageUtil imageUtil;

    private Classifier tileClassifier;
    private static final int INPUT_SIZE = 64;
    private static final int IMAGE_MEAN = 117;
    private static final float IMAGE_STD = 1;
    private static final String INPUT_NAME = "conv2d_1_input";
    private static final String OUTPUT_NAME = "dense_2/Softmax";

    private static final String MODEL_FILE = "file:///android_asset/frozen_tile_classifier.pb";
    private static final String LABEL_FILE = "file:///android_asset/tile_labels.txt";


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageLoader = new ImageLoader(getContentResolver());
        imageUtil = new ImageUtil(getContentResolver());

        setContentView(R.layout.activity_capture_image);
        Button btnCamera = findViewById(R.id.camera_button);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File photo = new File(Environment.getExternalStorageDirectory(),
                        "source_image.jpg");
                imageLoader.setImageUri(Uri.fromFile(photo));
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageLoader.getImageUri());
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
            imageLoader.loadBitmap(requestCode, data);
            launchOpenCV();
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


                        Bitmap warped_bitmap = imageLoader.getBitmap();
                        imageView.setImageBitmap(warped_bitmap);
                        Mat outputMat = imageUtil.createMat(warped_bitmap);
                        List<Mat> slices = ImageProcessing.sliceMat(outputMat, size);


                        String[][] results = new String[15][15];
                        for (int i = 0; i<slices.size(); i++) {
                            Bitmap bitmap = imageUtil.createBitmap(slices.get(i));
                            List<Classifier.Recognition> recognitions =
                                    tileClassifier.recognizeImage(bitmap);
                            for (Classifier.Recognition  recognition : recognitions) {
                                results[i/15][i%15] = recognition.getId();
                            }
                        }

                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i<15; i++){
                            for(int j = 0; j<15; j++){
                                stringBuilder.append(results[i][j]);
                            }
                            stringBuilder.append("\n");
                        }
                        String result = stringBuilder.toString();
                        Log.i("Recognition", result);


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
                            IMAGE_MEAN,
                            IMAGE_STD,
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
