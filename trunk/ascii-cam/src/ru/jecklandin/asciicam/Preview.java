package ru.jecklandin.asciicam;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Camera;
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
        	}
        	Log.d("AAAAA", "$$$$$$$$$$$$$$$$$NULL");
        }
           mCamera.setPreviewDisplay(holder);
        } catch (IOException exception) {
            mCamera.release();
            mCamera = null;
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
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(w, h);
        mCamera.setParameters(parameters);
        mCamera.startPreview();
    }

	public static void setcamOpen(boolean b) {
		camOpen = b;
		Log.d("%%%%%%", "CAM OPEN %%%%%%%%%%%%%%%%%^^^^^^");
		
	}

}