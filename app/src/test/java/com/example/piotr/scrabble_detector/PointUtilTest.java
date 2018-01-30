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
public class PointUtilTest {
    @Test
    public void findCenter_ListWithOnePointIsPassed() {
        Point expected = new Point(-20, 10);
        Point actual = PointUtil.findCenterPoint(Collections.singletonList(expected));
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void findCenter_ListWithTwoPointsIsPassed() {
        List<Point> points = Arrays.asList(
                new Point(-10, 0),
                new Point(-2, 10)
        );
        Point expected = new Point(-6, 5);
        Point actual = PointUtil.findCenterPoint(points);
        assertThat(actual, equalTo(expected));
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void findCenter_NullIsPassed() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Method argument cannot be null");
        PointUtil.findCenterPoint(null);
    }

    @Test
    public void sortPointsClockwise_NullIsPassed() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Method argument cannot be null");
        PointUtil.sortPointsClockwise(null);
    }


    @Test
    public void calculateAngle_NullIsPassed() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Method argument cannot be null");
        PointUtil.calculateAngle(null, new Point(2, 1));
    }

    private static Object[] calculateAngleTestData() {
        return new Object[]{
                new Object[]{new Point(0, 0), new Point(1, 1), 45.0},
                new Object[]{new Point(0, 0), new Point(-1, -1), 225.0},
                new Object[]{new Point(0, 0), new Point(0, 0), 0.0},
                new Object[]{new Point(0, 0), new Point(1, -1), 315.0}
        };
    }

    @Test
    @Parameters(method = "calculateAngleTestData")
    public void calculateAngle_ValidPointsArePassed(Point p1, Point p2, double expected) {
        assertThat(PointUtil.calculateAngle(p1, p2), equalTo(expected));
    }

    private static Object[] sortPointsClockwiseTestData() {
        return new Object[]{
                new List[]{
                        Arrays.asList(new Point(-1, -1)),
                        Arrays.asList(new Point(-1, -1))
                },

                new List[]{
                        Arrays.asList(new Point(0, 3), new Point(1, -1)),
                        Arrays.asList(new Point(1, -1), new Point(0, 3))
                },

                new List[]{
                        Arrays.asList(new Point(-1, -1), new Point(0, 3),
                                new Point(1, -1)),
                        Arrays.asList(new Point(1, -1), new Point(-1, -1),
                                new Point(0, 3))
                },

                new List[]{
                        Arrays.asList(new Point(-1, -1), new Point(1, 1),
                                new Point(0, 2), new Point(1, -1)),
                        Arrays.asList(new Point(1, -1), new Point(-1, -1),
                                new Point(0, 2), new Point(1, 1))
                },
        };
    }

    @Test
    @Parameters(method = "sortPointsClockwiseTestData")
    public void sortPointsClockwise_ValidPointsListIsPassed(List<Point> input, List<Point> expected) {
        assertThat(PointUtil.sortPointsClockwise(input), equalTo(expected));
    }


}
