package com.example.piotr.scrabble_detector;

import org.junit.Test;
import org.opencv.core.Point;
import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ImageProcessingSpecification {
    @Test
    public void findCenter_shouldOutputTheSamePoint_whenListWithOnePointIsPassed(){
        Point point = new Point(-20,10);
        Point center = ImageProcessing.findCenterPoint(Collections.singletonList(point));
        assertEquals(center, point);
    }

    @Test
    public void findCenter_shouldOutputCenterPointOfLine_whenListWithTwoPointsIsPassed(){
        List<Point> points = Arrays.asList(
                new Point(-10,0),
                new Point(-2, 10)
        );
        Point expected = new Point(-6,5);
        Point center = ImageProcessing.findCenterPoint(points);
        assertTrue(expected.equals(center));
    }




}
