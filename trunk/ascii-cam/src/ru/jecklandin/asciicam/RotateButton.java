package ru.jecklandin.asciicam;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.Button;

public class RotateButton extends Button {

//	private static Set<RotateButton> mButtonsToUpdate = new HashSet<RotateButton>();
//	private static Thread mUpdatingThread = new Thread(new Runnable() {
//		
//		@Override
//		public void run() {
//			
//		}
//	});
	
	private Matrix mMatrix = new Matrix();
	private final Matrix mSourceMatrix = new Matrix();
	private Bitmap mBitmap;
	private long mRotate;
	private Paint mPaint = new Paint();
	
	public RotateButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		Bitmap b = BitmapFactory.decodeResource(context.getResources(), R.drawable.glow);
		setBitmap(b);
		setGravity(Gravity.CENTER);
		mPaint.setAntiAlias(true);
		mPaint.setTypeface(AsciiApplication.getInstance().getArtTypeface());
	}
	 
	public void setBitmap(Bitmap b) {
		mBitmap = b;
		setMinimumHeight(mBitmap.getHeight());
		setMinimumWidth(mBitmap.getWidth());
		mPaint.setColor(Color.YELLOW);
	}
	
	@Override
	public void draw(Canvas canvas) { 
		
		PaintFlagsDrawFilter setfil = new PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG);
		canvas.setDrawFilter(setfil);  
		
		mMatrix.setRotate(mRotate, mBitmap.getWidth()/2, mBitmap.getHeight()/2);
		mRotate++;
		canvas.drawBitmap(mBitmap, mMatrix, mPaint);
		
		String text = getText().toString();
		Rect tr = new Rect();
		mPaint.getTextBounds(text, 0, text.length(), tr);
		
		int sh_x = (mBitmap.getWidth() - tr.right) / 2;
		int sh_y = tr.bottom;
		  
		canvas.drawText(text, sh_x, mBitmap.getHeight()/2+sh_y, mPaint);
		
		invalidate();
	}

}
