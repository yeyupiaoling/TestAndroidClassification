package com.yeyupiaoling.cv.mnn;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import com.yeyupiaoling.cv.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MNNDetection {
    private static final String TAG = MNNDetection.class.getName();

    private MNNNetInstance mNetInstance;
    private MNNNetInstance.Session mSession;
    private MNNNetInstance.Session.Tensor mInputTensor;
    private final MNNImageProcess.Config dataConfig;
    private Matrix imgData;
    private final int inputWidth = 300;
    private final int inputHeight = 300;
    private static final int NUM_THREADS = 4;

    /**
     * @param modelPath model path
     */
    public MNNDetection(String modelPath) throws Exception {
        dataConfig = new MNNImageProcess.Config();
        dataConfig.mean = new float[]{128.0f, 128.0f, 128.0f};
        dataConfig.normal = new float[]{0.0078125f, 0.0078125f, 0.0078125f};
        dataConfig.dest = MNNImageProcess.Format.RGB;
        imgData = new Matrix();

        File file = new File(modelPath);
        if (!file.exists()) {
            throw new Exception("model file is not exists!");
        }
        try {
            mNetInstance = MNNNetInstance.createFromFile(modelPath);
            MNNNetInstance.Config config = new MNNNetInstance.Config();
            config.numThread = NUM_THREADS;
            config.forwardType = MNNForwardType.FORWARD_CPU.type;
            mSession = mNetInstance.createSession(config);
            mInputTensor = mSession.getInput(null);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("load model fail!");
        }
    }

    public List<float[]> predictImage(String image_path) throws Exception {
        if (!new File(image_path).exists()) {
            throw new Exception("image file is not exists!");
        }
        FileInputStream fis = new FileInputStream(image_path);
        Bitmap bitmap = BitmapFactory.decodeStream(fis);
        return predictImage(bitmap);
    }

    public List<float[]> predictImage(Bitmap bitmap) throws Exception {
        return predict(bitmap);
    }


    // prediction
    private List<float[]> predict(Bitmap bmp) throws Exception {
        imgData.reset();
        imgData.postScale(inputWidth / (float) bmp.getWidth(), inputHeight / (float) bmp.getHeight());
        imgData.invert(imgData);
        MNNImageProcess.convertBitmap(bmp, mInputTensor, dataConfig, imgData);
        List<float[]> results = new ArrayList<>();

        try {
            mSession.run();
        } catch (Exception e) {
            throw new Exception("predict image fail! log:" + e);
        }
        MNNNetInstance.Session.Tensor output = mSession.getOutput(null);
        float[] result = output.getFloatData();
        results.add(result);
        Log.d(TAG, Arrays.toString(result));
        return results;
    }

    public void release(){
        if (mNetInstance != null) {
            mNetInstance.release();
            mNetInstance = null;
        }
    }
}
