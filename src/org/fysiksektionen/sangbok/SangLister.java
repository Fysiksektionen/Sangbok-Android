package org.fysiksektionen.sangbok;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/*
Class that reads the files on the device and processes them into songs!
Kept in separate class for readability.
*/
public class SangLister {
	
	private ArrayAdapter<Sang> sangerView;
	private List<List<Sang>> sangerList;
	private Activity myApp;
	
	public SangLister(Activity mA, ArrayAdapter<Sang> sW, List<List<Sang>> sL) {
		myApp = mA;
		sangerView = sW;
		sangerList = sL;
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
    	sangerList.clear();
    	//Initialize the sangerList to hold at least as many chapters as defined in the xml
    	String[] chapters = myApp.getResources().getStringArray(R.array.chapter_names);
    	for( int i=0; i<chapters.length; i++ ) {
    		sangerList.add( new ArrayList<Sang>() );
    	}
        //Work on the private storage of the app in the Android OS and find all files with correct ending
        File[] files = null;
        Sang temp;
        files = myApp.getFilesDir().listFiles();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(myApp);
		String fileEnding = sharedPrefs.getString("file_ending", myApp.getString(R.string.SongFileEnding));
        for (File file : files) {
            if(file.getPath().toLowerCase(Locale.ENGLISH).endsWith( fileEnding.toLowerCase(Locale.ENGLISH) )){
            	//Make a Song of the ".txt"-file (or other file ending if that is specified) and add to the list
            	temp = readSangFromFile( file );
            	if( temp.getChapter() <= 0 ) { //The smallest chapter number expected is 1. Otherwise handle it somehow!
            		temp.setChapter( 1 );
            	}
            	while( temp.getChapter() > sangerList.size() ) {//Error handling, if trying to add to a chapter larger than defined...
            		sangerList.add( new ArrayList<Sang>() );
            	}
            	sangerList.get( (temp.getChapter()-1) ).add( temp );
             }
        }
        //Sort all chapters so that standard is according to Chapter-sorting
        for( int i = 0; i < sangerList.size(); ++i ) {
        	Collections.sort( sangerList.get(i), Sang.getChapterComparator() );
        	for( Sang add : sangerList.get(i) ) {
        		sangerView.add( add );
        	}
        }
        if( sangerView.isEmpty() ) {//If no data is found tell the user what to do
        	sangerList.add( new ArrayList<Sang>() );
        	sangerList.get(sangerList.size()-1).add( new Sang( myApp.getString(R.string.no_song_found_title), "", myApp.getString(R.string.no_song_found_text), "", 0, 0) );
        	sangerView.add( new Sang( myApp.getString(R.string.no_song_found_title), "", myApp.getString(R.string.no_song_found_text), "", 0, 0) );
        }
        //Update the visual part
        TextView textView = (TextView) myApp.findViewById(R.id.what_is_seen);
    	textView.setText( myApp.getString(R.string.what_you_see) + " " + myApp.getString(R.string.whole_book) );
        sangerView.notifyDataSetChanged();
    }
    
    
    /* Open the passed file and process it so that it becomes a nice Song.
	 * so that the rest of the structure can work abstract with the type Song.
	 */
    public Sang readSangFromFile( File file ) {
    	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(myApp);
		String numDelimiter = sharedPrefs.getString("number_delimiter", myApp.getString(R.string.serverFileDelimiter));
		String fileEnding = sharedPrefs.getString("file_ending", myApp.getString(R.string.SongFileEnding));
    	Sang retSang = new Sang();
    	String path = file.getPath();
    	int basePathLength = myApp.getFilesDir().getPath().length();
    	retSang.setTitle( path );
    	String[] split = path.split( numDelimiter );
    	int splitLen = split.length;
    	if( fileEnding.matches("(?i).*" + numDelimiter + ".*") ) { //The file ending must not contain the character that is used in separating chapter-number from song-number in the file names. If so the code will fail and hence we make assertion fail instead.
    		retSang.setChapter( -1 );
    		retSang.setNumber( -1 );
    	}
    	else {
	    	try{
	    	retSang.setChapter( Integer.parseInt( split[splitLen-2].substring(basePathLength+1) ) ); //read all numbers starting after the base path up to delimiter
	    	if( split[splitLen-1].length() > fileEnding.length() ) {
	    		retSang.setNumber( Integer.parseInt( split[splitLen-1].substring(0, split[splitLen-1].length()-fileEnding.length() ) ) ); //after the - it is number and thus the rest except .txt is the song number within that chapter
	    	}
	    	}
	    	catch(NumberFormatException e) {//If something is wrong with the format, do not crash the app.
	    		retSang.setChapter( -1 );
	    		retSang.setNumber( -1 );
	    	}
    	}
        //get the file as a stream 
        try{
	        StringBuilder buffer = new StringBuilder();
	        BufferedReader bR= new BufferedReader( new FileReader(file) );
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
					if( !str.equals("") ) {
						buffer.append(str);
					}
	              	
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
	        		if( !buffer.toString().equals("") ) { //If a tag is on a own row then the buffer will be empty first time it reaches here, do not append newline character.
	        			buffer.append("\n");
	        		}
	        		buffer.append(str);
	        	}
	        }
	        //When exiting, do what you must with the rest!
	        setSangContent(state, retSang, buffer.toString());
	        bR.close();
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

}
