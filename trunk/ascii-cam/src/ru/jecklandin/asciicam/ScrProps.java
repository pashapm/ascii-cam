package ru.jecklandin.asciicam;


import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class ScrProps {

	public static int screenHeight;
	public static int screenWidth;

	public static DisplayMetrics mMetrics = new DisplayMetrics();
	
	public static void initialize(Activity ctx) {
		Display disp = ((WindowManager) ctx.getSystemService(
				android.content.Context.WINDOW_SERVICE)).getDefaultDisplay();
		ScrProps.screenHeight = disp.getHeight();
		ScrProps.screenWidth = disp.getWidth();
		ctx.getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
	}
	
	public static int scale(int p) {
		return (int) (p*mMetrics.density);
	}
}
