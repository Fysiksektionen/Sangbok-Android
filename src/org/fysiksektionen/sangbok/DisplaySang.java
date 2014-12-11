package org.fysiksektionen.sangbok;

import android.os.Bundle;
import android.app.Activity;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.text.method.ScrollingMovementMethod;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class DisplaySang extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_sang);
		// Show the Up button in the action bar.
		setupActionBar();
		
		// Determines if the screen is always on or not
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		if (sharedPrefs.getBoolean("screen_on", true) ) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		else {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		
		//Get the intent to get the Sang
		Intent intent = getIntent();
		Sang theSang = (Sang) intent.getSerializableExtra("Sang");
		setTitle(theSang.getTitle());
		
		//Display the melody
	    TextView textView = (TextView) findViewById(R.id.show_mel);
	    textView.setText(theSang.getMelody());
	    textView.setTextSize(17);
		
	    //Display the song-text there.
	    textView = (TextView) findViewById(R.id.show_sang_text);
	    textView.setText(theSang.getText());
	    textView.setTextSize(20);
	    textView.setMovementMethod(new ScrollingMovementMethod()); //To allow scrolling if the text is to long
	    
	    //Display the author
	    textView = (TextView) findViewById(R.id.show_author);
	    textView.setText(theSang.getAuthor());
	    textView.setTextSize(17);
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.display_sang, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		TextView textView;
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			super.onBackPressed();
			return true;
		case R.id.font_inc:
			textView = (TextView) findViewById(R.id.show_sang_text);
		    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textView.getTextSize()*1.1F);
			return true;
		case R.id.font_dec:
			textView = (TextView) findViewById(R.id.show_sang_text);
		    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textView.getTextSize()*0.9090909F);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
	    int action = event.getAction();
	    int keyCode = event.getKeyCode();
	    switch (keyCode) {
	        case KeyEvent.KEYCODE_VOLUME_UP:
	            if (action == KeyEvent.ACTION_DOWN) {
	            	TextView textView = (TextView) findViewById(R.id.show_sang_text);
	    		    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textView.getTextSize()*1.1F);
	            }
	            return true;
	        case KeyEvent.KEYCODE_VOLUME_DOWN:
	            if (action == KeyEvent.ACTION_DOWN) {
	            	TextView textView = (TextView) findViewById(R.id.show_sang_text);
	    		    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textView.getTextSize()*0.9090909F);
	            }
	            return true;
	        default:
	            return super.dispatchKeyEvent(event);
	    }
	}

}
