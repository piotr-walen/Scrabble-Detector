package com.example.piotr.scrabble_detector;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvException;

import java.util.List;

public class WarpedActivity extends AppCompatActivity {
    private static final int INPUT_SIZE = 64;
    private ImageView imageView;
    static {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warped);
        imageView = findViewById(R.id.warped_image_view);
        warp();

        Button btnAccept = findViewById(R.id.accept_warped_button);
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slice();

            }
        });

        Button btnDecline = findViewById(R.id.decline_warped_button);
        btnDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }

    private void warp(){
        try {

            MyApplication app = (MyApplication) getApplicationContext();
            Bitmap bitmap = app.getBitmap();

            imageView.setImageBitmap(bitmap);
            app.setWarpedBitmap(bitmap);

//            Bitmap warpedBitmap = ImageProcessing.warp(bitmap);
//            imageView.setImageBitmap(warpedBitmap);
//            app.setWarpedBitmap(warpedBitmap);

        } catch (CvException e) {
            Log.d("OpenCV", e.getMessage());
        }
    }

    private void slice() {
        try {
            MyApplication app = (MyApplication) getApplicationContext();
            Bitmap warpedBitmap = app.getWarpedBitmap();
            List<Bitmap> slices = ImageProcessing.slice(warpedBitmap, INPUT_SIZE);
            app.setSlices(slices);


        } catch (CvException e){
            Log.d("OpenCV", e.getMessage());
        }
    }




}
