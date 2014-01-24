/***
	Copyright (c) 2009 
	Author: Stefan Klumpp <stefan.klumpp@gmail.com>
	Web: http://stefanklumpp.com

	Licensed under the Apache License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may obtain
	a copy of the License at
		http://www.apache.org/licenses/LICENSE-2.0
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */

package org.fysiksektionen.sangbok.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPInputStream;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.style.LineHeightSpan.WithDensity;
import android.util.Log;

public class HttpClient {
	private static final String TAG = "HttpClient";

	public static JSONObject SendHttpGet(String URL) {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGetRequest = (HttpGet) withHeaders(new HttpGet(URL));

		try {
			HttpResponse response = (HttpResponse) httpClient.execute(httpGetRequest);

			return parseResponseToObject(response);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static JSONObject getJSONObjectFromPost(String URL, JSONObject jsonObjSend) {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost httpPostRequest = new HttpPost(URL);

		try {
			StringEntity se;
			se = new StringEntity(jsonObjSend.toString());
			// Set HTTP parameters
			httpPostRequest.setEntity(se);
			httpPostRequest = (HttpPost) withHeaders(httpPostRequest);

			HttpResponse response = (HttpResponse) httpclient.execute(httpPostRequest);

			return parseResponseToObject(response);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static JSONArray getJSONArray(String URL) {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGetRequest = (HttpGet) withHeaders(new HttpGet(URL));

		try {
			HttpResponse response = (HttpResponse) httpClient.execute(httpGetRequest);

			return parseResponseToArray(response);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private static HttpRequest withHeaders(HttpRequest request) {
		request.setHeader("Accept", "application/json");
		request.setHeader("Content-type", "application/json");
		request.setHeader("Accept-Encoding", "gzip");
		return request;
	}

	private static JSONObject parseResponseToObject(HttpResponse response) {
		// Get hold of the response entity (-> the data):
		HttpEntity entity = response.getEntity();

		if (entity != null) {
			// Read the content stream
			InputStream instream;
			try {
				instream = entity.getContent();
				Header contentEncoding = response.getFirstHeader("Content-Encoding");
				if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
					instream = new GZIPInputStream(instream);
				}
				// convert content stream to a String
				String resultString = convertStreamToString(instream);
				instream.close();

				// Transform the String into a JSONObject
				JSONObject jsonObjRecv;
				jsonObjRecv = new JSONObject(resultString);

				return jsonObjRecv;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	
	private static JSONArray parseResponseToArray(HttpResponse response) {
		HttpEntity entity = response.getEntity();
		
		if (entity != null) {
			InputStream instream;
			try {
				instream = entity.getContent();
				Header contentEncoding = response.getFirstHeader("Content-Encoding");
				if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
					instream = new GZIPInputStream(instream);
				}
				String resultString = convertStreamToString(instream);
				instream.close();
				
				JSONArray jsonArrRecv;
				jsonArrRecv = new JSONArray(resultString);
				
				return jsonArrRecv;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		return null;
	}

	private static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 * 
		 * (c) public domain:
		 * http://senior.ceng.metu.edu.tr/2009/praeda/2009/01/
		 * 11/a-simple-restful-client-at-android/
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

}