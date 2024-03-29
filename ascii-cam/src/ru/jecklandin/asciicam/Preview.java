package ru.jecklandin.asciicam;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Build;
import android.text.Layout;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;



class Preview extends SurfaceView implements SurfaceHolder.Callback {
	static boolean camOpen;
    SurfaceHolder mHolder;
    Camera mCamera;
    AsciiCamera cam;
    Preview(AsciiCamera context, Camera caam) {
        super(context);  
        cam = context;
        mCamera = caam;
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
        if (mCamera == null)	 {
        	mCamera = Camera.open();
        	if (mCamera == null) {
        		cam.restartApp();
        		return;
        	}
        }
           mCamera.setPreviewDisplay(holder);
        } catch (IOException exception) {
            mCamera.release();
            mCamera = null;
        } catch (RuntimeException e) {
        	cam.restartApp();
        	return;
        }
    }
   
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        // Because the CameraDevice object is not a shared resource, it's very
        // important to release it when the activity is paused. 
        //mCamera.stopPreview();
        //mCamera.release();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
    	 
    	Camera.Parameters parameters = null;
    	if (mCamera != null ) {
    		try {
    			parameters = mCamera.getParameters();
    		} catch (RuntimeException e) {
				cam.restartApp();
				return;
			}
    	} else {
    		cam.restartApp();
    		return;
    	}

    	List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
		if (sizes == null) {
			parameters.setPreviewSize(w, h);
		} else {
			Camera.Size rightSize = null;
    		int minDiff = Integer.MAX_VALUE;
    		for (Camera.Size size : sizes) {
    			int d = w - size.width;
    			if (d > 0 && d < minDiff) {
    				minDiff = d;
    				rightSize = size;
    			}
    		}
    		parameters.setPreviewSize(rightSize.width, rightSize.height);
		}
    	
        parameters.setPreviewSize(w, h);
        try {
        	mCamera.setParameters(parameters);
        } catch (Exception e) {
        	e.printStackTrace();
        	AsciiCamera.s_instance.restartApp();
        }
        
        mCamera.startPreview();
    }
    
	public static void setcamOpen(boolean b) {
		camOpen = b;
	}

}