package se.kth.f.sangbok;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
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
	private List<Chapter> chapterList;
	private List<String> chapterNames;
	
	private Menu myMenu;
	private boolean alphSortMenuItem = true;
	private boolean RETURN_FROM_SETTINGS = false;
	
	private SangLister sL;
	
	
	
	@Override
	public void onResume(){
		super.onResume();
		if( RETURN_FROM_SETTINGS ) {
			sL.initLists();
			invalidateOptionsMenu();
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
        chapterList = new ArrayList<Chapter>();
        chapterNames = new ArrayList<String>();
        sL = new SangLister(this, sangerView, chapterList, chapterNames);
        
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
        myMenu = menu; //Come in handy later when you want to change the menu-icons in resetIcons();
        
        //Create the submenu that shows one chapter at the time
        SubMenu sub = (SubMenu) menu.findItem( R.id.view_chpt ).getSubMenu();
        for( int i=0; i<chapterNames.size(); i++ ) {
        	sub.add( Menu.NONE, i, i, Integer.toString(i+1) + " - " + chapterNames.get(i) );
        }
        sub.add( Menu.NONE, chapterNames.size(), chapterNames.size(), getString( R.string.whole_book) );
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
		for( int i=0; i<chapterList.size(); ++i ) {
			tempList = chapterList.get(i).getSangs();
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
		
		if( id >= 0 && id <= chapterNames.size() ) {
			showChpt( id );
			resetIcons();
			MenuItem chpt_sort_item = myMenu.findItem( R.id.sort_chpt );
			chpt_sort_item.setIcon( R.drawable.chpt_sort );
			TextView textView = (TextView) findViewById(R.id.what_is_seen);
			String seen =" ";
			if( id != chapterNames.size() ) seen = " " + getString(R.string.view_chpt) + " ";
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
		if( chapter > chapterList.size() ) return;
		if( chapter == chapterNames.size() ) {
			showAllSang();
			return;
		}
		sangerView.clear();
		List<Sang> temp = chapterList.get(chapter).getSangs();
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
		for( int i=0; i<chapterList.size(); ++i ) {
			temp = chapterList.get(i).getSangs();
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
		    return SynchronizeHandler.sync(Sangbok.this);
		    // Return an integer to tell how the process went
		}

		@Override
		protected void onPostExecute(Integer upDate) {//Deliver a message to the user of how the synchronization went
			
			switch( upDate ){
			case 1:
				Toast.makeText(Sangbok.this, R.string.network_error, Toast.LENGTH_LONG).show();
				break;
			case 2:
				Toast.makeText(Sangbok.this, R.string.network_host_not_found, Toast.LENGTH_LONG).show();
				break;
			case 3:
				Toast.makeText(Sangbok.this, R.string.network_memory_bug, Toast.LENGTH_LONG).show();
				break;
			case 4:
				Toast.makeText(Sangbok.this, R.string.network_wierd_error, Toast.LENGTH_LONG).show();
				break;
			default:
				Toast.makeText(Sangbok.this, R.string.network_done, Toast.LENGTH_LONG).show();
				break;
			}

			//Re-initiate the song lists after synchronization.
			sL.initLists();
			//Re-initiate chapter names as displayed in options bar
			invalidateOptionsMenu();
		}
	}
	
	
}
