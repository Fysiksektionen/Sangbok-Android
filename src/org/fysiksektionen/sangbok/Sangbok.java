package org.fysiksektionen.sangbok;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.fysiksektionen.sangbok.domain.Song;
import org.fysiksektionen.sangbok.network.HttpClient;
import org.fysiksektionen.sangbok.network.RESTClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.R.integer;
import android.app.Activity;
import android.app.ActionBar;
import android.content.Intent;
import android.content.res.AssetManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
//import android.widget.ExpandableListView;
import android.widget.ListView;
//import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class Sangbok extends Activity {
	//some class-variables
	private ArrayAdapter<Sang> sangerView;
	private AssetManager assetManager;
	private ListView resList;
	private List<List<Sang>> sangerList;
	
	private Menu myMenu;
	private boolean alphSortMenuItem = true;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //Set-up the linkings
        assetManager = getAssets();
        sangerView = new ArrayAdapter<Sang>(this, android.R.layout.simple_list_item_1);
        resList = (ListView) findViewById(R.id.resultList);
        resList.setAdapter(sangerView);
        sangerList = new ArrayList<List<Sang>>();
        
        //Read all songs at start up
        initLists();
        
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
    
    
    /* Set up all chapters!
     */
    
    /* Initialize the SongList by
     * getting all .txt files in Assets
     * and convert it into Songs that can be used!
     */
    public void initLists() {
    	//Clear the list and re-do it!
    	sangerView.clear();
    	sangerList.clear();
    	//Initialize the sangerList to hold at least as many chapters as defined in the xml
    	String[] chapters = getResources().getStringArray(R.array.chapter_names);
    	for( int i=0; i<chapters.length; i++ ) {
    		sangerList.add( new ArrayList<Sang>() );
    	}
        //Work with assets and find all .txt files
        String[] files = null;
        Sang temp;
        try{
        files = assetManager.list("");
        } catch(IOException e) {
        	e.printStackTrace();
        }
        for (String file : files) {
            if(file.toLowerCase(Locale.ENGLISH).endsWith(".txt")){
            	//Make a Song of the .txt-file and add to the list
            	temp = readSangFromFile( file );
            	sangerView.add( temp );
            	while( temp.getChapter() > sangerList.size() ) {//Error handling, if trying to add to a chapter larger than defined...
            		sangerList.add( new ArrayList<Sang>() );
            	}
            	sangerList.get( temp.getChapter()-1 ).add( temp );
             }
        }
        sangerView.sort( Sang.getChapterComparator() );
        sangerView.notifyDataSetChanged();
    }
    
    /* Open the passed file and process it so that it becomes a nice Song.
	 * so that the rest of the structure can work abstract with the type Song.
	 */
    public Sang readSangFromFile(String file) {
    	InputStream iS = null; 
    	Sang retSang = new Sang();
    	retSang.setTitle(file);
    	retSang.setChapter( Character.getNumericValue(file.charAt(0)) ); //first character i chapter number
    	retSang.setNumber( Integer.parseInt( file.substring(2, file.length()-4) ) ); //then comes - and then the rest except .txt is the song number within that chapter
        //get the file as a stream 
        try{
        iS = assetManager.open(file);
        StringBuilder buffer = new StringBuilder();
        BufferedReader bR= new BufferedReader(new InputStreamReader(iS));
        String str;
        int state = -1;
        while ((str=bR.readLine()) != null) {
        	//Remove starting whites-paces
        	while(str.startsWith(" ") ) {
        		str = str.substring(1);
        	}
        	if( str.startsWith("<titel>") ) {
        		setSangContent(state, retSang, buffer.toString());
              	state = 0;
              	str = str.substring(7);
				//Remove following white-spaces
				while(str.startsWith(" ") ) {
					str = str.substring(1);
				}
				buffer.setLength(0);
              	buffer.append(str);
              	
        	}else if( str.startsWith("<melodi>") ) {
        		setSangContent(state, retSang, buffer.toString());
              	state = 1;
              	str = str.substring(8);
				//Remove following white-spaces
				while(str.startsWith(" ") ) {
					str = str.substring(1);
				}
				buffer.setLength(0);
              	buffer.append(str);
        		
        	}else if( str.startsWith("<text>") ) {
        		setSangContent(state, retSang, buffer.toString());
              	state = 2;
              	str = str.substring(6);
				//Remove following white-spaces
				while(str.startsWith(" ") ) {
					str = str.substring(1);
				}
              	buffer.append(str);
              	buffer.setLength(0);
              	buffer.append(str);
        		
        	}else if( str.startsWith("<author>") ) {
        		setSangContent(state, retSang, buffer.toString());
              	state = 3;
              	str = str.substring(8);
				//Remove following white-spaces
				while(str.startsWith(" ") ) {
					str = str.substring(1);
				}
              	buffer.append(str);
              	buffer.setLength(0);
              	buffer.append(str);
        		
        	}else if(  (str.equals("") || str.equals("\n")) && (state!=2)  ) {}
        	else {
        		buffer.append("\n");
        		buffer.append(str);
        	}
        }
        //When exiting, do what you must with the rest!
        setSangContent(state, retSang, buffer.toString());
	    iS.close();
        } catch(IOException e) {
	    	e.printStackTrace();
	    }
        return retSang;
   }
    //Helper function, to the "readSangFromFile(String file)"-function
    private void setSangContent(int state, Sang retSang, String toSet) {
        switch (state) {
		case 0:
			retSang.setTitle(toSet);
			break;
		case 1:
			retSang.setMelody(toSet);
			break;
		case 2:
			retSang.setText(toSet);
			break;
		case 3:
			retSang.setAuthor(toSet);
			break;
		case -1:
			break;
      	}
    }
    
    
    //When clicking the search-button
    public void onSearchButton(View view) {
    	searchAction();
    	return;
    }
    /* THE Search-function
     * Here so that it can be called from multiple instances!
     */
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
			textView.setText( R.string.whole_book );
			return;
		}
    	searchSubStr( searchString ); //Do the actual search!
    	if (sangerView.isEmpty() ) { //We dodn't find anything
    		textView.setText( R.string.no_search_results );
    	}else{//We found something
    		textView.setText( R.string.search_results );
    	}
    	return;
    }
    
    
    //Handle menu options
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if( id >= 0 && id <= sangerList.size() ) {
			showChpt( id );
			TextView textView = (TextView) findViewById(R.id.what_is_seen);
			String seen ="";
			if( id != sangerList.size() ) seen = getString(R.string.view_chpt) + " ";
		    textView.setText( seen + item.getTitle() );
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
			return true;
		case R.id.sync:
			return true;
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
	 * Function that searches for the occurrence of <str> in the <title> and <text> of a song
	 */
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
}
