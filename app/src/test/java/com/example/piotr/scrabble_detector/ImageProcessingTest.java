package com.example.piotr.scrabble_detector;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opencv.core.Point;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ImageProcessingTest {
    @Test
    public void findCenter_shouldOutputTheSamePoint_whenListWithOnePointIsPassed(){
        Point expected = new Point(-20,10);
        Point actual = ImageProcessing.findCenterPoint(Collections.singletonList(expected));
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void findCenter_shouldOutputCenterPointOfLine_whenListWithTwoPointsIsPassed(){
        List<Point> points = Arrays.asList(
                new Point(-10,0),
                new Point(-2, 10)
        );
        Point expected = new Point(-6,5);
        Point actual = ImageProcessing.findCenterPoint(points);
        assertThat(actual, equalTo(expected));
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void findCenter_shouldThrowIllegalArgumentException_whenNullIsPassed() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("method argument cannot be null");
        ImageProcessing.findCenterPoint(null);
    }




}
