package com.example.piotr.scrabble_detector;

public interface Classifier {
    String name();

    Classification recognize(final float[] pixels);
}