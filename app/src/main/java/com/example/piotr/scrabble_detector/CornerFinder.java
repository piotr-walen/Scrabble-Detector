package com.example.piotr.scrabble_detector;

import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class CornerFinder {

    static List<Point> findCorners(Mat preprocessedMat) {
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
                Point middlePoint = PointUtil.findCenterPoint(points);
                Log.i("OpenCV", "middle point = " + middlePoint.toString());
                points = PointUtil.sortPointsClockwise(points);
                Log.i("OpenCV", "sorted points = " + points.toString());

            } else {
                Log.e("OpenCV", "Failed to find correct contour.");
            }
        } else {
            Log.e("OpenCV", "Failed to find contour");
        }

        return points;
    }

    static MatOfPoint findMaxAreaContour(List<MatOfPoint> contours) {
        return Collections.max(contours, new Comparator<MatOfPoint>() {
            @Override
            public int compare(MatOfPoint c1, MatOfPoint c2) {
                return (int) (Imgproc.contourArea(c1) - Imgproc.contourArea(c2));
            }
        });
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

}
