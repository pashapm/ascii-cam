package ru.jecklandin.asciicam;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import ru.jecklandin.asciicam.AsciiTools.QUALITY;
import ru.jecklandin.asciicam.AsciiViewer.ActionMode;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

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
	static AsciiTools.QUALITY s_quality;
	
	static Bitmap s_defaultBitmap;
	
	public static String SAVE_DIR = "/sdcard/DCIM/asciicamera/";
	
	Camera m_camera;
	AsciiViewer m_viewer;
	Preview m_preview;
	boolean m_photoMode = true;
	PicPreviewCallback m_prCallback = new PicPreviewCallback();
	private Facade m_facade; 
	
	private static String s_aboutString = "© Evgeny Balandin, 2010 \n balandin.evgeny@gmail.com";
	
	public static AsciiCamera s_instance;
		
	/** Called when the activity is first created. */ 
    @Override 
    public void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 
        
        // :-|
        AsciiCamera.s_instance = this;
        
        Handler han = new Handler();
        ExceptionHandler.register(this, "http://android-exceptions-handler.appspot.com/exception.groovy",han);
        
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        Display disp = ((WindowManager) this.getSystemService(
				android.content.Context.WINDOW_SERVICE)).getDefaultDisplay();
        AsciiCamera.s_screenHeight = disp.getHeight();
        AsciiCamera.s_screenWidth = disp.getWidth();
        AsciiCamera.CONV_HEIGHT = AsciiCamera.s_screenHeight;//* 3 / 4;
        AsciiCamera.CONV_WIDTH = AsciiCamera.s_screenWidth;// * 3 / 4;
        
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
        LinearLayout lay = (LinearLayout)View.inflate(this, R.layout.button, null);
        ImageButton imb = (ImageButton) lay.findViewById(R.id.ShotButton);
        imb.setOnClickListener(new ImageButton.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				makeShot();
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
			 } else {
				 return super.onKeyDown(keyCode, event);
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
		return  super.onKeyDown(keyCode, event);
	}
	
	void makeShot() {
		m_photoMode = false;
		m_camera.takePicture(null, null, new PicSettingCallback(this));
		setContentView(m_viewer);
		m_camera.stopPreview(); 
	}
	
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem menuitem0 = menu.add(Menu.NONE, 0, Menu.NONE, "Save");
		menuitem0.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem arg0) {
				((AsciiViewer)m_viewer).showContextMenu(ActionMode.SAVE);
				return false;
			}
		});
		
		MenuItem menuitem1 = menu.add(Menu.NONE, 1, Menu.NONE, "Edit");
		menuitem1.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem arg0) {
				((AsciiViewer)m_viewer).showContextMenu(ActionMode.EDIT);
				return false;
			}
		});

		MenuItem menuitem2 = menu.add(Menu.NONE, 2, Menu.NONE, "About");
		menuitem2.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem arg0) {
				AsciiCamera.showAbout(AsciiCamera.this);
				return false;
			}
		});
		return super.onCreateOptionsMenu(menu);
	}
    
    public static void showAbout(Context ctx) {
    	AlertDialog.Builder d = new AlertDialog.Builder(ctx);
		d.setIcon(R.drawable.icon);
		d.setMessage(AsciiCamera.s_aboutString);
		d.setTitle(ctx.getResources().getString(R.string.app_name));
		d.create();
		d.show();
    }
    
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.getItem(0).setVisible(!m_photoMode);
		menu.getItem(1).setVisible(!m_photoMode);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
			super.onCreateContextMenu(menu, v, menuInfo);
			if (m_viewer.m_actionMode == ActionMode.SAVE) {
				menu.add(0, 0, 0, "As image");
				menu.add(0, 1, 1, "As text");
			} else if (m_viewer.m_actionMode == ActionMode.EDIT) {
				//menu.add(0, 2, 2, AsciiCamera.s_hiRes? "Low-res" : "Hi-res");
				menu.add(0, 3, 3, AsciiCamera.s_grayscale ? "Black & white" : "Grayscale");
				if (AsciiCamera.s_grayscale) {
					menu.add(0, 4, 4, "Invert");
				}
				menu.add(0, 5, 5, "Reduce font size (\"Volume down\" button)");
				menu.add(0, 6, 6, "Increase font size (\"Volume up\" button)");
			}
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
    
    protected void flipGrayscale() {
    	m_viewer.reset();
		AsciiCamera.s_grayscale =! AsciiCamera.s_grayscale;
		AsciiCamera.s_inverted = false;
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

	private Bitmap resizeBitmap(Bitmap b, int w, int h) {
		Bitmap b1 = Bitmap.createScaledBitmap(b, w, h , false);
		if (AsciiCamera.s_defaultBitmap != b) {
			b.recycle();	
		}
		
//		Bitmap b1 = null;
//		if (b.getHeight() != h || b.getWidth() != w ) {
//			b1 = Bitmap.createScaledBitmap(b, w, h , false);	
//			b.recycle();
//		} else {
//			b1 = b;
//		}
//		return b1;
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
		Bitmap b1 = resizeBitmap(b, AsciiCamera.CONV_WIDTH, AsciiCamera.CONV_HEIGHT);
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
		(new ConvertingAsyncTask())
			.setSize(size.m_w, size.m_h)
			.execute(b);
	}

	void savePicture() {
		if (AsciiCamera.isCardMounted()) {
			Toast.makeText(this, getString(R.string.unmount), Toast.LENGTH_SHORT).show();
			return;
		}
		Date d = Calendar.getInstance().getTime();
		String fname = d.getHours()+"-"+d.getMinutes()+"-"+d.getSeconds();
		m_viewer.savePicture(fname);
		Toast.makeText(AsciiCamera.this, fname+".png saved to "+AsciiCamera.SAVE_DIR, 1000).show();
	}
	 
	void saveText() {
		if (AsciiCamera.isCardMounted()) {
			Toast.makeText(this, getString(R.string.unmount),Toast.LENGTH_SHORT).show();
			return;
		}
		Date d = Calendar.getInstance().getTime();
		String fname = d.getHours()+"-"+d.getMinutes()+"-"+d.getSeconds();
    	FileWriter fw = null;
		try {
			fw = new FileWriter(AsciiCamera.SAVE_DIR + fname + ".txt");
			StringBuffer buf = new StringBuffer();
			
			for (String s : m_viewer.m_text) {
				buf.append(s);
				buf.append("\n");
			}
			fw.write(buf.toString());
			Toast.makeText(AsciiCamera.this, fname+".txt saved to "+AsciiCamera.SAVE_DIR, 1000).show();
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
	
	public static boolean savePicture(String fname, Bitmap b) {
		if (b==null) {
			throw new IllegalArgumentException("Bitmap is null");
		}
		
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(AsciiCamera.SAVE_DIR + fname + ".png");
			b.compress(CompressFormat.PNG, 100, fos);
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
	
	private void reset() {
		if (AsciiCamera.s_defaultBitmap != null) {
			AsciiCamera.s_defaultBitmap.recycle();
		}
		AsciiCamera.s_defaultBitmap = null;
		m_viewer.m_textsize = AsciiViewer.DEFAUL_FONT;
		AsciiCamera.s_inverted = false;
		AsciiCamera.s_grayscale = true; 
		AsciiCamera.s_quality = QUALITY.LOW;
		AsciiCamera.s_bitmapSize = new BitmapSize(AsciiCamera.CONV_WIDTH, AsciiCamera.CONV_HEIGHT);
		AsciiCamera.s_availableSizes = getResolutions();
		m_viewer.reset();
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
					AsciiCamera.s_defaultBitmap = resizeBitmap(params[0], s_w, s_h);
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
		
		public void setQuality(AsciiTools.QUALITY qua) {
			if (qua != AsciiCamera.s_quality) {
				AsciiCamera.s_quality = qua;
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
		
		public QUALITY getQuality() {
			return AsciiCamera.s_quality;
		}
		
		public void saveText() {
			 AsciiCamera.this.saveText();
		}
		
		public void savePicture() {
			AsciiCamera.this.savePicture();
		}
	}
	
} 







