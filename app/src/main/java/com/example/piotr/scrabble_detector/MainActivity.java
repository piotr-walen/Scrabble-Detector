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
import android.widget.TextView;

import org.opencv.android.OpenCVLoader;
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



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        OpenCVUtil openCVUtil = new OpenCVUtil(this);
        openCVUtil.loadOpenCV();

        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.source_view);

        imageUtil = new ImageUtil(getContentResolver());
        Log.i("Image util", imageUtil.toString());
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

        Button btnLaunchWarp = findViewById(R.id.warp_button);
        btnLaunchWarp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApplication app = (MyApplication) getApplicationContext();
                app.setBitmap(bitmap);

                Intent i = new Intent(getApplicationContext(), WarpedActivity.class);
                startActivity(i);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if(requestCode ==  Request.IMAGE_CAPTURE){
                try {
                    Log.i("Camera", "Opening camera");
                    bitmap = imageUtil.loadBitmapFromCamera(imageUri);
                    imageView.setImageBitmap(bitmap);

                } catch (IOException e) {
                    Log.e("IO", "Failed to load image from camera " + e.toString());
                }
            }
            if(requestCode == Request.LOAD_IMAGE) {
                Log.i("Gallery", "Opening gallery");
                imageUri = data.getData();
                bitmap = imageUtil.loadBitmapFromGallery(imageUri);
                imageView.setImageBitmap(bitmap);


            }
        }
    }
}
