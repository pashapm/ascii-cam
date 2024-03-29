package ru.jecklandin.asciicam;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.flurry.android.FlurryAgent;

import android.R.anim;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.text.Layout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class SlidingMenu extends Activity {

	AsciiCamera.Facade m_facade;
	
	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}

	//elems
	LinearLayout m_ly;
	LinearLayout m_biglay;
	LinearLayout m_ext;
	
	SeekBar m_textSizeSeek;
	CheckBox m_checkInvert;
	Spinner m_imsizeSpinner;
	RadioButton m_gsRadio;
	RadioButton m_colRadio;
	CheckBox m_bwCheck;
	Spinner m_quaSpinner;
	Button m_butPic;
	Button m_butText;
	Button m_butReset;
	Button m_butAbout;
	Button m_more;
	Button m_less;
	Button m_share;
	ImageView m_cowimage;
//	static MobclixMMABannerXLAdView adview_banner;
	
	Map<String, View> m_views;  

	//---------animations
	Animation m_appearAnimation;
	Animation m_fadeAnimation;

	private boolean m_mayAction = true;
	
	private static boolean is_ext  = false;
	@Override  
	protected void onStart() {
		
		//m_ly.startAnimation(m_appearAnimation);
		m_mayAction = false;
		
		m_textSizeSeek.setProgress(m_facade.getTextSize()*10);
		
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
		
		setExtVisible(is_ext);
		
		m_mayAction = true;

		FlurryAgent.onStartSession(this, AsciiCamera.FLURRY_KEY);
		super.onStart();
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE); 

		
        m_facade = AsciiCamera.s_instance.getFacade();
        
        //create animations
       
        m_appearAnimation = AnimationUtils.loadAnimation(this, R.anim.shake);
        m_fadeAnimation = AnimationUtils.loadAnimation(this, R.anim.fade);
         
        
        m_ly = (LinearLayout)View.inflate(this, R.layout.main, null);
        m_biglay = (LinearLayout)m_ly.findViewById(R.id.LinearLayout02);
        m_ext = (LinearLayout)m_ly.findViewById(R.id.extlay);
        
        
//        adview_banner = (MobclixMMABannerXLAdView) m_ly.findViewById(R.id.advertising_banner_view);
		//adview_banner.getAd();
//		adview_banner.setRefreshTime(5000);
//        if (adview_banner == null) {
//        	adview_banner = (MobclixMMABannerXLAdView) m_ly.findViewById(R.id.advertising_banner_view);
//    		//adview_banner.getAd();
//    		adview_banner.setRefreshTime(5000);
//        }
        
        m_views = new HashMap<String, View>();
        m_views.put("imsizelayout",m_ly.findViewById(R.id.LinearLayout03));
        m_views.put("sliderlayout",m_ly.findViewById(R.id.LinearLayout04));
        m_views.put("qualayout",m_ly.findViewById(R.id.LinearLayout09));
        m_views.put("checkbox",m_ly.findViewById(R.id.CheckBox01));
        m_views.put("rgroup",m_ly.findViewById(R.id.RadioGroup01));
        
        
        m_checkInvert = (CheckBox) m_ly.findViewById(R.id.CheckBox01);
        m_imsizeSpinner = (Spinner) m_ly.findViewById(R.id.Spinner01);
        m_textSizeSeek = (SeekBar) m_ly.findViewById(R.id.SeekBar01);
    	m_imsizeSpinner = (Spinner) m_ly.findViewById(R.id.Spinner01);
    	m_gsRadio = (RadioButton) m_ly.findViewById(R.id.RadioButton01);
    	m_colRadio = (RadioButton) m_ly.findViewById(R.id.colorize);
    	m_bwCheck = (CheckBox) m_ly.findViewById(R.id.CheckBox02);
    	m_quaSpinner = (Spinner) m_ly.findViewById(R.id.Spinner02);
    	m_butPic = (Button) m_ly.findViewById(R.id.Button01);
    	m_butText = (Button) m_ly.findViewById(R.id.Button02);
    	m_butReset = (Button) m_ly.findViewById(R.id.Button03); 
    	m_butAbout = (Button) m_ly.findViewById(R.id.Button04);
    	m_more = (Button) m_ly.findViewById(R.id.morebutton);
