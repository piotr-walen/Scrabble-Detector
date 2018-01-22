package com.example.piotr.scrabble_detector;

import org.junit.Test;
import org.opencv.core.Point;
import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ImageProcessingSpecification {
    @Test
    public void findCenter_shouldOutputTheSamePoint_whenListWithOnePointIsPassed(){
        Point expected = new Point(-20,10);
        try {
            Method method = ImageProcessing.class.getMethod("findCenterPoint", List.class);
            method.setAccessible(true);
            Point actual = new Point();
            method.invoke(actual, Collections.singletonList(expected));
            assertEquals(expected, actual);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void findCenter_shouldOutputCenterPointOfLine_whenListWithTwoPointsIsPassed(){
        List<Point> points = Arrays.asList(
                new Point(-10,0),
                new Point(-2, 10)
        );
        Point expected = new Point(-6,5);
        try {
            Method method = ImageProcessing.class.getMethod("findCenterPoint", List.class);
            method.setAccessible(true);
            Point actual = new Point();
            method.invoke(actual, points);
            assertTrue(expected.equals(actual));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }


    }




}
