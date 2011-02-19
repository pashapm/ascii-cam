package ru.jecklandin.asciicam;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.flurry.android.FlurryAgent;

public class NewMenu extends Activity implements OnClickListener, OnCheckedChangeListener {

	private RadioButton mSizeBtn;
	private RadioButton mColorBtn;
	private RadioButton mMoreBtn;
//	private RadioButton mDonateBtn;
	
	private LinearLayout mSizeLay;
	private LinearLayout mColorLay;
	private LinearLayout mMoreLay;
//	private LinearLayout mDonateLay;
	
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
	private Button m_donate;
	
	private boolean mMayAction = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        
        ScrProps.initialize(this);
        
		setContentView(R.layout.newmenu);

		mFacade = AsciiCamera.s_instance.getFacade();
		
		mSizeBtn = (RadioButton) findViewById(R.id.size_btn);
		mColorBtn = (RadioButton) findViewById(R.id.color_btn);
		mMoreBtn = (RadioButton) findViewById(R.id.more_btn);
//		mDonateBtn = (RadioButton) findViewById(R.id.donate_btn);
		
		mSizeLay = (LinearLayout) findViewById(R.id.include02);
		mColorLay = (LinearLayout) findViewById(R.id.include01);
		mMoreLay = (LinearLayout) findViewById(R.id.include03);
//		mDonateLay = (LinearLayout) findViewById(R.id.include04);

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
    	m_donate = (Button) findViewById(R.id.donate);
		
    	// typefaces
    	Typeface tf = AsciiApplication.getInstance().getArtTypeface();
    	Button[] btns = new Button[] {
    			m_butPic, m_butText, m_butReset, m_butAbout, m_share
    	};
    	for (Button b : btns) {
    		b.setTypeface(tf);
    	}
    	m_gsRadio.setTypeface(tf);
    	m_colRadio.setTypeface(tf);
    	m_bwCheck.setTypeface(tf);
    	m_checkInvert.setTypeface(tf);
    	((TextView) findViewById(R.id.res_label)).setTypeface(tf);
    	m_donate.setTypeface(tf);
    	 
    	m_donate.setOnClickListener(new OnClickListener() {
			   
			@Override
			public void onClick(View v) {
				FlurryAgent.onEvent("donate");
				String search = "http://market.android.com/details?id=ru.jecklandin.asciicamdonate";
				Intent i = new Intent();
				i.setData(Uri.parse(search));
				startActivity(i);
			}
		});
	}
	
	private void setListeners() {
		mSizeBtn.setOnCheckedChangeListener(this);
		mColorBtn.setOnCheckedChangeListener(this);
		mMoreBtn.setOnCheckedChangeListener(this);
//		mDonateBtn.setOnCheckedChangeListener(this);
		
		m_bwCheck.setOnCheckedChangeListener(this);
		m_checkInvert.setOnCheckedChangeListener(this);
		m_gsRadio.setOnCheckedChangeListener(this);
		m_colRadio.setOnCheckedChangeListener(this);
		
		m_butPic.setOnClickListener(this);
		m_butText.setOnClickListener(this);
		m_butReset.setOnClickListener(this);
		m_butAbout.setOnClickListener(this);
		m_share.setOnClickListener(this);
		
		m_textSizeSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
        	int progress = 0;
        	
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				AsciiCamera.s_instance.getFacade().setTextSize(this.progress);
			}
			 
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				this.progress = progress / 10;
			}
		});
		
        m_imsizeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
//				if ((m_imsizeSpinner.getTag() != null) 
//						&& (((Integer)m_imsizeSpinner.getTag()).intValue() != arg2)) {
//					mFacade.setImageSize(AsciiCamera.s_availableSizes[arg2]);
//					m_imsizeSpinner.setTag(arg2);
//				}
				   
				if (mMayAction && (last.equals(-1) || !last.equals(arg2))) {
					mFacade.setImageSize(AsciiCamera.s_availableSizes[arg2]);
					last = arg2;
				}
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
	}
	
	Integer last = -1;
	
	@Override  
	protected void onStart() {
		hideAll();
		FlurryAgent.onStartSession(this, AsciiCamera.FLURRY_KEY);
		initWidgets();
		setListeners();
		super.onStart();
	}
	
	private void initWidgets() {
		mMayAction = false;
		
		ArrayAdapter<BitmapSize> imsize_adapter = new ArrayAdapter<BitmapSize>(
				this, android.R.layout.simple_spinner_item, mFacade.getAvailableSizes());
		m_imsizeSpinner.setAdapter(imsize_adapter);
		
		m_textSizeSeek.setProgress(10);
		m_textSizeSeek.setProgress(mFacade.getTextSize()*10);
		m_imsizeSpinner.setSelection(3);
		BitmapSize bms[] = AsciiCamera.s_availableSizes;
		for (int i=0; i<bms.length;++i) {
			if (bms[i].equals(AsciiCamera.s_bitmapSize)) {
				m_imsizeSpinner.setSelection(i);
			}
		}

		if (AsciiCamera.s_grayscale) {
			m_gsRadio.setChecked(true);  
		} else { 
			m_colRadio.setChecked(true);
		}
		
		m_checkInvert.setChecked(AsciiCamera.s_inverted);
		m_bwCheck.setChecked(AsciiCamera.s_bw);
		
		mMayAction = true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.save_pic:
			finish();
			mFacade.savePicture();
			FlurryAgent.onEvent("picSaved");
			break;
		case R.id.save_text:
			finish();
			FlurryAgent.onEvent("textSaved");
			mFacade.saveText();
			break;
		case R.id.btn_reset:
			mFacade.reset();
			initWidgets();
			break;
		case R.id.about_btn:
			FlurryAgent.onEvent("onAbout");
			Intent i = new Intent(this, About.class);
			startActivity(i);
			break;
		case R.id.share:
			mFacade.share();
			break;
		default:
			break;
		}
	}
	
	private void hideAll() {
		mSizeLay.setVisibility(View.GONE);
		mColorLay.setVisibility(View.GONE);
		mMoreLay.setVisibility(View.GONE);
//		mDonateLay.setVisibility(View.GONE);
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
	


	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
		
		switch (arg0.getId()) {
		
		//main buttons
		case R.id.size_btn:
			if (isChecked) {
				hideAll();
				mSizeLay.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.color_btn:
			if (isChecked) {
				hideAll();
				mColorLay.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.more_btn:
			if (isChecked) {
				hideAll();
				mMoreLay.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.donate_btn:
			if (isChecked) {
				hideAll();
//				mDonateLay.setVisibility(View.VISIBLE);
			}
			break;
			 
		//particular buttons
		case R.id.check_invert:
			mFacade.setInverted(isChecked);
			break;
		case R.id.radio_gs:
			mFacade.setGrayscale(isChecked);
			if (isChecked) {
				m_bwCheck.setEnabled(true);
				m_checkInvert.setEnabled(true);
				m_butText.setText(R.string.saveastext);
			}
			break;
		case R.id.radio_color:
			mFacade.setColorized(isChecked);
			if (isChecked) {
				m_bwCheck.setEnabled(false);
				m_checkInvert.setEnabled(false);
				m_butText.setText(R.string.saveashtml);
			}
			break;
		case R.id.check_bw:
			mFacade.setBW(isChecked);
			break;
			
		default:
			break;
		}
	}

}
