package org.fysiksektionen.sangbok;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

public class SynchronizeHandler {
	private static int readTimeOutInms = 15000;
	private static int connectionTimeOutInms = 20000;
	
	public static int sync(Activity myApp) {
		Integer upDate = 1;
		ConnectivityManager connMgr = (ConnectivityManager) myApp.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(myApp);
		String serverURL = sharedPrefs.getString("server_URL", myApp.getString(R.string.serverURL));
		String instructionURL = sharedPrefs.getString("server_instruction_URL", myApp.getString(R.string.serverInstructionURL));
		String chapterNamesURL = myApp.getString(R.string.chapter_names_file);
		String numDelimiter = sharedPrefs.getString("number_delimiter", myApp.getString(R.string.serverFileDelimiter));
		String fileEnding = sharedPrefs.getString("file_ending", myApp.getString(R.string.SongFileEnding));
	    if (networkInfo != null && networkInfo.isConnected()) {//We have Internet. Time to sync!
	    	InputStream is = null;
	    	InputStream sangStream = null;
	    	InputStream chapterStream = null;
	    	FileOutputStream outputStream = null;
	    	HttpURLConnection conn = null;
	        try {
	        	//Set up connection with the download site, and try to download the instruction-document
	            URL url = new URL( serverURL + instructionURL  );
	            conn = (HttpURLConnection) url.openConnection();
	            conn.setReadTimeout(readTimeOutInms);
	            conn.setConnectTimeout(connectionTimeOutInms);
	            conn.setRequestMethod("GET");
	            conn.setDoInput(true);
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
			            conn.setReadTimeout(readTimeOutInms);
			            conn.setConnectTimeout(connectionTimeOutInms);
			            conn.setRequestMethod("GET");
			            conn.setDoInput(true);
			            conn.connect();
			            sangStream = conn.getInputStream();
			            //Write the songs to file
			            outputStream = myApp.openFileOutput( Integer.toString(readingChpt) + numDelimiter + Integer.toString(i) + fileEnding, Context.MODE_PRIVATE);
			            outputStream.write(new Scanner(sangStream,"UTF-8").useDelimiter("\\A").next().getBytes());
			            outputStream.close();
	              	}
	              	
		        }
	            
	            // close streams
	            is.close();
	            sangStream.close();
	            
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
	        try{
	            //Download the chapter names while at it!
	            URL url = new URL( serverURL + chapterNamesURL  );
	            conn = (HttpURLConnection) url.openConnection();
	            conn.setReadTimeout(readTimeOutInms);
	            conn.setConnectTimeout(connectionTimeOutInms);
	            conn.setRequestMethod("GET");
	            conn.setDoInput(true);
	            conn.connect();
	            chapterStream = conn.getInputStream();
	            outputStream = myApp.openFileOutput( chapterNamesURL, Context.MODE_PRIVATE);
	            outputStream.write(new Scanner(chapterStream,"UTF-8").useDelimiter("\\A").next().getBytes());
	            outputStream.close();
	            
	            // close stream and disconnect
	            chapterStream.close();
	            conn.disconnect();
	        }
	        catch( IOException e ) {
	        	e.printStackTrace();
	        	upDate = 6;
	        }
	
	    }else {//We do not have Internet, display error
	    	upDate = 2;
		}
	    return upDate;
	}
}
