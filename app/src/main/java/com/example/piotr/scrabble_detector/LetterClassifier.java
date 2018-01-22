package com.example.piotr.scrabble_detector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Vector;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Log;

import org.tensorflow.Operation;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;


public class LetterClassifier implements Classifier {

    private static final String TAG = "LetterClassifier";

    // Config values.
    private String inputName;
    private String outputName;
    private int inputSize;
    private int numClasses;
    // Pre-allocated buffers.
    private Vector<String> labels = new Vector<String>();

    private boolean logStats = false;

    private TensorFlowInferenceInterface inferenceInterface;
    private LetterClassifier() {
    }


    /**
     * Initializes a native TensorFlow session for classifying images.
     *
     * @param assetManager  The asset manager to be used to load assets.
     * @param modelFilename The filepath of the model GraphDef protocol buffer.
     * @param labelFilename The filepath of label file for classes.
     * @param inputSize     The input size. A square image of inputSize x inputSize is assumed.
     * @param inputName     The label of the image input node.
     * @param outputName    The label of the output node.
     * @throws IOException
     */
    public static Classifier create(
            AssetManager assetManager,
            String modelFilename,
            String labelFilename,
            int inputSize,
            String inputName,
            String outputName) {
        LetterClassifier c = new LetterClassifier();
        c.inputName = inputName;
        c.outputName = outputName;
        // Read the label names into memory.
        String actualFilename = labelFilename.split("file:///android_asset/")[1];
        Log.i(TAG, "Reading labels from: " + actualFilename);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(assetManager.open(actualFilename)));
            String line;
            while ((line = br.readLine()) != null) {
                c.labels.add(line);
            }
            br.close();
        } catch (IOException e) {
            throw new RuntimeException("Problem reading label file!", e);
        }
        c.inferenceInterface = new TensorFlowInferenceInterface(assetManager, modelFilename);
        // Ideally, inputSize could have been retrieved from the shape of the input operation.  Alas,
        // the placeholder node for input in the graphdef typically used does not specify a shape, so it
        // must be passed in as a parameter.
        c.inputSize = inputSize;

        // The shape of the output is [N, NUM_CLASSES], where N is the batch size.
        final Operation operation = c.inferenceInterface.graphOperation(outputName);
        c.numClasses = (int) operation.output(0).shape().size(1);
        Log.i(TAG, "Read " + c.labels.size() + " labels, output layer size is " + c.numClasses);

        // Pre-allocate buffers.
        return c;
    }

    @Override
    public Recognition recognizeImage(final Bitmap bitmap) {

        int[] intValues = loadBitmapToIntValues(bitmap);
        float[] floatValues = convertToFloatValues(intValues);
        feedGraph(floatValues,1);
        float[] outputs = getOutputs(1);
        return getRecognitions(outputs);
    }

    private float[] convertToFloatValues(int[] intValues) {
        float [] floatValues = new float[inputSize * inputSize * 3];
        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            floatValues[i * 3 + 0] = (float) (((val >> 16) & 0xFF)) / 255;
            floatValues[i * 3 + 1] = (float) (((val >> 8) & 0xFF)) / 255;
            floatValues[i * 3 + 2] = (float) ((val & 0xFF)) / 255;
        }
        return floatValues;
    }

    private int[] loadBitmapToIntValues(Bitmap bitmap) {
        // Preprocess the image data from 0-255 int to normalized float based
        // on the provided parameters.
        int[] intValues = new int[inputSize * inputSize];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        return intValues;
    }

    private void feedGraph(float[] floatValues, int batchSize) {
        // Copy the input data into TensorFlow.
        inferenceInterface.feed(inputName, floatValues, batchSize, inputSize, inputSize, 3);

    }

    private float[] getOutputs(int batchSize){
        float[] outputs = new float[numClasses*batchSize];
        String[] outputNames = new String[]{outputName};

        // Run the inference call.
        inferenceInterface.run(outputNames, logStats);
        // Copy the output Tensor back into the output array.
        inferenceInterface.fetch(outputName, outputs);

        return outputs;

    }

    @NonNull
    private Recognition getRecognitions(float[] outputs) {
        // Find the best classifications.

        Comparator<Recognition> comparator = new Comparator<Recognition>() {
            @Override
            public int compare(Recognition lhs, Recognition rhs) {
                // Intentionally reversed to put high confidence at the head of the queue.
                return Float.compare(rhs.getConfidence(), lhs.getConfidence());
            }
        };

        PriorityQueue<Recognition> pq = new PriorityQueue<>(3, comparator);

        for (int i = 0; i < outputs.length; ++i) {
            Recognition recognition = new Recognition("" + i,
                    labels.size() > i ? labels.get(i) : "unknown", outputs[i], null);
            pq.add(recognition);
        }

        return pq.poll();
    }

    @Override
    public List<Recognition> recognizeImages(List<Bitmap> images){
        int batchSize = images.size();
        int subArraySize = inputSize * inputSize * 3;
        float [] floatValues = new float[subArraySize * batchSize];
        for (int i = 0; i<images.size(); i++) {
            int[] subArray = loadBitmapToIntValues(images.get(i));
            System.arraycopy(convertToFloatValues(subArray),0,floatValues,i*subArraySize,subArraySize);
        }
        feedGraph(floatValues, batchSize);
        float[] outputs = getOutputs(batchSize);

        List<Recognition> recognitions = new ArrayList<>();
        for(int i = 0; i<batchSize; i++){
            float[] singleOutput = new float[numClasses];
            System.arraycopy(outputs,i*numClasses,singleOutput,0,numClasses);
            Recognition recognition = getRecognitions(singleOutput);
            recognitions.add(recognition);
        }

        return recognitions;
    }


    @Override
    public void enableStatLogging(boolean logStats) {
        this.logStats = logStats;
    }

    @Override
    public String getStatString() {
        return inferenceInterface.getStatString();
    }

    @Override
    public void close() {
        inferenceInterface.close();
    }
}
