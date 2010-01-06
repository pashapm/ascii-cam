package ru.jecklandin.ascii;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Movie;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.widget.ImageView;

class AsciiViewer extends ImageView {
	
	Bitmap m_bitmap;
	String[] m_text;
	int m_textsize = 6;
	
	Matrix m_matrix = new Matrix();
	
	private boolean m_savePic = false;
	private Bitmap m_saveBitmap; // bitmap to save into a file
	
	private boolean m_wait = false;
	float m_waitProgress = 0;
	
	
	AsciiViewer(Context context) {
		super(context);
	}

	private String m_fname;
	
	void savePicture(String fname) {
		this.m_savePic = true;
		m_fname = fname;
		invalidate();
	}
	
	@Override
	 protected void onDraw(Canvas canvas) { 
		Paint p = new Paint();
		p.setTextSize(m_textsize);
		p.setTypeface(Typeface.MONOSPACE);
		p.setColor(AsciiCam.s_inverted ? Color.BLACK : Color.WHITE);
		
		m_matrix.reset();
		m_matrix.setRotate(-90,0,0);
		m_matrix.postTranslate(0, AsciiCam.s_screenHeight);
		
		Canvas canvas2 = null;
		if (m_savePic) {
			canvas2 = new Canvas();
			m_saveBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
			canvas2.setBitmap(m_saveBitmap);
			canvas = canvas2;
		}
		canvas.setMatrix(m_matrix);
			
		if (AsciiCam.s_inverted) {
			canvas.drawARGB(255, 255, 255, 255);
		} else {
			canvas.drawARGB(255, 0, 0, 0);
		}
	
		if (m_text!=null && !m_wait) {
			for (int i=0; i<m_text.length; ++i)
				canvas.drawText(m_text[i], 0, (float) ((m_textsize-2)*i), p);	
		}	
	
		if (m_wait) {
			m_matrix.reset();
			canvas.setMatrix(m_matrix);
			p.setTextSize(17);
			canvas.drawText("Asciization "+(int)(m_waitProgress*100)+"%", 
				   canvas.getWidth()/2 - p.measureText("Asciization 99%")/2, //sample text size
				   canvas.getHeight()/2, p);
		}
		
		if (m_savePic && !m_wait) {
			AsciiCam.savePicture(m_fname, m_saveBitmap);
			m_savePic = false;
			invalidate();
		}
	}

	void setWaiting(boolean b) {
		m_wait = b;
	}
}
