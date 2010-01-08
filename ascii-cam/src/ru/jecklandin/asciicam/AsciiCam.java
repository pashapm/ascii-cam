package ru.jecklandin.asciicam;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import ru.jecklandin.asciicam.R;
import ru.jecklandin.asciicam.AsciiViewer.ActionMode;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Movie;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class AsciiCam extends Activity { 
	
	static int s_screenHeight = 0;
	static int s_screenWidth = 0;
	
	static int CONV_HEIGHT = 240;
	static int CONV_WIDTH = 320;

	static boolean s_inverted = false;
	static boolean s_grayscale = true; 
	static boolean s_hiRes = false;
	
	static String SAVE_DIR = "/sdcard/asciicam/";
	
	Camera m_camera;
	Bitmap s_bitmap;
	AsciiViewer m_viewer;
	Preview m_preview;
	boolean m_photoMode = true;
	PicPreviewCallback m_prCallback = new PicPreviewCallback();
	
	private static String s_aboutString = "© Evgeny Balandin, 2010 \n balandin.evgeny@gmail.com";

	
	/** Called when the activity is first created. */ 
    @Override 
    public void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        
        Display disp = ((WindowManager) this.getSystemService(
				android.content.Context.WINDOW_SERVICE)).getDefaultDisplay();
        AsciiCam.s_screenHeight = disp.getHeight();
        AsciiCam.s_screenWidth = disp.getWidth();
        AsciiCam.CONV_HEIGHT = AsciiCam.s_screenHeight;//* 3 / 4;
        AsciiCam.CONV_WIDTH = AsciiCam.s_screenWidth;// * 3 / 4;
        
        m_viewer = new AsciiViewer(this);
        m_camera = Camera.open(); 
        Parameters pp = m_camera.getParameters();
        pp.setPictureSize(AsciiCam.CONV_WIDTH , AsciiCam.CONV_HEIGHT);
        m_camera.setParameters(pp);
        
        m_preview = new Preview(this, m_camera);
        setContentView(m_preview);  
        
        File f = new File(AsciiCam.SAVE_DIR);
        if (!f.exists())
        	f.mkdir();
        
        registerForContextMenu(m_viewer);
    }  
 
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		 if (m_photoMode) { 
			 if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_CAMERA) {
				 m_photoMode = false;
				 m_camera.takePicture(null, null, new PicSettingCallback(this));
				 setContentView(m_viewer);
				 m_camera.stopPreview(); 
				 return true;
			 } else {
				 return super.onKeyDown(keyCode, event);
			 }
		 } else {
			 if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_CAMERA) {
				 m_photoMode = true;
				
				 //crapy solution :(
				 Intent in = new Intent(this, AsciiCam.class);
				 startActivity(in);
				 onStop();
				 finish();

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
			 }
			 m_viewer.invalidate();
		 }
		return  super.onKeyDown(keyCode, event);
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
				AlertDialog.Builder d = new AlertDialog.Builder(AsciiCam.this);
				d.setIcon(R.drawable.icon);
				d.setMessage(AsciiCam.s_aboutString);
				d.setTitle(getResources().getString(R.string.app_name));
				d.show();
				return false;
			}
		});
		return super.onCreateOptionsMenu(menu);
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
				menu.add(0, 2, 2, AsciiCam.s_hiRes? "Low-res" : "Hi-res");
				menu.add(0, 3, 3, AsciiCam.s_grayscale ? "Black & white" : "Grayscale");
				if (AsciiCam.s_grayscale) {
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
		AsciiCam.s_grayscale =! AsciiCam.s_grayscale;
		AsciiCam.s_inverted = false;
		convertBitmapAsync(m_viewer.m_bitmap);
	}

	protected void invert() {
		m_viewer.reset();
    	AsciiCam.s_inverted =! AsciiCam.s_inverted;
    	convertBitmapAsync(m_viewer.m_bitmap);
  	}

	protected void changeResolution() {
		m_viewer.reset();
		AsciiCam.s_hiRes =! AsciiCam.s_hiRes;
		convertBitmapAsync(m_viewer.m_bitmap);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		m_camera.release();
		Log.d("ACT", "onStop");
	}  

	private Bitmap resizeBitmap(Bitmap b) {
		Bitmap b1 = null;
		int s = AsciiCam.s_hiRes ? 1 : 2;
		if (b.getHeight()!=AsciiCam.CONV_HEIGHT / s || b.getWidth()!=AsciiCam.CONV_WIDTH  / s) {
			b1 = Bitmap.createScaledBitmap(b, AsciiCam.CONV_WIDTH / s, AsciiCam.CONV_HEIGHT / s, false);	
			b.recycle();
		} else {
			b1 = b;
		}
		return b1;
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
	void convertBitmapAsync(Bitmap b) {
		(new ConvertingAsyncTask()).execute(b);
	}

	void savePicture() {
		Date d = Calendar.getInstance().getTime();
		String fname = d.getHours()+"-"+d.getMinutes()+"-"+d.getSeconds();
		m_viewer.savePicture(fname);
		Toast.makeText(AsciiCam.this, fname+".png saved to "+AsciiCam.SAVE_DIR, 1000).show();
	}
	
	void saveText() {
		Date d = Calendar.getInstance().getTime();
		String fname = d.getHours()+"-"+d.getMinutes()+"-"+d.getSeconds();
    	FileWriter fw = null;
		try {
			fw = new FileWriter(AsciiCam.SAVE_DIR + fname + ".txt");
			StringBuffer buf = new StringBuffer();
			
			for (String s : m_viewer.m_text) {
				buf.append(s);
				buf.append("\n");
			}
			fw.write(buf.toString());
			Toast.makeText(AsciiCam.this, fname+".txt saved to "+AsciiCam.SAVE_DIR, 1000).show();
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
	
	public static boolean savePicture(String fname, Bitmap b) {
		if (b==null) {
			throw new IllegalArgumentException("Bitmap is null");
		}
		
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(AsciiCam.SAVE_DIR + fname + ".png");
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

	// ==================  async stuff
	
	interface ProgressCallback {
		void update(Float f);
	}
	
	class ConvertingAsyncTask extends AsyncTask<Bitmap, Float, String[]> implements ProgressCallback {

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
		}

		@Override
		protected String[] doInBackground(Bitmap... params) {
			Bitmap b1 = resizeBitmap(params[0]);
			m_viewer.m_bitmap = b1;
			return AsciiTools.convertBitmap(b1, this);	
		}
		
		@Override
		public void update(Float f) {
			publishProgress(f);
		}
	}
	
} 







