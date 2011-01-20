package ru.jecklandin.asciicam;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.flurry.android.FlurryAgent;

public class About extends ListActivity {
	
	private final String CREDITS_TEXT = "Evgeny Balandin, 2010";
	private final String LINKS_TEXT = "Send me a <a href=\"mailto:balandin.evgeny@gmail.com\">letter</a>.";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.about);
		
		TextView links = (TextView) findViewById(R.id.links_text);
		links.setText(Html.fromHtml(LINKS_TEXT));
		links.setMovementMethod(LinkMovementMethod.getInstance());
		links.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				FlurryAgent.onEvent("onAboutLinkClicked");
			}
		});
		
		MyApp asciicamera = new MyApp();
		asciicamera.icon = R.drawable.gosling;
		asciicamera.descr = "Slingshot shooter";
		asciicamera.name = "Go Sling!";
		asciicamera.pack = "ru.jecklandin.duckshot";
		
		MyApp smsilence = new MyApp();
		smsilence.icon = R.drawable.smsilence;
		smsilence.descr = "Allows your phone choose the ring mode depending on the incoming SMS";
		smsilence.name = "SMSilence";
		smsilence.pack = "ru.jecklandin.silencesign";
		
		MyApp[] apps = new MyApp[] { asciicamera, smsilence	};
		final MyAppsAdapter adapter = new MyAppsAdapter(this, R.layout.my_apps_elem, R.id.app_name, apps);
		getListView().setAdapter(adapter);
		getListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				MyApp app = adapter.getItem(arg2);
				FlurryAgent.onEvent("adClicked"+app.name);
				Intent i = new Intent();
				i.setData(Uri.parse("market://details?id="+app.pack)); 
				startActivity(i);
			}
		});
	}
	 
	@Override
	protected void onStop() {
		super.onStop(); 
		FlurryAgent.onEndSession(this);
	}
	
	@Override
	protected void onStart() {
		FlurryAgent.onStartSession(this, AsciiCamera.FLURRY_KEY);
		super.onStart();
	}
	
	private class MyApp {
		public int icon;
		public String name;
		public String descr;
		public String pack;
	}

	private class MyAppsAdapter extends ArrayAdapter<MyApp> {
		
		public MyAppsAdapter(Context context, int res, int textViewResourceId,
				MyApp[] objects) {
			super(context, res, textViewResourceId, objects);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inf = LayoutInflater.from(getContext());
				convertView = inf.inflate(R.layout.my_apps_elem, null);
			}
			
			ImageView icon = (ImageView) convertView.findViewById(R.id.app_icon);
			TextView name = (TextView) convertView.findViewById(R.id.app_name);
			TextView descr = (TextView) convertView.findViewById(R.id.app_descr);
			
			MyApp app = getItem(position);
			
			icon.setImageResource(app.icon);
			name.setText(app.name);
			descr.setText(app.descr);
			
			return convertView;
		}
	}
}
