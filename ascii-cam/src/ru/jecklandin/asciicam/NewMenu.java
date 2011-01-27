package ru.jecklandin.asciicam;

import com.flurry.android.FlurryAgent;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewStub.OnInflateListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;

public class NewMenu extends Activity implements OnClickListener {

	private Button mSizeBtn;
	private Button mColorBtn;
	private Button mMoreBtn;
	
	private LinearLayout mSizeLay;
	private LinearLayout mColorLay;
	private LinearLayout mMoreLay;
	
	private AsciiCamera.Facade mFacade;
	
	private SeekBar m_textSizeSeek;
	private CheckBox m_checkInvert;
	private Spinner m_imsizeSpinner;
	private RadioButton m_gsRadio;
	private RadioButton m_colRadio;
	private CheckBox m_bwCheck;
	private Spinner m_quaSpinner;
	private Button m_butPic;
	private Button m_butText;
	private Button m_butReset;
	private Button m_butAbout;
	private Button m_share;
	
	private boolean mMayAction = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        
		setContentView(R.layout.newmenu);

//		mFacade = AsciiCamera.s_instance.getFacade();
		
		mSizeBtn = (Button) findViewById(R.id.size_btn);
		mColorBtn = (Button) findViewById(R.id.color_btn);
		mMoreBtn = (Button) findViewById(R.id.more_btn);
		
		mSizeLay = (LinearLayout) findViewById(R.id.include02);
		mColorLay = (LinearLayout) findViewById(R.id.include01);
		mMoreLay = (LinearLayout) findViewById(R.id.include03);

        m_imsizeSpinner = (Spinner) findViewById(R.id.spinner_resolution);
        m_textSizeSeek = (SeekBar) findViewById(R.id.seek_ratio);
    	m_gsRadio = (RadioButton) findViewById(R.id.radio_gs);
    	m_colRadio = (RadioButton) findViewById(R.id.radio_color);
    	m_bwCheck = (CheckBox) findViewById(R.id.check_bw);
    	m_checkInvert = (CheckBox) findViewById(R.id.check_invert);
    	m_butPic = (Button) findViewById(R.id.save_pic);
    	m_butText = (Button) findViewById(R.id.save_text);
    	m_butReset = (Button) findViewById(R.id.btn_reset); 
    	m_butAbout = (Button) findViewById(R.id.about_btn);
    	m_share = (Button) findViewById(R.id.share);
		
		mSizeBtn.setOnClickListener(this);
		mColorBtn.setOnClickListener(this);
		mMoreBtn.setOnClickListener(this);
	}
	
	@Override  
	protected void onStart() {
		
		hideAll();
		
		mMayAction = false;
		m_textSizeSeek.setProgress(10);
//		m_textSizeSeek.setProgress(mFacade.getTextSize()*10);
//		m_imsizeSpinner.setSelection(3);
//		BitmapSize bms[] = AsciiCamera.s_availableSizes;
//		for (int i=0; i<bms.length;++i) {
//			if (bms[i].equals(AsciiCamera.s_bitmapSize)) {
//				m_imsizeSpinner.setSelection(i);
//			}
//		}
//
//		if (AsciiCamera.s_grayscale) {
//			m_gsRadio.setChecked(true);  
//		} else { 
//			m_colRadio.setChecked(true);
//		}
//		
//		m_checkInvert.setChecked(AsciiCamera.s_inverted);
//		m_bwCheck.setChecked(AsciiCamera.s_bw);
//		
//		mMayAction = true;
//
//		FlurryAgent.onStartSession(this, AsciiCamera.FLURRY_KEY);
		super.onStart();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		
		//main buttons
		
		case R.id.size_btn:
			hideAll();
			mSizeLay.setVisibility(View.VISIBLE);
			break;
		case R.id.color_btn:
			hideAll();
			mColorLay.setVisibility(View.VISIBLE);
			break;
		case R.id.more_btn:
			hideAll();
			mMoreLay.setVisibility(View.VISIBLE);
			break;
			
		//particular buttons
			
		default:
			break;
		}
	}
	
	private void hideAll() {
		mSizeLay.setVisibility(View.GONE);
		mColorLay.setVisibility(View.GONE);
		mMoreLay.setVisibility(View.GONE);
	}

	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}
	
    @Override
    protected void onStop() {
    	FlurryAgent.onEndSession(this);
        super.onStop();
    }
	
	private void setListeners() {
		
	}

}
