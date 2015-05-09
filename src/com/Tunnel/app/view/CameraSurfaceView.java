package com.Tunnel.app.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.Tunnel.app.activity.ImageCropActivity;
import com.Tunnel.app.dialog.ImageSaveDialog;
import com.Tunnel.app.util.BitmapUtil;
import com.Tunnel.app.util.GlobalSwitch;

import java.io.IOException;
import java.util.List;

/**
 * Created by Yang Yupeng on 2014/8/5.
 */
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback{

    private final static String TAG = "Tunnel.CameraSurfaceView";
    private Context context;
    private Camera camera;
    private Camera.ShutterCallback mShutterCallback = null;

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        getHolder().addCallback(this);
    }

    public Camera getCamera()
    {
        return camera;
    }

    public void autoFocus()
    {
        try {
            camera.autoFocus(autoFocusCallback);
        }
        catch (Exception e)
        {
            //autoFocus failed. do nothing here...
        }
    }

    private Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (camera != null){
                camera.takePicture(mShutterCallback, null, mPictureCallback);
            }
        }
    };

    public void setShutterCallback(Camera.ShutterCallback callback)
    {
        mShutterCallback = callback;
    }


    private final Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
                Bitmap rawBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, null);
                Matrix m = new Matrix();
                m.setRotate(getPreviewDegree((Activity)context));
                Bitmap bitmap = Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.getWidth(), rawBitmap.getHeight(), m, false);
                rawBitmap.recycle();

                if (!GlobalSwitch.bCorrectionMode) {
                    ImageSaveDialog.create(context, bitmap, camera).show();
                }
                else {
                    String imagePath = BitmapUtil.saveTempBitmapCache(bitmap);
                    Intent intent = new Intent(context, ImageCropActivity.class);
                    intent.putExtra("ImagePath", imagePath);
                    context.startActivity(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera = Camera.open();
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            camera.release();
            camera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Camera.Parameters param = camera.getParameters();
        param.setPictureFormat(ImageFormat.JPEG);

//        Log.d(TAG, String.format("Surface view size: (%d-%d) rate: %f", height, width, (float)height / width));
//
//        for (Camera.Size size:param.getSupportedPreviewSizes())
//        {
//            Log.d(TAG, String.format("Supported Preview size: (%d-%d) rate: %f", size.width, size.height, (float)size.width / size.height));
//        }
        Camera.Size size = getOptimalPreviewSize(param.getSupportedPreviewSizes(), height, width); //此处交换宽高，因为锁定了竖屏
        //Log.d(TAG, String.format("Optimal Preview size: (%d-%d)", size.width, size.height));
        param.setPreviewSize(size.width, size.height);


//        for (Camera.Size size1:param.getSupportedPictureSizes())
//        {
//            Log.d(TAG, String.format("Supported Picture size: (%d-%d) rate: %f", size1.width, size1.height, (float)size1.width / size1.height));
//        }
        size = getOptimalPictureSize(param.getSupportedPictureSizes(), size.width, size.height); //此处交换宽高，因为锁定了竖屏
        //Log.d(TAG, String.format("Optimal Picture size: (%d-%d)", size.width, size.height));
        param.setPictureSize(size.width, size.height);

        camera.setDisplayOrientation(getPreviewDegree((Activity)context));
        camera.setParameters(param);
        camera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        if(camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
        }
        camera = null;
    }

    public static Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h)
    {
        final double ASPECT_TOLERANCE = 0.2;
        double targetRatio = (double) w / h;
        if (sizes == null) {
            return null;
        }
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes)
        {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff)
            {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        // Cannot find the one match the aspect ratio, ignore the
        // requirement
        if (optimalSize == null)
        {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff)
                {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    private Camera.Size getOptimalPictureSize(List<Camera.Size> sizes, int w, int h)
    {
        final double ASPECT_TOLERANCE = 0.2;
        double targetRatio = (double) w / h;
        if (sizes == null) {
            return null;
        }
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes)
        {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (size.height > targetHeight && (size.height - targetHeight) < minDiff)
            {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        // Cannot find the one match the aspect ratio, ignore the
        // requirement
        if (optimalSize == null)
        {
            targetHeight = h;

            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff)
                {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public static int getPreviewDegree(Activity activity) {
        int degree[] = {90, 0, 270, 180};
        return degree[activity.getWindowManager().getDefaultDisplay().getRotation()];
    }
}
