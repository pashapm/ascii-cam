package ru.jecklandin.asciicam;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Movie;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

class AsciiViewer extends ImageView {
	
	static int DEFAUL_FONT = 6;
	enum ActionMode {SAVE, EDIT};
	ActionMode m_actionMode;
	
	Bitmap m_bitmap;
	String[] m_text;
	int m_textsize = AsciiViewer.DEFAUL_FONT;
	
	Matrix m_matrix = new Matrix();
	
	private boolean m_savePic = false;
	private Bitmap m_saveBitmap; // bitmap to save into a file
	
	private boolean m_wait = false;
	float m_waitProgress = 0;
	
	
	AsciiViewer(Context context) {
		super(context);
	}

	private String m_fname;
	
	private float m_shiftY = 0;
	private float m_shiftX = 0;
	
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
		p.setColor(AsciiCamera.s_inverted ? Color.BLACK : Color.WHITE);
		
		m_matrix.reset();
		m_matrix.setRotate(-90,0,0);
		m_matrix.postTranslate(0, AsciiCamera.s_screenHeight);
		m_matrix.postTranslate(m_shiftX, m_shiftY);
		
		Canvas canvas2 = null;
		if (m_savePic) {
			canvas2 = new Canvas();
			m_saveBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
			canvas2.setBitmap(m_saveBitmap);
			canvas = canvas2;
		}
		canvas.setMatrix(m_matrix);
			
		if (AsciiCamera.s_inverted) {
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
		} else if (m_text==null) {
			m_matrix.reset();
			canvas.setMatrix(m_matrix);
			p.setTextSize(17);
			canvas.drawText("Resizing the picture...", 
					   canvas.getWidth()/2 - p.measureText("Resizing the picture...")/2, //sample text size
					   canvas.getHeight()/2, p);
		}
		
		if (m_savePic && !m_wait) {
			AsciiCamera.savePicture(m_fname, m_saveBitmap);
			m_savePic = false;
			invalidate();
		}
	}
	
	/**
	 * Points of the image that was really pressed
	 */
	private float m_touchOffsetX = 0;
	private float m_touchOffsetY = 0;
	
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            	 m_touchOffsetX = x - m_shiftX;
            	 m_touchOffsetY = y - m_shiftY;
                break;
            case MotionEvent.ACTION_MOVE:
            	 shiftTo((x-m_touchOffsetX), (y-m_touchOffsetY));
                 break;

        }
        return true;
    }

	void setWaiting(boolean b) {
		m_wait = b;
	}
	
	/**
	 * Shifts the image
	 */
	void shift(float x, float y) {
		m_shiftX += x;
		m_shiftY += y;
		invalidate(); 
	}
	
	void shiftTo(float x, float y) {
		m_shiftX = x;
		m_shiftY = y;
		invalidate();
	}

	void reset() {
		m_shiftX = 0;
		m_shiftY = 0;
		m_textsize = AsciiViewer.DEFAUL_FONT; 
	}
	
	/**
	 * Reduce or increase text size.
	 * @param delta
	 */
	void changeTextSize(int delta) {
		m_textsize+=delta;
		if (m_textsize < 4 || m_textsize > 15) {
			 m_textsize = AsciiViewer.DEFAUL_FONT; 
	    } 
		invalidate();
	}
	
	void setTextSize(int size) {
		m_textsize = size;
		invalidate();
	}
	
	void showContextMenu(ActionMode mode) {
		m_actionMode = mode;
		showContextMenu();
	}
}
