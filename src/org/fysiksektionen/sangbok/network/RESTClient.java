package org.fysiksektionen.sangbok.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/*  
 * This entire class was blatantly stolen from 
 * http://senior.ceng.metu.edu.tr/2009/praeda/2009/01/11/a-simple-restful-client-at-android/
 */
public class RESTClient {

	private static String convertStreamToString(InputStream inputStream) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(inputStream));
		StringBuilder stringBuilder = new StringBuilder();

		String line = null;
		try {
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return stringBuilder.toString();
	}

	/*
	 * This is a test function which will connects to a given rest service and
	 * prints it's response to Android Log with labels "RESTClient".
	 */
	private static String connect(String url) {

		HttpClient httpclient = new DefaultHttpClient();

		// Prepare a request object
		HttpGet httpget = new HttpGet(url);

		// Execute the request
		HttpResponse response;
		InputStream instream = null;
		try {
			response = httpclient.execute(httpget);
			// Examine the response status
			Log.i("RESTClient", String.format(
					"HTTP request to {0} returned with status message {1}.",
					url, response.getStatusLine().toString()));

			// Get hold of the response entity
			HttpEntity entity = response.getEntity();
			// If the response does not enclose an entity, there is no need
			// to worry about connection release

			if (entity != null) {

				// A Simple JSON Response Read
				instream = entity.getContent();
				String result = convertStreamToString(instream);
				return result;
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (instream != null) {
				try {
					instream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return "";
	}

	public static void logJSONObject(String url) {
		String result = RESTClient.connect(url);
		try {
			// A Simple JSONObject Creation
			JSONObject json = new JSONObject(result);
			Log.i("RESTClient", "<jsonobject>\n" + json.toString()
					+ "\n</jsonobject>");

			// A Simple JSONObject Parsing
			JSONArray nameArray = json.names();
			JSONArray valArray = json.toJSONArray(nameArray);
			for (int i = 0; i < valArray.length(); i++) {
				Log.i("RESTClient",
						"<jsonname" + i + ">\n" + nameArray.getString(i)
								+ "\n</jsonname" + i + ">\n" + "<jsonvalue" + i
								+ ">\n" + valArray.getString(i)
								+ "\n</jsonvalue" + i + ">");
			}

			// A Simple JSONObject Value Pushing
			json.put("sample key", "sample value");
			Log.i("RESTClient", "<jsonobject>\n" + json.toString()
					+ "\n</jsonobject>");

			// Closing the input stream will trigger connection release
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}