package ru.jecklandin.ascii;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import ru.jecklandin.ascii.AsciiCam.ConvertingAsyncTask;
import ru.jecklandin.ascii.AsciiCam.ProgressCallback;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

public class AsciiTools {

	static Map<Integer, String> symbolsMap = new HashMap<Integer, String>() {{
		put(0, " ");
		put(1, ".");
		put(2, "\"");
		put(3, "^");
		put(4, "?");
		put(5, "o");
		put(6, "0");
		put(7, "O");
		put(8, "G");
		put(9, "8");
		put(10, "@");
	}};
	
	static Map<Integer, String> neatSymbolsMap = new HashMap<Integer, String>() {{
		put(0, " ");
		put(1, ".");
		put(2, ",");
		put(3, "_");
		put(4, "'");
		put(5, ")");
		put(6, "/");
		put(7, "J");
		put(8, "`");
		put(9, "\\");
		put(10, "(");
		put(11, "L");
		put(12, "^");
		put(13, "7");
		put(14, "P");
		put(15, "@");
	}};
	
	public static String[] convertBitmap(Bitmap b, ProgressCallback progressCallback) {
		int sq_size = 2;
		int sq_width = b.getWidth()/sq_size; 
		int sq_height = b.getHeight()/sq_size;
		int[][] symbols = new int[sq_width][sq_height];
		
		for (int i=0; i<sq_width; i++) {
			for (int j=0; j<sq_height; j++) {
				if (AsciiCam.s_grayscale) {
					symbols[i][j] = (int)(AsciiTools.getAverageValue(b, i, j, sq_size));
				} else {
					symbols[i][j] = (int)(AsciiTools.getNeatValue(b, i, j, sq_size));
				}
			}
			if (progressCallback!=null && i%5==0) {
				progressCallback.update(((float)i)/((float)(sq_width-1)));
			}
		}
		
		String[] ret = new String[sq_width];
		for (int i=0; i<sq_width; i++) {
			StringBuffer buf = new StringBuffer("");
			for (int j=sq_height-1; j>0; j--) {
				int s = (AsciiCam.s_inverted && AsciiCam.s_grayscale) ? (10-symbols[i][j]) : (symbols[i][j]);
				if (AsciiCam.s_grayscale) {
					buf.append(AsciiTools.symbolsMap.get(s));	
				} else {
					buf.append(AsciiTools.neatSymbolsMap.get(s));
				}
			}
			ret[i] = buf.toString();
		}
		return ret;
	}
	
	private static float getAverageValue(Bitmap bm, int x, int y, int sq_size) {
		float hsv[] = new float[3];
		float val_sum = 0;
		if (true) { //low quality
			Color.colorToHSV(bm.getPixel(x*sq_size+sq_size/2, y*sq_size+sq_size/2), hsv);
			return hsv[2]*10;	
		} else {
			for (int i=0; i<sq_size;++i) 
			for (int j=0; j<sq_size;++j) {
				Color.colorToHSV(bm.getPixel(x*sq_size+i, y*sq_size+j), hsv);
				val_sum += hsv[2];
			}
			return val_sum * 10 /(sq_size*sq_size);	
		}
	}
	
	private static int getNeatValue(Bitmap bm, int x, int y, int sq_size) {
		int res = 0;
		float hsv[] = new float[3];
		for (int i=0; i<sq_size;++i) 
			for (int j=0; j<sq_size;++j) {
				Color.colorToHSV(bm.getPixel(x*sq_size+i, y*sq_size+j), hsv);
				if (hsv[2] > 0.6) {
					if (i==0 && j==1)
						res |= (1<<1); // 0010
					else if (i==1 && j==0)
						res |= (1<<2); // 0100
					else if (i==1 && j==1)
						res |= 1;      // 0001
					else if (i==0 && j==0) 
						res |= (1<<3); // 1000
				}
				//Log.d("pix"+i+":"+j, ""+hsv[2]);
			}
		return res;
	}
	
		
}
