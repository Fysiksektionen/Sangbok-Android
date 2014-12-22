package se.kth.f.sangbok;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
		int status = 0;     // 1) We do not have Internet connection
							// 2) Cannot connect to host
							// 3) We couldn't write to file locally
							// 4) Wired...
		ConnectivityManager connMgr = (ConnectivityManager) myApp.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(myApp);
		String serverURL = sharedPrefs.getString("server_URL", myApp.getString(R.string.serverURL));
	    if (networkInfo != null && networkInfo.isConnected()) {//We have Internet. Time to sync!
	    	InputStream inputStream = null;
	    	FileOutputStream outputStream = null;
	    	HttpURLConnection conn = null;
	    	Scanner scanner = null;
	        try{
	        	//Connect to the server
	            URL url = new URL( serverURL );
	            conn = (HttpURLConnection) url.openConnection();
	            conn.setReadTimeout(readTimeOutInms);
	            conn.setConnectTimeout(connectionTimeOutInms);
	            conn.setRequestMethod("GET");
	            conn.setDoInput(true);
	            conn.connect();
	            inputStream = conn.getInputStream();
	        }
            catch( IOException e ) {
	        	e.printStackTrace();
	        	status = 2;
	        	return status;
            }
	        try {
	            //Download the whole database as raw text!
	            outputStream = myApp.openFileOutput( myApp.getString(R.string.current_song_book_databese_file_name), Context.MODE_PRIVATE);
	            scanner = new Scanner(inputStream, "UTF-8");
//	            System.out.println("B‰‰!!!");
//	            System.out.println(scanner.useDelimiter("\\A").next().getBytes());
	            outputStream.write(scanner.useDelimiter("\\A").next().getBytes());
	        }
            catch( IOException e ) {
	        	e.printStackTrace();
	        	status = 3;
	        	return status;
            }
	        try {
	            // close stream and disconnect
	            scanner.close();
	            inputStream.close();
	            outputStream.close();
	            conn.disconnect();
	        }
	        catch( IOException e ) {
	        	e.printStackTrace();
	        	status = 4;
	        	return status;
	        }
	    }else {//We do not have Internet, display error
	    	status = 1;
		}
	    return status;
	}
}
