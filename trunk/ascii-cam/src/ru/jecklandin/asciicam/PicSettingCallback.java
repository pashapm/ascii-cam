package ru.jecklandin.asciicam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.util.Log;


class PicSettingCallback implements PictureCallback {
	private AsciiCamera m_asciiCam;
	PicSettingCallback(AsciiCamera  v) {
		this.m_asciiCam = v;
	}  
	
	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		if (AsciiCamera.s_defaultBitmap != null) {
			AsciiCamera.s_defaultBitmap.recycle();
		}
		AsciiCamera.s_defaultBitmap = null;
		BitmapFactory.Options opts = new Options();
		opts.inPreferredConfig = Config.RGB_565;
		m_asciiCam.convertBitmapAsync(BitmapFactory. decodeByteArray(data, 0, data.length, opts)
				,new BitmapSize(AsciiCamera.CONV_WIDTH, AsciiCamera.CONV_HEIGHT));
	}
	   
	
}

