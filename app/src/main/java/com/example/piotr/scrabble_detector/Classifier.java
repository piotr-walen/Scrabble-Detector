package com.example.piotr.scrabble_detector;

/**
 * Created by piotr on 1/11/18.
 */

public interface Classifier {
    String name();

    Classification recognize(final float[] pixels);
}