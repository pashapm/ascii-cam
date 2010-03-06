package ru.jecklandin.asciicam;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;

public class PromptDialog {
	
	
	public static void prompt(String defaultValue, final PromptDialogCallback callback, Context ctx)
	{
		//load some kind of a view
		LayoutInflater li = LayoutInflater.from(ctx);
		View view = li.inflate(R.layout.promptdialog, null);
		//get a builder and set the view
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle("Prompt");
		builder.setView(view);
		//add buttons and listener
		
		builder.setPositiveButton("OK", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				callback.setValue("t");
			}
		});
		builder.setNegativeButton("Cancel", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		//get the dialog
		AlertDialog ad = builder.create();
		//show
		ad.show();
	}
	
	interface PromptDialogCallback {
		void setValue(String s);
	}
	
}
