package ru.jecklandin.asciicam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
		m_asciiCam.convertBitmapAsync(BitmapFactory.decodeByteArray(data, 0, data.length));
		//m_asciiCam.convertBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
	}
	
	
}

