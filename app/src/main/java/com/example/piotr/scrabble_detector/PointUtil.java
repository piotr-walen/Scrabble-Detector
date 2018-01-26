package com.example.piotr.scrabble_detector;

import org.opencv.core.Point;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PointUtil {

    static Point findCenterPoint(List<Point> points) throws IllegalArgumentException {
        if (points == null) {
            throw new IllegalArgumentException("Method argument cannot be null");
        }
        int sumX = 0;
        int sumY = 0;
        int n = points.size();
        for (Point point : points) {
            sumX += point.x;
            sumY += point.y;
        }
        return new Point(sumX / n, sumY / n);
    }

    static List<Point> sortPointsClockwise(List<Point> points) throws IllegalArgumentException {
        if (points == null) {
            throw new IllegalArgumentException("Method argument cannot be null");
        }
        if (points.size() == 1) {
            return points;
        }

        final Point center = findCenterPoint(points);

        Collections.sort(points, new Comparator<Point>() {
            @Override
            public int compare(Point p1, Point p2) {
                return (int) (calculateAngle(center, p2) - calculateAngle(center, p1));
            }
        });

        return points;
    }

    static double calculateAngle(Point p1, Point p2) throws IllegalArgumentException {
        if (p1 == null || p2 == null) {
            throw new IllegalArgumentException("Method argument cannot be null");
        }
        double angle = Math.toDegrees(Math.atan2(p2.y - p1.y, p2.x - p1.x));
        return angle >= 0 ? angle : angle + 360.0;
    }
}
