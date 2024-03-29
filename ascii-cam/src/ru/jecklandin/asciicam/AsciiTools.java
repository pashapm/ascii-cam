package ru.jecklandin.asciicam;

import java.util.HashMap;
import java.util.Map;

import ru.jecklandin.asciicam.AsciiCamera.ProgressCallback;
import android.graphics.Bitmap;

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
	
	public static ColoredValue[][] convertColorBitmap(Bitmap b, ProgressCallback progressCallback) {
		int sq_size = 4;
		int sq_width = b.getWidth()/sq_size; 
		int sq_height = b.getHeight()/sq_size;
		ColoredValue[][] symbols = new ColoredValue[sq_width][sq_height];
		
		for (int i=0; i<sq_width; i++) {
			for (int j=0; j<sq_height; j++) {
				symbols[i][j] = (AsciiTools.getColoredValue(b, i, j, sq_size));
			}
			if (progressCallback!=null && i%5==0) {
				progressCallback.update(((float)i)/((float)(sq_width-1)));
			}
		}
		return symbols;
	}
	
	public static String[] convertBitmap(Bitmap b, ProgressCallback progressCallback) {
		int sq_size = 3;
		int sq_width = b.getWidth()/sq_size; 
		int sq_height = b.getHeight()/sq_size;
		int[][] symbols = new int[sq_width][sq_height];
		
		for (int i=0; i<sq_width; i++) {
			for (int j=0; j<sq_height; j++) {
				if (AsciiCamera.s_grayscale) {
					symbols[i][j] = (int)(10*AsciiTools.getAverageValue(b, i, j, sq_size));
				} else {
					symbols[i][j] = (int)(10*AsciiTools.getNeatValue(b, i, j, sq_size));
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
				int s = (AsciiCamera.s_inverted && AsciiCamera.s_grayscale) ? (10-symbols[i][j]) : (symbols[i][j]);
				if (! AsciiCamera.s_bw) {
					buf.append(AsciiTools.symbolsMap.get(s));	
				} else {
					buf.append(AsciiTools.neatSymbolsMap.get(s));
				}
			}  
			ret[i] = buf.toString();
		}
		return ret;
	}
	
	private static ColoredValue getColoredValue(Bitmap bm, int x, int y, int sq_size) {
		int color = bm.getPixel(x*sq_size+sq_size/2, y*sq_size+sq_size/2);
		ColoredValue val = new ColoredValue();
		val.color = color;
		val.value = 10*AsciiTools.getValue(color);
		val.symbol = AsciiTools.symbolsMap.get((int)val.value);
		return val;	
	}
	
	private static float getValue(int color) {
		int r = (char)((color >> 16) & 0xff);
		int g = (char)((color >> 8) & 0xff);
		int b = (char)(color & 0xff);
		char gray = (char)(0.30f*r+0.59f*g+0.11f*b);
		return ((float)gray)/256f;
	}
	
	private static float getAverageValue(Bitmap bm, int x, int y, int sq_size) {
		int p = bm.getPixel(x*sq_size+sq_size/2, y*sq_size+sq_size/2);
		return AsciiTools.getValue(p);
	}
	
	private static int getNeatValue(Bitmap bm, int x, int y, int sq_size) {
		int res = 0;
		for (int i=0; i<sq_size;++i) 
			for (int j=0; j<sq_size;++j) {
				float val = AsciiTools.getValue(bm.getPixel(x*sq_size+i, y*sq_size+j));
				if (val > 0.6) {
					if (i==0 && j==1)
						res |= (1<<1); // 0010
					else if (i==1 && j==0)
						res |= (1<<2); // 0100
					else if (i==1 && j==1)
						res |= 1;      // 0001
					else if (i==0 && j==0) 
						res |= (1<<3); // 1000
				}
			}
		return res;
	}
}

class ColoredValue {
	float value;
	int color;
	String symbol;
}
