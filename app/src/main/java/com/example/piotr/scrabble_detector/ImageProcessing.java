package com.example.piotr.scrabble_detector;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class ImageProcessing {


    static Bitmap warp(Bitmap bitmap){
        Mat sourceMat = createMat(bitmap);
        Mat outputMat = preprocessMat(sourceMat);
        List<Point> corners = ImageProcessing.findCorners(outputMat);
        Mat warpedMat = ImageProcessing.warpMat(corners, sourceMat);
        return createBitmap(warpedMat);
    }



    static List<Bitmap> slice (Bitmap bitmap, int output_size){
        Mat outputMat = createMat(bitmap);
        Size size = new Size(output_size,output_size);
        List<Mat> slices = ImageProcessing.sliceMat(outputMat, size);
        List<Bitmap> bitmaps = new ArrayList<>();
        for (int i = 0; i<slices.size(); i++) {
            bitmaps.add(createBitmap(slices.get(i)));
        }

        return bitmaps;
    }

    private static Mat createMat(Bitmap bitmap){
        Mat imageMat = new Mat();
        Utils.bitmapToMat(bitmap, imageMat);
        Imgproc.cvtColor(imageMat,imageMat,Imgproc.COLOR_BGRA2BGR);

        return imageMat;
    }

    private static Bitmap createBitmap(Mat imageMat){
        Bitmap outputBitmap = Bitmap.createBitmap(imageMat.cols(), imageMat.rows(),
                Bitmap.Config.RGB_565);
        Utils.matToBitmap(imageMat, outputBitmap);
        return outputBitmap;
    }


    private static Mat preprocessMat(Mat sourceMat) {
        Log.i("OpenCV", "Started bitmap processing");

        Mat imageMat = sourceMat.clone();
        Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.blur(imageMat, imageMat, new Size(7, 7));
        Imgproc.Canny(imageMat, imageMat, 10.0, 100.0);
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
                new Size(5, 5));
        Imgproc.dilate(imageMat, imageMat, element);

        return imageMat;
    }

    private static List<Point> findCorners(Mat preprocessedMat){
        List<Point> points = Collections.emptyList();
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(preprocessedMat, contours, new Mat(), Imgproc.RETR_LIST,
                Imgproc.CHAIN_APPROX_SIMPLE);
        if (!contours.isEmpty()) {
            MatOfPoint contour = findMaxAreaContour(contours);
            MatOfPoint approxContour = squareContour(contour);
            points = approxContour.toList();
            Log.i("OpenCV", "points = " + points.toString());
            Log.i("OpenCV", "number of points = " + Integer.toString(points.size()));

            if (points.size() == 4) {
                Point middlePoint = findCenterPoint(points);
                Log.i("OpenCV", "middle point = " + middlePoint.toString());
                points = sortPointsClockwise(points);
                Log.i("OpenCV", "sorted points = " + points.toString());

            } else {
                Log.e("OpenCV", "Failed to find correct contour.");
            }
        } else {
            Log.e("OpenCV", "Failed to find contour");
        }

        return points;
    }

    private static MatOfPoint findMaxAreaContour(List<MatOfPoint> contours) {
        MatOfPoint contour = contours.get(0);
        for (MatOfPoint c : contours) {
            if (Imgproc.contourArea(c) > Imgproc.contourArea(contour)) {
                contour = c;
            }
        }
        return contour;
    }

    private static MatOfPoint squareContour(MatOfPoint contour) {
        MatOfPoint2f contour2f = new MatOfPoint2f();
        contour.convertTo(contour2f, CvType.CV_32FC2);
        double epsilon = 0.03 * Imgproc.arcLength(contour2f, true);
        MatOfPoint2f approxContour2f = new MatOfPoint2f();
        Imgproc.approxPolyDP(contour2f, approxContour2f, epsilon, true);
        MatOfPoint approxContour = new MatOfPoint();
        approxContour2f.convertTo(approxContour, CvType.CV_32S);

        return approxContour;
    }

    private static Point findCenterPoint(List<Point> points) {
        int sumX = 0;
        int sumY = 0;
        int n = points.size();
        for (Point point : points) {
            sumX += point.x;
            sumY += point.y;
        }
        return new Point(sumX / n, sumY / n);
    }

    private static List<Point> sortPointsClockwise(List<Point> points) {
        Point centerPoint = findCenterPoint(points);
        double x_center = centerPoint.x;
        double y_center = centerPoint.y;

        List<Point> sortedPoints = new ArrayList<>();
        if (points.size() == 4) {
            for (Point point : points) {
                if (point.x <= x_center && point.y <= y_center) {
                    sortedPoints.add(0, point);
                } else if (point.x <= x_center && point.y > y_center) {
                    sortedPoints.add(1, point);
                } else if (point.x > x_center && point.y > y_center) {
                    sortedPoints.add(2, point);
                } else if (point.x > x_center && point.y <= y_center) {
                    sortedPoints.add(3, point);
                }
            }
        }

        return sortedPoints;
    }

    private static Mat warpMat(List<Point> sortedPoints, Mat sourceImageMat) {
        MatOfPoint2f src = new MatOfPoint2f();
        src.fromList(sortedPoints);
        Log.i("OpenCV", "warping... source points = " + src.toString());

        double size = 300;
        MatOfPoint2f dst = new MatOfPoint2f(
                new Point(0, 0),
                new Point(size, 0),
                new Point(size, size),
                new Point(0, size)
        );

        Log.i("OpenCV", "warping... destination points = " + dst.toString());
        Mat M = Imgproc.getPerspectiveTransform(src, dst);
        Mat outputMat = new Mat();
        Imgproc.warpPerspective(sourceImageMat, outputMat, M, new Size(size, size));
        Log.i("OpenCV", "Image has been WarpedActivity");

        return outputMat;
    }

    private static ArrayList<Mat> sliceMat(Mat image, Size size) {
        Log.i("OpenCV", "image " + image.toString());
        ArrayList<Mat> slices = new ArrayList<>();
        int width = image.width();
        int height = image.height();
        int slice_width = width/15;
        int slice_height = height/15;

        Log.i("OpenCV","slice_width= "+Integer.toString(slice_width) + " slice_height= "+ Integer.toString(slice_height));
        if (width > 0 && height > 0) {
            for (int i = 0; i<15; i++) {
                for (int j = 0; j<15; j++) {
                    int x = i*slice_width;
                    int y = j*slice_height;
                    //Log.i("OpenCV", "x = " + Integer.toString(x) + " y = "+Integer.toString(y));
                    Mat slice = image.submat(x,x+slice_width,y,y+slice_width);

                    //Log.i("OpenCV", "slice "+slice.toString());
                    Mat outSlice = new Mat();
                    Imgproc.resize(slice, outSlice, size);
                    slices.add(outSlice);
                }
            }
        }
        return slices;
    }
}
