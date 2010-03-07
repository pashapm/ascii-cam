package ru.jecklandin.asciicam;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class PromptDialog {
	
	
	public static void prompt(String defaultValue, final PromptDialogCallback callback, Context ctx)
	{
		//load some kind of a view
		LayoutInflater li = LayoutInflater.from(ctx);
		View view = li.inflate(R.layout.promptdialog, null);
		//get a builder and set the view
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle(R.string.savingfile);
		builder.setView(view);
		//add buttons and listener
		
		final EditText edit = (EditText) view.findViewById(R.id.EditText01);
		edit.setText(defaultValue);
		    
		builder.setPositiveButton("OK", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				callback.ok(edit.getEditableText().toString());
			}
		});
		builder.setNegativeButton("Cancel", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				callback.cancel();
			}
		});
		//get the dialog
		AlertDialog ad = builder.create();
		//show
		ad.show();
	}
	
	interface PromptDialogCallback {
		void ok(String s);
		void cancel();
	}
	
}
