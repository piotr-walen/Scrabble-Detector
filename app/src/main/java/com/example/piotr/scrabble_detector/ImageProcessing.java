package com.example.piotr.scrabble_detector;

import android.graphics.Bitmap;
import android.util.Log;

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

    static final int INPUT_IMAGE_SIZE = 64;

    static Mat processBitmap(Bitmap bitmap) {
        Mat sourceImageMat = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap, sourceImageMat);
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
        return imageMat;
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
        double epsilon = 0.01 * Imgproc.arcLength(contour2f, true);
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

    private static void warpImage(List<Point> sortedPoints, Mat sourceImageMat, Mat imageMat) {
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

    static ArrayList<Mat> sliceMat(Mat image) {
        int width = image.width();
        int height = image.height();
        ArrayList<Mat> slices = new ArrayList<>();
        Size size = new Size(INPUT_IMAGE_SIZE, INPUT_IMAGE_SIZE);

        for (int x = 0; x < width; x += width / 15) {
            for (int y = 0; y < height; y += height / 15) {
                Point p1 = new Point(x, y);
                Point p2 = new Point(p1.x + width, p1.y + height);
                Rect rectCrop = new Rect(p1.x, p1.y, (p2.x - p1.x + 1), (p2.y - p1.y + 1));
                Mat slice = image.submat(rectCrop);
                Imgproc.resize(slice,slice,size);
                slices.add(slice);
            }
        }

        return slices;
    }
}
