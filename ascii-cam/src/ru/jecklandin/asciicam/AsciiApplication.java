package ru.jecklandin.asciicam;

import android.app.Application;
import android.graphics.Typeface;

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
	}
	
	public Typeface getArtTypeface() {
		return mArtTypeface;
	}
	
}
