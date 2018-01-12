package com.example.piotr.scrabble_detector;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int RESULT_LOAD_IMAGE = 2;
    private ImageView imageView;
    private Bitmap bitmap;
    private Uri imageUri;
    private List<Classifier> mClassifiers = new ArrayList<>();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_image);
        Button btnCamera = findViewById(R.id.camera_button);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File photo = new File(Environment.getExternalStorageDirectory(),
                        "source_image.jpg");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
                imageUri = Uri.fromFile(photo);
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
        });
        Button btnGallery = findViewById(R.id.gallery_button);
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });
        imageView = findViewById(R.id.imageView);
        Log.i("TensorFlow", "loading model");
        loadModel();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.i("Camera", "Opening camera");
            Uri selectedImage = imageUri;
            getContentResolver().notifyChange(selectedImage, null);
            ContentResolver cr = getContentResolver();
            try {
                bitmap = android.provider.MediaStore.Images.Media
                        .getBitmap(cr, selectedImage);
                bitmap = ExifUtil.rotateBitmap(selectedImage.getPath(), bitmap);
                launchOpenCV();
            } catch (Exception e) {
                Log.e("Camera", e.toString());
            }
        }

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Log.i("Gallery", "Opening gallery");
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            if (selectedImage != null) {
                try {
                    Cursor cursor = getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String mCurrentPhotoPath = cursor.getString(columnIndex);
                        cursor.close();
                        bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
                        bitmap = ExifUtil.rotateBitmap(mCurrentPhotoPath, bitmap);
                        launchOpenCV();
                    }
                } catch (Exception e) {
                    Log.e("Gallery", e.toString());
                }
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

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    try {
                        Mat outputMat = ImageProcessing.processBitmap(bitmap);
                        Bitmap outputBitmap = createBitmap(outputMat);
                        imageView.setImageBitmap(outputBitmap);
                        saveImage(outputBitmap);

                        List<Mat> slices = ImageProcessing.sliceMat(outputMat);

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
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mClassifiers.add(
                            TileClassifier.create(getAssets(), "TileClassifier",
                                    "frozen_tile_classifier.pb",
                                    Arrays.asList("non-tile", "tile"),
                                    ImageProcessing.INPUT_IMAGE_SIZE, "input", "output",
                                    true));
                } catch (final Exception e) {
                    Log.e("TensorFlow", "Error initializing classifiers! " + e.toString());
                    throw new RuntimeException("Error initializing classifiers!", e);
                }
            }
        }).start();
    }

    private Bitmap createBitmap(Mat imageMat){
        Bitmap outputBitmap = Bitmap.createBitmap(imageMat.cols(), imageMat.rows(),
                Bitmap.Config.RGB_565);
        Utils.matToBitmap(imageMat, outputBitmap);
        return outputBitmap;
    }

    private void saveImage(Bitmap bitmap) {
        try {
            String path = Environment.getExternalStorageDirectory().toString();
            File file = new File(path, "output_image.jpg");
            OutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
            fOut.flush();
            fOut.close();
            MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(),
                    file.getName(), file.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
