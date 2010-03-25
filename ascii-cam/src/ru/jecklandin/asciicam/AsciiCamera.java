package ru.jecklandin.asciicam;

import static android.provider.MediaStore.MediaColumns.DATA;

import static android.provider.MediaStore.MediaColumns.DISPLAY_NAME;
import static android.provider.MediaStore.MediaColumns.MIME_TYPE;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import ru.jecklandin.asciicam.PromptDialog.PromptDialogCallback;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.Media;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import static android.provider.MediaStore.Images.Media.*;

import com.nullwire.trace.ExceptionHandler;

public class AsciiCamera extends Activity { 
	
	//screen size
	static int s_screenHeight ;
	static int s_screenWidth ;
	
	//base bitmap size
	static int CONV_HEIGHT ;
	static int CONV_WIDTH ;
	
	//global settings 
	static BitmapSize[] s_availableSizes;
	static BitmapSize s_bitmapSize;
	
	static boolean s_inverted;
	static boolean s_grayscale; 
	static boolean s_colorized; 
	static boolean s_bw;
	
	static Bitmap s_defaultBitmap;
	
	public static String SAVE_DIR = "/sdcard/asciicamera/";
	
	Camera m_camera;
	AsciiViewer m_viewer;
	Preview m_preview;
	boolean m_photoMode = true;
	PicPreviewCallback m_prCallback = new PicPreviewCallback();
	private Facade m_facade;
	  
	private static String s_aboutString = "© Evgeny Balandin, 2010 \nbalandin.evgeny@gmail.com";
	
	public static AsciiCamera s_instance;
		
	/** Called when the activity is first created. */ 
    @Override     
    public void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 
        AsciiCamera.s_instance = this;
        
        Handler han = new Handler();
        
        //disabled for chinese
        ExceptionHandler.register(this, "http://android-exceptions-handler.appspot.com/exception.groovy",han);
        
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        Display disp = ((WindowManager) this.getSystemService(
				android.content.Context.WINDOW_SERVICE)).getDefaultDisplay();
        AsciiCamera.s_screenHeight = disp.getHeight();
        AsciiCamera.s_screenWidth = disp.getWidth();
        AsciiCamera.CONV_HEIGHT = AsciiCamera.s_screenHeight;
        AsciiCamera.CONV_WIDTH = AsciiCamera.s_screenWidth;
        
        m_viewer = new AsciiViewer(this);
        m_camera = Camera.open(); 
         
//        dont work on android > 2.0  
//        Parameters pp = m_camera.getParameters();
//        pp.setPictureSize(AsciiCamera.CONV_WIDTH , AsciiCamera.CONV_HEIGHT);
//        m_camera.setParameters(pp);
        
        m_preview = new Preview(this, m_camera);
        setContentView(m_preview);  
        
        File f = new File(AsciiCamera.SAVE_DIR);
        if (!f.exists())
        	f.mkdirs();
        

