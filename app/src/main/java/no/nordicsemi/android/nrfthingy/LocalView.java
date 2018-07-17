package no.nordicsemi.android.nrfthingy;


import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by dongsung on 2018-01-25.
 */

//MHE//motionfragment에서 사용되는 카메라 부분입니다.
public class LocalView extends SurfaceView implements SurfaceHolder.Callback {
    Camera mCamera = null;
    SurfaceHolder mHolder = null;
    MediaRecorder mRecoder = null;
    boolean isFrontCamera = true;
    int mWidth = 0;
    int mHeight = 0;
    byte[] callbackBuffer;
    public List<Camera.Size> prSupportedPreviewSizes;
    private Camera.Size prPreviewSize;

    LocalView(Context context){
        super(context);
        mCamera = Camera.open();
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        prSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
    }

    private Camera openCamera (int facing) {
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

        cameraCount = Camera.getNumberOfCameras();
        for (int cameraId = 0; cameraId < cameraCount; cameraId++){
            Camera.getCameraInfo(cameraId, cameraInfo);
            if (cameraInfo.facing == facing){
                try{
                    cam = Camera.open(cameraId);
                    break;
                } catch (RuntimeException e){
                    e.printStackTrace();
                }
            }
        }

        return cam;
    }

    public void startCamera(int facing) {
        mCamera = openCamera(facing);
        if (mCamera == null){
            mCamera = Camera.open();
        }
        try {
            Camera.Parameters parameters = mCamera.getParameters();

            parameters.set("orientation", "portrait");
            mCamera.setDisplayOrientation(90);
            parameters.setRotation(90);

            mCamera.setParameters(parameters);

            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (IOException e) {
        }
    }

    public void closeCamera() {
        if(mCamera != null){
            mCamera.stopPreview();
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.release();
            mCamera = null;
        }
    }



    public void startRecord() {
        Log.d("LocalView", "startRecording");

        File tempDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MHE_VIDEO");
        if(!tempDir.exists())
            tempDir.mkdirs();   // mhevideo 디렉터리를 만들어준다

        String time = new SimpleDateFormat("MMdd_HHmmss").format(new Date(System.currentTimeMillis()));
        String path = tempDir+ "/Video_" +time +".mp4";

        mCamera.stopPreview();
        mCamera.unlock();
        mRecoder = new MediaRecorder();
        mRecoder.setCamera(mCamera);
        mRecoder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecoder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mRecoder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecoder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecoder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mRecoder.setOutputFile(path);
        mRecoder.setVideoSize(1280, 720);
        mRecoder.setVideoFrameRate(60);
        mRecoder.setPreviewDisplay(mHolder.getSurface());

        try {
            mRecoder.prepare();
        } catch (IllegalStateException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mRecoder.start();
    }

    public void stopRecord() {
        Log.d("LocalView", "stopRecording");
        if (mRecoder != null) {
            mRecoder.stop();
            mRecoder.release();
        }
    }

    public void surfaceCreated(SurfaceHolder holder){
        Log.d("LocalView", "surfaceCreated");
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT) {
                parameters.set("orientation", "portrait");
                mCamera.setDisplayOrientation(90);
                parameters.setRotation(90);
            } else {
                parameters.set("orientation", "landscape");
                mCamera.setDisplayOrientation(0);
                parameters.setRotation(0);
            }
            mCamera.setParameters(parameters);

            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h){
        Log.d("LocalView", "surfaceChanged");
        try{
            mCamera.stopPreview();
        } catch (Exception e){
        }
        if(mCamera != null){
            mWidth = w;
            mHeight = h;
            Camera.Parameters parameters = mCamera.getParameters();
            List<String> focusModes = parameters.getSupportedFocusModes();
            if(focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)){
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            List<Camera.Size> allSizes = parameters.getSupportedPreviewSizes();
            Camera.Size size = allSizes.get(0);
            for (int i =0;i<allSizes.size();i++){
                if(allSizes.get(i).width > size.width){
                    size = allSizes.get(i);
                }
            }
//            parameters.setPreviewSize(size.width, size.height);
            parameters.setPreviewSize(prPreviewSize.width, prPreviewSize.height);

            mCamera.setDisplayOrientation(90);
            parameters.setRotation(90);
//            callbackBuffer = null;
//            callbackBuffer = new byte[parameters.getPreviewSize().width
//                    *parameters.getPreviewSize().height
//                    * ImageFormat.getBitsPerPixel(parameters.getPreviewFormat())/8];
//
//            mCamera.addCallbackBuffer(callbackBuffer);
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e)
            {

            }
//            mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
//                public void onPreviewFrame(byte[] data, Camera camera){
//                    Log.d("LocalView", "onPreviewFrame: ");
//                }
//            });
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder){
        Log.d("LocalView", "surfaceDestroyed: ");
        if (mCamera != null) {
            mCamera.stopPreview();
            //mCamera.release();
            //mCamera = null;
        }
    }

    private Camera.Size getBestPreviewSize(int width, int height)
    {
        Camera.Size result=null;
        Camera.Parameters p = mCamera.getParameters();
        for (Camera.Size size : p.getSupportedPreviewSizes()) {
            if (size.width<=width && size.height<=height) {
                if (result==null) {
                    result=size;
                } else {
                    int resultArea=result.width*result.height;
                    int newArea=size.width*size.height;

                    if (newArea>resultArea) {
                        result=size;
                    }
                }
            }
        }
        return result;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.v("jyp@@@","onMeasure()");

        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        Log.v("jyp@@@","width: "+width);
        Log.v("jyp@@@","height: "+height);
        setMeasuredDimension(width, height);

        if (prSupportedPreviewSizes != null) {
            prPreviewSize = getOptimalPreviewSize(prSupportedPreviewSizes, width, height);
            Log.v("jyp@@@","prPreviewSize.width: "+prPreviewSize.width);
            Log.v("jyp@@@","prPreviewSize.height: "+prPreviewSize.height);
        }
    }

    public Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {

        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null)
            return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;

            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        return optimalSize;
    }

}