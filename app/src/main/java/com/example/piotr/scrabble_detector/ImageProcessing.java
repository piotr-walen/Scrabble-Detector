package com.example.piotr.scrabble_detector;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

class ImageProcessing {

    static Mat processBitmap(Mat sourceImageMat) {
        Log.i("OpenCV", "Started bitmap processing");

        Mat imageMat = sourceImageMat.clone();
        Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.blur(imageMat, imageMat, new Size(7, 7));
        Imgproc.Canny(imageMat, imageMat, 10.0, 100.0);
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
                new Size(5, 5));
        Imgproc.dilate(imageMat, imageMat, element);
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(imageMat, contours, new Mat(), Imgproc.RETR_LIST,
                Imgproc.CHAIN_APPROX_SIMPLE);
        if (!contours.isEmpty()) {
            MatOfPoint contour = findMaxAreaContour(contours);
            MatOfPoint approxContour = squareContour(contour);

            List<Point> points = approxContour.toList();
            Log.i("OpenCV", "points = " + points.toString());
            Log.i("OpenCV", "number of points = " + Integer.toString(points.size()));

            if (points.size() == 4) {
                Point middlePoint = findCenterPoint(points);
                Log.i("OpenCV", "middle point = " + middlePoint.toString());
                List<Point> sortedPoints = sortPointsClockwise(points);
                Log.i("OpenCV", "sorted points = " + sortedPoints.toString());
                warpImage(sortedPoints, sourceImageMat, imageMat);

            } else {
                Log.e("OpenCV", "Failed to find correct contour.");
            }
        } else {
            Log.e("OpenCV", "Failed to find contour");
        }

        Log.i("OpenCV", "Processed mat: " + imageMat.toString());
        return imageMat;
    }


    static MatOfPoint findMaxAreaContour(List<MatOfPoint> contours) {
        MatOfPoint contour = contours.get(0);
        for (MatOfPoint c : contours) {
            if (Imgproc.contourArea(c) > Imgproc.contourArea(contour)) {
                contour = c;
            }
        }
        return contour;
    }

    static MatOfPoint squareContour(MatOfPoint contour) {
        MatOfPoint2f contour2f = new MatOfPoint2f();
        contour.convertTo(contour2f, CvType.CV_32FC2);
        double epsilon = 0.03 * Imgproc.arcLength(contour2f, true);
        MatOfPoint2f approxContour2f = new MatOfPoint2f();
        Imgproc.approxPolyDP(contour2f, approxContour2f, epsilon, true);
        MatOfPoint approxContour = new MatOfPoint();
        approxContour2f.convertTo(approxContour, CvType.CV_32S);

        return approxContour;
    }

    static Point findCenterPoint(List<Point> points) {
        int sumX = 0;
        int sumY = 0;
        int n = points.size();
        for (Point point : points) {
            sumX += point.x;
            sumY += point.y;
        }
        return new Point(sumX / n, sumY / n);
    }

    static List<Point> sortPointsClockwise(List<Point> points) {
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

    static void warpImage(List<Point> sortedPoints, Mat sourceImageMat, Mat imageMat) {
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
        Imgproc.warpPerspective(sourceImageMat, imageMat, M, new Size(size, size));
        Log.i("OpenCV", "Image has been warped");
    }

    static ArrayList<Mat> sliceMat(Mat image, Size size) {
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
                    Log.i("OpenCV", "x = " + Integer.toString(x) + " y = "+Integer.toString(y));
                    Mat slice = image.submat(x,x+slice_width,y,y+slice_width);

                    Log.i("OpenCV", "slice "+slice.toString());
                    Mat outSlice = new Mat();
                    Imgproc.resize(slice, outSlice, size);
                    slices.add(outSlice);
                }
            }
        }
        return slices;
    }
}