        //add button to the content view
        RelativeLayout lay = (RelativeLayout)View.inflate(this, R.layout.relbutton, null);
        ImageButton imb = (ImageButton) lay.findViewById(R.id.ShotButton);
        TextView warn = (TextView) lay.findViewById(R.id.warning);
        warn.setVisibility( AsciiCamera.isCardMounted() ? View.VISIBLE : View.GONE);
        imb.setOnClickListener(new ImageButton.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				makeShot();
			}
		});
          
        
        Button b = (Button) lay.findViewById(R.id.PickImage);
        b.setOnClickListener(new Button.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				pickFromAlbum();
			}
		});
        
        getWindow().addContentView(lay, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT)); 
   
        registerForContextMenu(m_viewer);
        reset();
    }  
    
    public Facade getFacade() {
    	//TODO INNER CLASSES
    	if (m_facade == null) { 
    		m_facade = new Facade();;
    	} 
    	return m_facade;
    }
   
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		 if (m_photoMode) { 

			 if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||  keyCode == KeyEvent.KEYCODE_CAMERA) {
				 makeShot();
				 return true;
			 } 
		 } else {
			 if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_BACK) {
				 m_photoMode = true;
				 restartApp();
				 return true;
			 } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)  {
				 m_viewer.changeTextSize(-1);
				 return true;
			 } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
				 m_viewer.changeTextSize(1);
				 return true;
			 } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
				 m_viewer.shift(0, 15);
			 } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
				 m_viewer.shift(0, -15);
			 } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
				 m_viewer.shift(-15, 0);
			 } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
				 m_viewer.shift(15, 0);
			 } else if (keyCode == KeyEvent.KEYCODE_MENU) {
				 Intent i = new Intent(this, SlidingMenu.class);
				 startActivity(i); 
				 return true;
			 }
			 
			 
			 m_viewer.invalidate();
		 }
		return true;
	}
	
	void makeShot() {
		m_photoMode = false;
		m_camera.takePicture(null, null, new PicSettingCallback(this));
		setContentView(m_viewer);
		m_camera.stopPreview(); 
	}
    
    public static void showAbout(Context ctx) {
    	AlertDialog.Builder d = new AlertDialog.Builder(ctx);
		d.setIcon(R.drawable.icon);
		TextView tw = new TextView(ctx);
		tw.setText(AsciiCamera.s_aboutString + "\n\n" + ctx.getString(R.string.credits));
		tw.setPadding(10, 10, 10, 10);  
		Linkify.addLinks(tw, Linkify.EMAIL_ADDRESSES);
		d.setView(tw);
		d.setTitle(ctx.getResources().getString(R.string.app_name));
		d.create();
		d.show();
    }
    
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
//		menu.getItem(0).setVisible(!m_photoMode);
//		menu.getItem(1).setVisible(!m_photoMode);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			savePicture();
			break;
		case 1:
			saveText();
			break;
		case 2:
			changeResolution();
			break;
		case 3:
			flipGrayscale();
			break;
		case 4:
			invert();
			break;
		case 5:
			m_viewer.changeTextSize(-1);
			break;
		case 6:
			m_viewer.changeTextSize(1);
			break;
		default:
		}
		return true;
	}
    
	protected void colorize(boolean col) {
		m_viewer.reset();
		AsciiCamera.s_colorized = col;
		AsciiCamera.s_grayscale = !col;
	}
	
    protected void flipGrayscale() {
    	m_viewer.reset();
		AsciiCamera.s_grayscale =! AsciiCamera.s_grayscale;
	}
    
    
	protected void invert() {
		m_viewer.reset();
    	AsciiCamera.s_inverted =! AsciiCamera.s_inverted;
  	}

	protected void changeResolution() {
		m_viewer.reset();
		//AsciiCamera.s_hiRes =! AsciiCamera.s_hiRes;
		convert();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		m_camera.release();	
	}  

	/**
	 * Saves aspect ratio
	 * @param b
	 * @param width
	 * @return
	 */
	private Bitmap resizeBitmap(Bitmap b) {
		float ratio = ((float)b.getWidth()) / ((float)b.getHeight());
		boolean horiz = ratio > 1.0f;
		
		if (horiz) { //simply resize
			int height = (int) (AsciiCamera.CONV_WIDTH / ratio);
			return resizeBitmap(b, AsciiCamera.CONV_WIDTH, height);
		} else { //rotate and resize
			int width = (int) (AsciiCamera.CONV_WIDTH * ratio);
			Bitmap rb = resizeBitmap(b, width, AsciiCamera.CONV_WIDTH);
			Matrix m = new Matrix();
			m.setRotate(-90, 0, 0);
			m.postTranslate(0, AsciiCamera.CONV_HEIGHT);
			return Bitmap.createBitmap(rb, 0, 0, rb.getWidth(), rb.getHeight(), m, false);
		}
	}
	
	private Bitmap resizeBitmap(Bitmap b, int w, int h) {
		if (b == null) {  //strange error
			restartApp();
		}
		
		Bitmap b1 = Bitmap.createScaledBitmap(b, w, h , false);
		if (AsciiCamera.s_defaultBitmap != b) {
			b.recycle();	
		}
		
		return b1;
	}
	
	public void convert() {
		convertBitmapAsync(AsciiCamera.s_defaultBitmap, 
				AsciiCamera.s_bitmapSize);
	}
	
	/**
	 * Sync. convert the given bitmap
	 */
	public void convertBitmap(Bitmap b) {
		Bitmap b1 = resizeBitmap(b);
		m_viewer.m_bitmap = b1;
		m_viewer.m_text = AsciiTools.convertBitmap(b1, new ProgressCallback() {
			
			@Override
			public void update(Float f) {
				m_viewer.m_waitProgress = f;
				m_viewer.invalidate();
			}
		});	
		m_viewer.postInvalidate();
	}
	
	/**
	 * Async. convert the given bitmap
	 */
	void convertBitmapAsync(Bitmap b, BitmapSize size) {
		if (AsciiCamera.s_colorized) {
			(new ConvertingColoredAsyncTask())
			.setSize(size.m_w, size.m_h)
			.execute(b);
		} else {
			(new ConvertingAsyncTask())
			.setSize(size.m_w, size.m_h)
			.execute(b);
		}
	}

	void savePicture() {
		if (AsciiCamera.isCardMounted()) {
			Toast.makeText(this, getString(R.string.unmount), Toast.LENGTH_SHORT).show();
			return;
		}
		Date d = Calendar.getInstance().getTime();
		String fname = d.getHours()+"-"+d.getMinutes()+"-"+d.getSeconds()+".png";
		m_viewer.savePicture(fname);
		
	}
	
	private String getGrayscaleText() {
		if (m_viewer.m_text == null) {
			return "";
		}
		StringBuffer buf = new StringBuffer();
		for (String s : m_viewer.m_text) {
			buf.append(s);
			buf.append("\n");
		}
		return buf.toString();
	}
	
	private String getColorizedText() {
		if (m_viewer.m_coloredText == null) {
			return "";
		}
		
		StringBuffer buf = new StringBuffer();
        try {
            InputStream is = getAssets().open("htmlbeg");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String text = new String(buffer);
            buf.append(text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
		
		
		for (int i=0; i<m_viewer.m_coloredText.length; ++i)  {
			for (int j=m_viewer.m_coloredText[i].length-1; j>0; --j) {
				ColoredValue cv = m_viewer.m_coloredText[i][j];
				
				Integer in = new Integer(cv.color);
				String symbol = cv.symbol;
				
			    buf.append("<font color=\"#");
			    if (!symbol.equals(" ")) {
			    	 buf.append(Integer.toHexString(in).substring(2));
			    } else {
			    	 buf.append("000");
			    }
			   
			    buf.append("\">");
			    buf.append(symbol.equals(" ") ? "0" : symbol);
			    buf.append("</font>");
			}
			buf.append("<br>");
		}
		
		try {
            InputStream is = getAssets().open("htmlend");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String text = new String(buffer);
            buf.append(text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
		
		return buf.toString();
	}
	
	private void saveText() {
		if (AsciiCamera.isCardMounted()) {
			Toast.makeText(this, getString(R.string.unmount),Toast.LENGTH_SHORT).show();
			return;
		}
		
		Date d = Calendar.getInstance().getTime();
		String fname = d.getHours()+"-"+d.getMinutes()+"-"+d.getSeconds() + 
			(AsciiCamera.s_colorized ? ".html" : ".txt");
		
		PromptDialog.prompt(fname, new PromptDialogCallback() {
			
			@Override
			public void ok(String s) {
				processSavingText(s);
				Intent i = new Intent(AsciiCamera.this, SlidingMenu.class);
				startActivity(i); 
			}

			@Override
			public void cancel() {
				Intent i = new Intent(AsciiCamera.this, SlidingMenu.class);
				startActivity(i); 
			}
		}, this);
	}
	
	void processSavingText(String fname) {
    	FileWriter fw = null;
		try {
			fw = new FileWriter(AsciiCamera.SAVE_DIR + fname);
			fw.write( AsciiCamera.s_colorized ? 
					getColorizedText() : 
					getGrayscaleText());
			
			Toast.makeText(AsciiCamera.this, fname + 
					getString(R.string.savedto) + " " +
					AsciiCamera.SAVE_DIR, 1000).show();
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fw!=null) {
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}	
			}
		}
	}
	
	//android:clearTaskOnLaunch is set, nevertheless sometimes onCreate isn't called... 
	//then need to restart
	void restartApp() {
		//crapy solution :(
		 Intent in = new Intent(this, AsciiCamera.class);
		 onStop();
		 startActivity(in);
		 finish();
	}
	
	void savePicture(String fname, final Bitmap b) {
			PromptDialog.prompt(fname, new PromptDialogCallback() {
			
			@Override
			public void ok(String s) {
				processSavingPicture(s, b);
				Intent i = new Intent(AsciiCamera.this, SlidingMenu.class);
				startActivity(i); 
			}

			@Override
			public void cancel() {
				Intent i = new Intent(AsciiCamera.this, SlidingMenu.class);
				startActivity(i); 
			}
		}, this);
	}
	
	private boolean processSavingPicture(String fname, Bitmap b) {
		if (b==null) {
			throw new IllegalArgumentException("Bitmap is null");
		}
		
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(AsciiCamera.SAVE_DIR + fname);
			b.compress(CompressFormat.PNG, 100, fos);
			
			ContentValues cv = new ContentValues();
		    cv.put(DISPLAY_NAME, fname);
		    cv.put(ORIENTATION, 90);
		    cv.put(MIME_TYPE, "image/png");
		    cv.put(DATA, AsciiCamera.SAVE_DIR + fname);
		    AsciiCamera.s_instance.getContentResolver().insert(Images.Media.EXTERNAL_CONTENT_URI, cv);

		    Toast.makeText(AsciiCamera.s_instance, fname + " " +
					AsciiCamera.s_instance.getString(R.string.savedto) + " " +
					AsciiCamera.SAVE_DIR, 1000).show();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}  finally {
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
	}
	
	
	public static boolean isCardMounted() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_SHARED);
	}
	
	/**
	 * Show dialog asking if user wants to send a report. Used by remote-stacktrace
	 * @param sendt
	 */
	public void showExceptionDialog(final Thread sendt) {
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setCancelable(false);
		b.setTitle(getString(R.string.app_name)+" had crashed last time");
		b.setMessage("Please press \"Send\" to submit report (~1Kb). \nPress \"Cancel\" to continue.");
		b.setPositiveButton("Send", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				sendt.start();
			}
		});
		b.setNegativeButton("Cancel", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ExceptionHandler.cleanStackTraces();
			}
		});
		b.create().show();
	}
	
	private void pickFromAlbum() {
		m_photoMode = false;
		setContentView(m_viewer);
		m_camera.stopPreview(); 
		Intent i = new Intent(Intent.ACTION_PICK);
		i.setType("image/*");
		startActivityForResult(i, 1); 
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		if (requestCode != 1) {
			return;
		}
		
		if (resultCode == Activity.RESULT_OK) {
			//fetch image
			try {
				Bitmap bm = Media.getBitmap(getContentResolver(), data.getData());
				if (AsciiCamera.s_defaultBitmap != null) {
					AsciiCamera.s_defaultBitmap.recycle();
				}
				AsciiCamera.s_defaultBitmap = null;
				AsciiCamera.s_instance.convertBitmapAsync(bm
						,new BitmapSize(AsciiCamera.CONV_WIDTH, AsciiCamera.CONV_HEIGHT));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (resultCode == Activity.RESULT_CANCELED){
			restartApp();
		}
	}
	
	private void reset() {
		m_viewer.m_textsize = AsciiViewer.DEFAUL_FONT;
		AsciiCamera.s_inverted = false;
		AsciiCamera.s_bw = false;

		SharedPreferences prefs = getSharedPreferences("asciicamera", MODE_PRIVATE);
		AsciiCamera.s_grayscale = prefs.getBoolean("gs", false); 
		AsciiCamera.s_colorized = prefs.getBoolean("col", true);
		
		AsciiCamera.s_bitmapSize = new BitmapSize(AsciiCamera.CONV_WIDTH, AsciiCamera.CONV_HEIGHT);
		AsciiCamera.s_availableSizes = getResolutions();
		m_viewer.reset();
		m_viewer.resetTextSize();
	}
	
	BitmapSize[] getResolutions() {
		float w = AsciiCamera.CONV_WIDTH;
		float h = AsciiCamera.CONV_HEIGHT;
		float ratio = w/h;
		float[] mp = {0.3f, 0.5f, 0.8f, 1f, 1.3f, 1.6f, 2f};
		BitmapSize[] vec = new BitmapSize[mp.length];
		for (int i=0; i<mp.length; ++i) {
			int nw = (int) (w * mp[i]);
			int nh = (int) (nw / ratio);
			vec[i] = new BitmapSize(nw, nh);
		}
		return vec;
	}
	
	// ==================  async stuff
	
	interface ProgressCallback {
		void update(Float f);
	}
	
	class ConvertingAsyncTask extends AsyncTask<Bitmap, Float, String[]> implements ProgressCallback {
		private int s_w;
		private int s_h;
		
		protected ConvertingAsyncTask setSize(int w, int h) {
			s_w = w;
			s_h = h;
			return this;
		}
		
        @Override
		protected void onProgressUpdate(Float... values) {
        	m_viewer.m_waitProgress = values[0];
        	m_viewer.postInvalidate();
 		}

		@Override
        protected void onPreExecute() {
			m_viewer.setWaiting(true);
        } 
		
		@Override 
		protected void onPostExecute(String[] result) {
			m_viewer.m_text = result;
			m_viewer.setWaiting(false);
			m_viewer.postInvalidate();
			AsciiCamera.s_bitmapSize = new BitmapSize(s_w, s_h);
		}

		@Override
		protected String[] doInBackground(Bitmap... params) {
			Bitmap b1;
			if (s_w == AsciiCamera.CONV_WIDTH && s_h == AsciiCamera.CONV_HEIGHT) {
				if (AsciiCamera.s_defaultBitmap == null)  {
					AsciiCamera.s_defaultBitmap = resizeBitmap(params[0]);
				}
				b1 = AsciiCamera.s_defaultBitmap;
			} else {
				b1 = resizeBitmap(params[0], s_w, s_h);
			}
			m_viewer.m_bitmap = b1;
			return AsciiTools.convertBitmap(b1, this);	
		}
		
		@Override
		public void update(Float f) {
			publishProgress(f);
		}
	}
	
	class ConvertingColoredAsyncTask extends AsyncTask<Bitmap, Float, ColoredValue[][]> implements ProgressCallback {
		private int s_w;
		private int s_h;
		
		protected ConvertingColoredAsyncTask setSize(int w, int h) {
			s_w = w;
			s_h = h;
			return this;
		}
		
        @Override
		protected void onProgressUpdate(Float... values) {
        	m_viewer.m_waitProgress = values[0];
        	m_viewer.postInvalidate();
 		}

		@Override
        protected void onPreExecute() {
			m_viewer.setWaiting(true);
        } 
		
		@Override 
		protected void onPostExecute(ColoredValue[][] result) {
			m_viewer.m_coloredText = result;
			m_viewer.setWaiting(false);
			m_viewer.postInvalidate();
			AsciiCamera.s_bitmapSize = new BitmapSize(s_w, s_h);
		}

		@Override
		protected ColoredValue[][] doInBackground(Bitmap... params) {
			Bitmap b1;
			if (s_w == AsciiCamera.CONV_WIDTH && s_h == AsciiCamera.CONV_HEIGHT) {
				if (AsciiCamera.s_defaultBitmap == null)  {
					AsciiCamera.s_defaultBitmap = resizeBitmap(params[0]);
				}
				b1 = AsciiCamera.s_defaultBitmap;
			} else {
				b1 = resizeBitmap(params[0], s_w, s_h);
			}
			m_viewer.m_bitmap = b1;
			return AsciiTools.convertColorBitmap(b1, this);	
		}
		
		@Override
		public void update(Float f) {
			publishProgress(f);
		}

	}
	
	class Facade  {
		
		public void setTextSize(int ts) {
			m_viewer.setTextSize(ts + 4);
		}
		
		public void setInverted(boolean inverted) {
			if (inverted != s_inverted) {
				invert();
		    	convert();
			}
		}
		
		public void setImageSize(BitmapSize bm) {
			if (! bm.equals(AsciiCamera.s_bitmapSize)) {
				AsciiCamera.s_bitmapSize = bm;
				convert();
			}
		}
		
		public void setGrayscale(boolean gs) {
			if (gs != AsciiCamera.s_grayscale) {
				flipGrayscale();
				convert();
			}
		}
		
		
		public void setBW(boolean bw) {
			if (bw != AsciiCamera.s_bw) {
				AsciiCamera.s_bw = bw;
				m_viewer.reset();
				convert();
			}
		}
		
		public void setColorized(boolean col) {
			if (AsciiCamera.s_colorized != col) {
				AsciiCamera.this.colorize(col);
				convert();
			}
		}
		
		public void reset() {
			AsciiCamera.this.reset();
			AsciiCamera.this.convert();
		}
		 
		public int getTextSize() {
			return m_viewer.m_textsize - 4;
		}
		
		public boolean isGrayscale() {
			return AsciiCamera.s_grayscale;
		}
		
		public boolean isInverted() {
			return AsciiCamera.s_inverted;
		}
		
		public BitmapSize[] getAvailableSizes() {
			return getResolutions();
		}
		
		public int getCurrentWidth() {
			return AsciiCamera.s_bitmapSize.m_w;
		}
		
		public int getCurrentHeight() {
			return AsciiCamera.s_bitmapSize.m_h;
		}
		
		public void saveText() {
			 AsciiCamera.this.saveText();
		}
		
		public void savePicture() {
			AsciiCamera.this.savePicture();
		}

		public void pickFromAlbum() {
			AsciiCamera.s_instance.pickFromAlbum();
		}

	}
	
} 







