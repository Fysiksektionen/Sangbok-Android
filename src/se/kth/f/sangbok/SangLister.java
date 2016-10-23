package se.kth.f.sangbok;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.JsonReader;
import android.util.JsonToken;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

/*
Class that reads the files on the device and processes them into songs!
Kept in separate class for readability.
*/
public class SangLister {
	
	private ArrayAdapter<Sang> sangerView;
	private List<Chapter> chapterList;
	private Activity myApp;
	private List<String> chapterNames;
	
	public SangLister(Activity mA, ArrayAdapter<Sang> sW, List<Chapter> cL, List<String> cN) {
		myApp = mA;
		sangerView = sW;
		chapterList = cL;
		chapterNames = cN;
	}
	
    /* Set up all chapters!
     */
    
    /* Initialize the SongList by
     * getting all files with correct ending in the Private File Domain on the Android OS
     * and convert it into Songs that can be used!
     */
    public void initLists() {
    	//Clear the list and re-do it!
    	sangerView.clear();
    	chapterList.clear();
    	chapterNames.clear();

    	//Work on the private storage of the app in the Android OS
    	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(myApp);
		String fileName = sharedPrefs.getString("fsangbok", myApp.getString(R.string.current_song_book_databese_file_name));
    	File file = new File(myApp.getFilesDir().getAbsolutePath() + "/" + fileName);
//    	List<Chapter> chapterList = new ArrayList<Chapter>();
        try{
        	InputStream is = new BufferedInputStream(new FileInputStream(file));
            //Work with the info gotten from the server!
        	JsonReader reader = new JsonReader(new InputStreamReader(is, "UTF-8"));
        	reader.beginArray();
            while (reader.hasNext()) {
            	chapterList.add( readChapter(reader) );
            }
            reader.endArray();
            // close streams
            is.close();
        }
        catch(FileNotFoundException e){
        	e.printStackTrace();
        } catch (IOException e) {
			e.printStackTrace();
		}
        
        //Add chapters to running-app-storage
        //Sort all chapters so that standard is according to Chapter-sorting, also sort all songs in the chapter.
        Collections.sort( chapterList, Chapter.getNumberComparator() );
        while( chapterList.size() > 0 ) {
        	if( chapterList.get(0).getNumber() != -1) {
        		break;
        	}
        	chapterList.remove(0);
        }
        for( int i = 0; i < chapterList.size(); ++i ) {
        	chapterNames.add(chapterList.get(i).getName());
        	Collections.sort( chapterList.get(i).getSangs(), Sang.getChapterComparator() );
        }
    	
        //Add songs to the View part as well
        for( int i = 0; i < chapterList.size(); ++i ) {
        	for( Sang toAdd : chapterList.get(i).getSangs() ) {
        		sangerView.add( toAdd );
        	}
        }
        if( sangerView.isEmpty() ) {//If no data is found tell the user what to do
//        	chapterList.add( new ArrayList<Sang>() );
        	List<Sang> noSangFoundList = new ArrayList<Sang>();
        	noSangFoundList.add(new Sang( myApp.getString(R.string.no_song_found_title), "", myApp.getString(R.string.no_song_found_text), "", 0, 0) );
        	chapterList.add( new Chapter( "", 0, noSangFoundList) );
        	sangerView.add( new Sang( myApp.getString(R.string.no_song_found_title), "", myApp.getString(R.string.no_song_found_text), "", 0, 0) );
        }
        //Update the visual part
        TextView textView = (TextView) myApp.findViewById(R.id.what_is_seen);
    	textView.setText( myApp.getString(R.string.what_you_see) + " " + myApp.getString(R.string.whole_book) );
        sangerView.notifyDataSetChanged();
    }
    
    
    /*
     * Build a chapter.
	 */
    public Chapter readChapter( JsonReader reader ) throws IOException {
    	String chptName = "";
    	int chptNum = -1;
    	List<Sang> chptSangs = new ArrayList<Sang>();
        
        reader.beginObject();
	     while (reader.hasNext()) {
	       String name = reader.nextName();
	       if (name.equals("id")) {
	    	   chptNum = reader.nextInt();
	       } else if (name.equals("chapter")) {
	    	   chptName = reader.nextString();
	       } else if (name.equals("songs")) {
	    	   reader.beginArray();
	    	   while (reader.hasNext()) {
	    		   chptSangs.add(readSang(reader));
    	       }
    	       reader.endArray();
	       } else {
	         reader.skipValue();
	       }
	     }
	     reader.endObject();
	     return new Chapter(chptName, chptNum, chptSangs);
   }
    
    /*
     * Build a song.
	 */
    public Sang readSang(JsonReader reader) throws IOException {
	     int id = -1; //Not used in current implementation
	     int chapter_id = -1;
	     int number = -1;
	     String title = "";
	     String author = "";
	     String melody = "";
	     String text = "";

	     reader.beginObject();
	     while (reader.hasNext()) {
	       String name = reader.nextName();
	       if (name.equals("id")) {
	         id = reader.nextInt();
	       } else if (name.equals("chapter_id") && reader.peek() != JsonToken.NULL) {
	    	   chapter_id = reader.nextInt();
	       } else if (name.equals("number") && reader.peek() != JsonToken.NULL) {
	    	   number = reader.nextInt();
	       } else if (name.equals("title") && reader.peek() != JsonToken.NULL) {
	    	   title = reader.nextString();
	       } else if (name.equals("author") && reader.peek() != JsonToken.NULL) {
	    	   author = reader.nextString();
	       } else if (name.equals("melody") && reader.peek() != JsonToken.NULL) {
	    	   melody = reader.nextString();
	       } else if (name.equals("text") && reader.peek() != JsonToken.NULL) {
		         text = reader.nextString();
	       } else {
	         reader.skipValue();
	       }
	     }
	     reader.endObject();
	     return new Sang(title, melody, text, author, chapter_id, number);
	   }
}
