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

import org.opencv.core.CvException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {


    private ImageView imageView;
    private ImageUtil imageUtil;
    private Uri imageUri;
    private Bitmap bitmap;
    private OpenCVUtil openCVUtil;

    private Classifier tileClassifier;
    private static final int INPUT_SIZE = 64;
    private static final String INPUT_NAME = "conv2d_1_input";
    private static final String OUTPUT_NAME = "dense_2/Softmax";

    private static final String MODEL_FILE = "file:///android_asset/frozen_tile_classifier.pb";
    private static final String LABEL_FILE = "file:///android_asset/tile_labels.txt";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openCVUtil = new OpenCVUtil(this);
        openCVUtil.loadOpenCV();


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
                    launch();
                } catch (IOException e) {
                    Log.e("IO", "Failed to load image from camera " + e.toString());
                }
            }
            if(requestCode == Request.LOAD_IMAGE) {
                Log.i("Gallery", "Opening gallery");
                imageUri = data.getData();
                bitmap = imageUtil.loadBitmapFromGallery(imageUri);
                launch();
            }
        }
    }

    private void launch(){
        try {
            List<Bitmap> bitmaps = ImageProcessing.process(bitmap, INPUT_SIZE);
            List<Classifier.Recognition> recognitions = recognize(bitmaps,15*3);
            Log.i("Recognition", buildResultMatrix(recognitions));

        } catch (CvException e) {
            Log.d("Exception", e.getMessage());
        }
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



}
