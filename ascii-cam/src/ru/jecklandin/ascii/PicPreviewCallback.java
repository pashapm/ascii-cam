package ru.jecklandin.ascii;

import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;

class PicPreviewCallback implements PreviewCallback {

	boolean print = false;
	
	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		//data 320x240
		if (print) {
			
			Log.d("P", "AAAAAAAAAAAAAAAAAAA"+data.length);
			byte[] data1 =  new byte[data.length];
		System.arraycopy(data, 0, data1, 0, data.length);
			
		StringBuffer buf = new StringBuffer();
		int offset = 0;
		for (int i=0; i<data1.length; i++) {
			if (i%480 == 0) {
				buf.append("\n");
			} else {
				buf.append(data1[i]);
			}
		}
		
		
			Log.d("P", buf.toString());
			print = false;
			camera.setPreviewCallback(null);
		}
		
		
	}
	
}
