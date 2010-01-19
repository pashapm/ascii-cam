package ru.jecklandin.asciicam;

import android.R.anim;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

public class SlidingMenu extends Activity {

	Animation shake;
	LinearLayout ly;


	@Override
	protected void onStart() {
		// TODO Auto-generated method stubl
		ly.startAnimation(shake);
		super.onStart();
		
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        ly = (LinearLayout)View.inflate(this, R.layout.main, null);
        
       
        shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        setContentView(ly);
	}
}
