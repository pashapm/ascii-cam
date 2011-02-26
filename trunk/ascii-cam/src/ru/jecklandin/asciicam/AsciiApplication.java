package ru.jecklandin.asciicam;

import java.io.File;

import android.app.Application;
import android.graphics.Typeface;
import android.os.Environment;
import android.util.Log;

public class AsciiApplication extends Application {

	private static AsciiApplication sInstance;
	
	private Typeface mArtTypeface;
	
	public static AsciiApplication getInstance() {
		return sInstance;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		AsciiApplication.sInstance = this;
		mArtTypeface = Typeface.createFromAsset(getAssets(), "helsinki.ttf");
		
		AsciiCamera.SAVE_DIR = Environment.getExternalStorageDirectory()+"/asciicamera/";
		Log.d("!!!!", AsciiCamera.SAVE_DIR);
        File f = new File(AsciiCamera.SAVE_DIR);
        if (!f.exists())    
        	f.mkdir();  
	}
	
	public Typeface getArtTypeface() {
		return mArtTypeface;
	}
	
}
