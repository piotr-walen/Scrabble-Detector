package com.example.piotr.scrabble_detector;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.opencv.core.Point;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;


@RunWith(JUnitParamsRunner.class)
public class ImageProcessingTest {
    @Test
    public void findCenter_ListWithOnePointIsPassed() {
        Point expected = new Point(-20, 10);
        Point actual = ImageProcessing.findCenterPoint(Collections.singletonList(expected));
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void findCenter_ListWithTwoPointsIsPassed() {
        List<Point> points = Arrays.asList(
                new Point(-10, 0),
                new Point(-2, 10)
        );
        Point expected = new Point(-6, 5);
        Point actual = ImageProcessing.findCenterPoint(points);
        assertThat(actual, equalTo(expected));
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void findCenter_NullIsPassed() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Method argument cannot be null");
        ImageProcessing.findCenterPoint(null);
    }

    @Test
    public void sortPointsClockwise_NullIsPassed() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Method argument cannot be null");
        ImageProcessing.sortPointsClockwise(null);
    }

    @Test
    public void sortPointClockwise_OnePointIsPassed() {
        List<Point> points = Collections.singletonList(new Point(-1, 10));
        assertThat(ImageProcessing.sortPointsClockwise(points), equalTo(points));
    }


    @Test
    public void sortPointClockwise_ValidPointListIsPassed() {
        List<Point> input = Arrays.asList(
                new Point(-1, -1),
                new Point(1, -1),
                new Point(0, 2)
        );

        List<Point> expected = Arrays.asList(
                new Point(0, 2),
                new Point(-1, -1),
                new Point(1, -1)
        );

        assertThat(ImageProcessing.sortPointsClockwise(input), equalTo(expected));

    }


    @Test
    public void calculateAngle_whenNullIsPassed() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Method argument cannot be null");
        ImageProcessing.calculateAngle(null, new Point(2, 1));
    }

    private static Object[] calculateAngleTestData() {
        return new Object[] {
                new Object[] {new Point(0,0), new Point(1,1), 45.0},
                new Object[] {new Point(0,0), new Point(-1,-1), 225.0},
                new Object[] {new Point(0,0), new Point(0,0), 0.0},
                new Object[] {new Point(0,0), new Point(1,-1), 315.0},
        };
    }

    @Test
    @Parameters(method = "calculateAngleTestData")
    public void calculateAngle_whenValidPointsArePassed(Point p1, Point p2, double expected) {
        assertThat(ImageProcessing.calculateAngle(p1, p2), equalTo(expected));
    }


}