//    	m_less = (Button) m_ly.findViewById(R.id.less);
    	m_share = (Button) m_ly.findViewById(R.id.share);
//    	m_cowimage =  (ImageView) m_ly.findViewById(R.id.cow); 
    	  
//    	m_cowimage.setImageResource(R.drawable.cowedit);
    	
        m_textSizeSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
        	int progress = 0;
        	
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				AsciiCamera.s_instance.getFacade().setTextSize(this.progress);
			}
			 
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				m_biglay.setBackgroundColor(Color.parseColor("#88ffffff"));
				//SlidingMenu.this.makeOthersTransparent("sliderlayout");
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				this.progress = progress / 10;
			}
		});
       
        m_bwCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

				SlidingMenu.this.m_facade.setBW(isChecked);
				
			}
		});
        
        m_checkInvert.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

				SlidingMenu.this.m_facade.setInverted(isChecked);
			}
		}); 
        
        m_gsRadio.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

				SlidingMenu.this.m_facade.setGrayscale(isChecked);
				if (isChecked) {
					m_bwCheck.setEnabled(true);
					m_checkInvert.setEnabled(true);
					m_butText.setText(R.string.saveastext);
				}
			}
		});
        
        m_gsRadio.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View arg0) {
				SharedPreferences prefs = getSharedPreferences("asciicamera", MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putBoolean("gs", true);
				editor.commit();
				editor.putBoolean("col", false);
				editor.commit();
				Toast.makeText(SlidingMenu.this, R.string.settingssaved, Toast.LENGTH_LONG).show();
				m_facade.reset();
				return true;
			}
		});
        
        m_colRadio.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

				SlidingMenu.this.m_facade.setColorized(isChecked);
				if (isChecked) {
					m_bwCheck.setEnabled(false);
					m_checkInvert.setEnabled(false);
					m_butText.setText(R.string.saveashtml);
				}
			}
		});
        
        m_colRadio.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View arg0) {
				SharedPreferences prefs = getSharedPreferences("asciicamera", MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putBoolean("gs", false);
				editor.commit();
				editor.putBoolean("col", true);
				editor.commit();
				Toast.makeText(SlidingMenu.this, R.string.settingssaved, Toast.LENGTH_LONG).show();
				m_facade.reset();
				return true;
			}
		});
                
        m_imsizeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				m_facade.setImageSize(AsciiCamera.s_availableSizes[arg2]);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});

        ArrayAdapter<BitmapSize> imsize_adapter = 
        	new ArrayAdapter<BitmapSize>(this, android.R.layout.simple_spinner_item, 
        	m_facade.getAvailableSizes());
        m_imsizeSpinner.setAdapter(imsize_adapter);
        
        m_butPic.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SlidingMenu.this.finish();
				m_facade.savePicture();
			}
		});
        
        m_butText.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SlidingMenu.this.finish();
				m_facade.saveText();
			}
		});
        
        m_butReset.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_facade.reset();
				onStart();
			}
		});
        
        m_butAbout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				AsciiCamera.showAbout(SlidingMenu.this);
				FlurryAgent.onEvent("onAbout");
				Intent i = new Intent(SlidingMenu.this, About.class);
				startActivity(i);
			}
		});
        
        m_more.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				is_ext =! is_ext;
				setExtVisible(is_ext);
				FlurryAgent.onEvent("onMore");
			};
		});
        
        m_less.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				is_ext =! is_ext;
				setExtVisible(is_ext);
			};
		});
        
        m_share.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_facade.share();
			}
		});
        
        setContentView(m_ly);
	}
	
	private void setExtVisible(boolean b) {
		if (!b) {
			m_ext.setVisibility(View.GONE);
			m_biglay.setVisibility(View.VISIBLE);
		} else { 
			m_ext.setVisibility(View.VISIBLE);
			m_biglay.setVisibility(View.GONE);
		}
	}
	
    @Override
    protected void onStop() {
    	FlurryAgent.onEndSession(this);
        super.onStop();
    }
}


