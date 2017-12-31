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
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    ImageView imageView;
    Mat imageMat;
    Bitmap bitmap;
    Bitmap output_bitmap;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static int RESULT_LOAD_IMAGE = 2;
    private Uri imageUri;
    private String mCurrentPhotoPath;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_image);
        Button btnCamera = findViewById(R.id.camera_button);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File photo = new File(Environment.getExternalStorageDirectory(), "source_image.jpg");
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
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            mCurrentPhotoPath = cursor.getString(columnIndex);
            cursor.close();
            bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
            bitmap = ExifUtil.rotateBitmap(mCurrentPhotoPath,bitmap);
            launchOpenCV();
        }
    }



    private void launchOpenCV(){
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
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
        Mat sourceImageMat = new Mat(bitmap.getWidth(),bitmap.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap, sourceImageMat);

        imageMat = sourceImageMat.clone();
        Imgproc.cvtColor(imageMat,imageMat,Imgproc.COLOR_RGB2GRAY);
        Imgproc.blur(imageMat,imageMat, new Size(7,7));
        Imgproc.Canny(imageMat,imageMat,10.0,100.0);
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
        Imgproc.dilate(imageMat,imageMat,element);

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(imageMat, contours, new Mat(), Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
        MatOfPoint contour = contours.get(0);
        for(MatOfPoint c : contours) {
            if(Imgproc.contourArea(c) > Imgproc.contourArea(contour)) {
                contour = c;
            }
        }
        MatOfPoint2f contour2f = new MatOfPoint2f();
        contour.convertTo(contour2f,CvType.CV_32FC2);
        double epsilon = 0.01 * Imgproc.arcLength(contour2f,true);
        MatOfPoint2f approxContour2f = new MatOfPoint2f();
        Imgproc.approxPolyDP(contour2f,approxContour2f,epsilon,true);
        MatOfPoint approxContour = new MatOfPoint();
        approxContour2f.convertTo(approxContour,CvType.CV_32S);
        List<MatOfPoint> approxContourList = new ArrayList<>();
        approxContourList.add(approxContour);

        imageMat = sourceImageMat.clone();
        Imgproc.drawContours(imageMat, approxContourList, 0, new Scalar(0,0,255),5);

        List<Point> points = approxContour.toList();
        Log.i("OpenCV", "points = " + points.toString());
        Log.i("OpenCV", "number of points = " + Integer.toString(points.size()));

        List<Point> sortedPoints = new ArrayList<>();
        if(points.size() == 4){
            Point middlePoint = center(points);
            Log.i("OpenCV","middle point = " + middlePoint.toString());
            sortedPoints = sort(points);
            Log.i("OpenCV", "sorted points = " + sortedPoints.toString());
        } else {
            Log.e("OpenCV", "Failed to find correct contour.");
        }
        if(!sortedPoints.isEmpty()){
            MatOfPoint2f src = new MatOfPoint2f();
            src.fromList(sortedPoints);
            Log.i("OpenCV","warping... source points = " + src.toString());

            double size = 300;
            MatOfPoint2f dst = new MatOfPoint2f(
                    new Point(0,0), // awt has a Point class too, so needs canonical name here
                    new Point(size,0),
                    new Point(size,size),
                    new Point(0,size)
            );

            Log.i("OpenCV","warping... destination points = " +dst.toString());

            Mat M = Imgproc.getPerspectiveTransform(src,dst);
            Imgproc.warpPerspective(sourceImageMat,imageMat,M,new Size(size,size));
            Log.i("OpenCV","Image has been warped");
        } else {
            Toast.makeText(this, "Failed to warp image!", Toast.LENGTH_SHORT).show();
            Log.e("OpenCV","Failed to warp image");

        }


        //checking output
        Mat outputMat = imageMat.clone();
        output_bitmap = Bitmap.createBitmap(outputMat.cols(), outputMat.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(outputMat, output_bitmap);
        drawOnImageView(output_bitmap);
        saveImage(output_bitmap);

    }

    private void drawOnImageView(Bitmap bitmap){
        imageView.setImageBitmap(bitmap);
    }

    private void saveImage(Bitmap bitmap){
        try {
            String path = Environment.getExternalStorageDirectory().toString();
            File file = new File(path, "output_image.jpg"); // the File to save , append increasing numeric counter to prevent files from getting overwritten.
            OutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
            fOut.flush();
            fOut.close();
            MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());
        } catch (Exception e){
            e.printStackTrace();
        }
    }



    private Point center(List<Point> points){
        int sumX = 0;
        int sumY = 0;
        int n = points.size();
        for (Point point : points) {
            sumX += point.x;
            sumY += point.y;
        }
        Point center = new Point(sumX/n,sumY/n);
        return center;
    }

    private List<Point> sort(List<Point> points){
        Point centerPoint = center(points);
        double x_center = centerPoint.x;
        double y_center = centerPoint.y;

        List<Point> sortedPoints = new ArrayList<Point>();
        if(points.size() == 4){
            for(Point point : points) {
                if(point.x < x_center && point.y < y_center){
                    sortedPoints.add(0,point);
                }
                if(point.x < x_center && point.y > y_center){
                    sortedPoints.add(1,point);
                }
                if(point.x > x_center && point.y > y_center){
                    sortedPoints.add(2,point);
                }
                if(point.x > x_center && point.y < y_center){
                    sortedPoints.add(3,point);
                }
            }
        }

        return sortedPoints;
    }

}
