package com.example.piotr.scrabble_detector;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ResultActivity extends AppCompatActivity {
    private Classifier tileClassifier;
    private List<Bitmap> bitmaps;
    private static final int INPUT_SIZE = 64;
    private static final String INPUT_NAME = "conv2d_1_input";
    private static final String OUTPUT_NAME = "dense_2/Softmax";
    private static final String MODEL_FILE = "file:///android_asset/frozen_tile_classifier.pb";
    private static final String LABEL_FILE = "file:///android_asset/tile_labels.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        loadModels();

    }

    private void loadModels() {
        Log.i("TensorFlow", "loading model");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    tileClassifier = TileClassifier.create(getAssets(), MODEL_FILE, LABEL_FILE,
                            INPUT_SIZE, INPUT_NAME, OUTPUT_NAME);

                } catch (final Exception e) {
                    String error = "Error initializing classifiers! ";
                    Log.e("TensorFlow", error + e.toString());
                    throw new RuntimeException(error, e);
                }
            }
        }).start();
    }

    private void launch(){
        List<Classifier.Recognition> recognitions = recognize(bitmaps,15*3);
        String result = buildResultMatrix(recognitions);
        Log.i("Recognition", result);
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
