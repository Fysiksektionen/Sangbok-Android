package org.fysiksektionen.sangbok;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class Sangbok extends Activity {
	//some class-variables
	private ArrayAdapter<Sang> sangerView;
	private ListView resList;
	private List<List<Sang>> sangerList;
	
	private Menu myMenu;
	private boolean alphSortMenuItem = true;
	private boolean RETURN_FROM_SETTINGS = false;
	
	private SangLister sL;
	
	
	@Override
	public void onResume(){
		super.onResume();
		if( RETURN_FROM_SETTINGS ) {
			sL.initLists();
		}
		RETURN_FROM_SETTINGS = false;
	}
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //Fix some layout details
        setTitle( getString(R.string.app_label) );

        //Set-up the linkings
        sangerView = new ArrayAdapter<Sang>(this, android.R.layout.simple_list_item_1);
        resList = (ListView) findViewById(R.id.resultList);
        resList.setAdapter(sangerView);
        sangerList = new ArrayList<List<Sang>>();
        sL = new SangLister(this, sangerView, sangerList);
        
        //Read all songs at start up
        sL.initLists();
        
        //Connect a OnItemClickListener to the ListView with Songs
        resList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	//When clicked, pass the clicked Sang to the ViewSang activity
            	Sang clickedSang = sangerView.getItem((int)id);
            	Intent intent = new Intent(Sangbok.this, DisplaySang.class);
            	intent.putExtra("Sang", clickedSang);
            	startActivity(intent);
            }
            });
        
        //Connect listener to EditText
        EditText editText = (EditText) findViewById(R.id.search_string);
        editText.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {           	
                boolean handled = false;
                //Workaround since sometimes the event is NULL eventhough it should bnot be...
                //https://github.com/commonsguy/cw-omnibus/tree/master/ActionBar/ActionBarDemo
                //http://stackoverflow.com/questions/11301061/null-keyevent-and-actionid-0-in-oneditoraction-jelly-bean-nexus-7
                if (event != null && event.getAction() != KeyEvent.ACTION_DOWN) {
                }
                else if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || event == null
                    || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                          searchAction();
                          handled = true;
                }
                return handled;
            }
        });
    }

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        myMenu = menu; //Come in handy later when ou want to change the menu-icons in resetIcons();
        
        //Create the submenu that shows one chapter at the time
        SubMenu sub = (SubMenu) menu.findItem( R.id.view_chpt ).getSubMenu();
        String[] chapters = getResources().getStringArray(R.array.chapter_names);
        for( int i=0; i<chapters.length; i++ ) {
        	sub.add( Menu.NONE, i, i, Integer.toString(i+1) + " - " + chapters[i] );
        }
        sub.add( Menu.NONE, chapters.length, chapters.length, getString( R.string.whole_book) );
        return super.onCreateOptionsMenu(menu);
    }
    

    //When clicking the search-button
    public void onSearchButton(View view) {
    	searchAction();
    	return;
    }
    /* THE SEARCH-function
     * Placed here so that it can be called from multiple instances! */
    private void searchAction() {
    	//start by resetting icons. Search result is displayed in chapter sort.
    	resetIcons();
    	MenuItem item = myMenu.findItem( R.id.sort_chpt );
    	item.setIcon( R.drawable.chpt_sort );
    	//Write what is seen...
    	TextView textView = (TextView) findViewById(R.id.what_is_seen);
	    EditText editText = (EditText) findViewById(R.id.search_string);
		String searchString = editText.getText().toString();
		if( searchString.equals("") ) { //If no search string show complete book
			showAllSang();
			textView.setText( getString(R.string.what_you_see) + " " + getString(R.string.whole_book) );
			return;
		}
    	searchSubStr( searchString ); //Do the actual search!
    	if (sangerView.isEmpty() ) { //We dodn't find anything
    		textView.setText( R.string.no_search_results );
    	}else{//We found something
    		textView.setText( getString(R.string.what_you_see) + " " + getString(R.string.search_results) );
    	}
    	return;
    }
    
	/* Function that searches for the occurrence of <str> in the <title> and <text> of a song */
	private void searchSubStr( String str ) {
		sangerView.clear();
		List<Sang> tempList;
		Sang tempSang;
		for( int i=0; i<sangerList.size(); ++i ) {
			tempList = sangerList.get(i);
			for( int j=0; j<tempList.size(); ++j ) {
				tempSang = tempList.get(j);
				if( tempSang.getTitle().toLowerCase(Locale.ENGLISH).contains(str.toLowerCase(Locale.ENGLISH)) ||  tempSang.getText().toLowerCase(Locale.ENGLISH).contains(str.toLowerCase(Locale.ENGLISH))) {
					sangerView.add( tempSang );
				}
				
			}
		}
		sangerView.notifyDataSetChanged();
		return;
	}
    
    
    //Handle menu options selected.
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		
		if( id >= 0 && id <= sangerList.size() ) {
			showChpt( id );
			TextView textView = (TextView) findViewById(R.id.what_is_seen);
			String seen =" ";
			if( id != sangerList.size() ) seen = " " + getString(R.string.view_chpt) + " ";
		    textView.setText( getString(R.string.what_you_see) + seen + item.getTitle() );
			return true;
		}
		switch (id) {
		case R.id.sort_alph:
	        //When sorting alphabetically it updates the icon and states according to alphabetically or reverse.
	        if( alphSortMenuItem ){
	        	resetIcons();
	        	sangerView.sort( Sang.getTitleComparator() );
                item.setTitle( getString(R.string.alph_sort) );
                item.setIcon( R.drawable.alph_sort_f);
                alphSortMenuItem = false;
            }else{
            	resetIcons();
            	sangerView.sort( Sang.getRevTitleComparator() );
                item.setTitle( getString(R.string.alph_sort_rev) );
                item.setIcon( R.drawable.alph_sort_rev_f);
                alphSortMenuItem = true;
            }
	        sangerView.notifyDataSetChanged();
			return true;
		case R.id.sort_chpt:
			resetIcons();
			item.setIcon( R.drawable.chpt_sort );
			sangerView.sort( Sang.getChapterComparator() );
	        sangerView.notifyDataSetChanged();
			return true;
		case R.id.view_chpt:
			//Take care of above... This is the menu option opening the sub-menu whose alternative taken care above if the if-statement
			return true;
		case R.id.sync:
			new sync().execute();
			return true;
		case R.id.menu_settings:
			RETURN_FROM_SETTINGS = true;
			Intent intent = new Intent(Sangbok.this, SettingsActivity.class);
        	startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}
	/*Helper function to reset the sort-icons to gray. Useful sometimes...
	 */
	private void resetIcons() {
		MenuItem item = myMenu.findItem( R.id.sort_alph );
		item.setIcon( R.drawable.alph_sort_gray );
		item.setTitle( getString(R.string.alph_sort) );
		alphSortMenuItem = true;
		item = myMenu.findItem( R.id.sort_chpt );
		item.setIcon( R.drawable.chpt_sort_gray );
		item.setTitle( getString(R.string.chpt_sort) );
	}
	/*Function that only shows songs from the selected Chapter!
	 */
	private void showChpt( int chapter ) {
		if( chapter > sangerList.size() ) return;
		if( chapter == sangerList.size() ) {
			showAllSang();
			return;
		}
		sangerView.clear();
		List<Sang> temp = sangerList.get(chapter);
		for( int i=0; i<temp.size(); ++i ) {
			sangerView.add( temp.get(i) );
		}
		sangerView.notifyDataSetChanged();
		return;
	}
	/*Function that shows all songs loaded to the App
	 */
	private void showAllSang() {
		sangerView.clear();
		List<Sang> temp;
		for( int i=0; i<sangerList.size(); ++i ) {
			temp = sangerList.get(i);
			for( int j=0; j<temp.size(); ++j ) {
				sangerView.add( temp.get(j) );
			}
		}
		sangerView.notifyDataSetChanged();
		return;
	}

	
	/*
	 * Function that performs the Synchronization of songs with the server.
	 */
	private class sync extends AsyncTask<String, Boolean, Integer> {
		
		@Override
        protected Integer doInBackground(String... urls) {
			Integer upDate = 1;
			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(Sangbok.this);
			String serverURL = sharedPrefs.getString("server_URL",getString(R.string.serverURL));
			String instructionURL = sharedPrefs.getString("server_instruction_URL",getString(R.string.serverInstructionURL));
			String numDelimiter = sharedPrefs.getString("number_delimiter",getString(R.string.serverFileDelimiter));
			String fileEnding = sharedPrefs.getString("file_ending",getString(R.string.SongFileEnding));
		    if (networkInfo != null && networkInfo.isConnected()) {//We have Internet. Time to sync!
		    	InputStream is = null;
		    	InputStream sangStream = null;
		    	FileOutputStream outputStream = null;
		    	HttpURLConnection conn = null;
		        try {
		        	//Set up connection with the download site
		            URL url = new URL( serverURL + instructionURL  );
		            conn = (HttpURLConnection) url.openConnection();
		            conn.setReadTimeout(10000 /* milliseconds */);
		            conn.setConnectTimeout(15000 /* milliseconds */);
		            conn.setRequestMethod("GET");
		            conn.setDoInput(true);
		            // Starts the query
		            conn.connect();
		            is = conn.getInputStream();
		        }
	            catch( IOException e ) {
		        	e.printStackTrace();
		        	upDate = 3;
		        	return upDate;
		        }
		        try{
		            //Work with the info gotten from the server!
					InputStreamReader isr = new InputStreamReader(is);
					BufferedReader ir = new BufferedReader(isr);
					String line;
					int readingChpt;
					int buffLen = -1;
					int start = -1;
					int stop = -1;
					StringBuilder buffer = new StringBuilder();
					//First row is trash!
					ir.readLine();
					//Then work as long as there are rows left. The come in pairs of three.
					while( (line = ir.readLine()) != null ) {
						line = line.trim();
						//First line is the chapter integer
						readingChpt = Integer.parseInt( line );

						buffer.setLength(0);
						buffer.append( serverURL );
		              	buffer.append(line);
		              	buffer.append( numDelimiter );
		              	buffLen = buffer.length();
		              	
		              	//then follows start and stop for song numbers in that chapter
		              	line = ir.readLine().trim();	              	
		              	start = Integer.parseInt(line);
		              	line = ir.readLine().trim();
		              	stop = Integer.parseInt(line);
		              	for( int i=start; i<=stop; ++i ) {
		              		buffer.setLength(buffLen);
		              		buffer.append(i);
		              		buffer.append( fileEnding );
		              		conn = (HttpURLConnection) new URL( buffer.toString() ).openConnection();
				            conn.setReadTimeout(10000); //miliseconds
				            conn.setConnectTimeout(15000); //miliseconds
				            conn.setRequestMethod("GET");
				            conn.setDoInput(true);
				            conn.connect();
				            sangStream = conn.getInputStream();
				            //Write the songs to file
				            outputStream = openFileOutput( Integer.toString(readingChpt) + numDelimiter + Integer.toString(i) + fileEnding, Context.MODE_PRIVATE);
				            outputStream.write(new Scanner(sangStream,"UTF-8").useDelimiter("\\A").next().getBytes());
				            outputStream.close();
		              	}
		              	
			        }
		            
		            // close stream and disconnect
		            is.close();
		            conn.disconnect();
		        }
		        catch( IOException e ) {
		        	e.printStackTrace();
		        	if( e.getMessage().startsWith("http://") ){
		        		upDate = 5;
		        		return upDate;
		        	}else {
			        	upDate = 4;
			        	return upDate;
		        	}
		        }
		        catch( NumberFormatException e ) {
		        	e.printStackTrace();
		        	upDate = 5;
		        	return upDate;
		        }

		    }else {//We do not have Internet, display error
		    	upDate = 2;
	    	}
		    return upDate;
		}
		/*Help function to write the file.
		 * To distinguish 
		 */

		@Override
		protected void onPostExecute(Integer upDate) {//Deliver a message to the user of how the synchronization went
			switch( upDate) {
			case 1:
				Toast.makeText(Sangbok.this, R.string.network_done, Toast.LENGTH_LONG).show();
				break;
			case 2:
				Toast.makeText(Sangbok.this, R.string.network_error, Toast.LENGTH_LONG).show();
				break;
			case 3:
				Toast.makeText(Sangbok.this, R.string.network_host_not_found, Toast.LENGTH_LONG).show();
				break;
			case 4:
				Toast.makeText(Sangbok.this, R.string.network_memory_bug, Toast.LENGTH_LONG).show();
				break;
			case 5:
				Toast.makeText(Sangbok.this, R.string.network_host_error, Toast.LENGTH_LONG).show();
				break;
			}
			//Re-initiate the song lists after synchronization.
			sL.initLists();
		}
	}
	
	
}
