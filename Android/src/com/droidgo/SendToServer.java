package com.droidgo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.droidgo.settings.Preferences;

import android.os.AsyncTask;

class SendToServer extends AsyncTask<Void, Integer, Void> {

	String message;
	
	public SendToServer(String msg) {
		setServer(msg);
	}

	@Override
	protected void onPreExecute() {
		// update the UI immediately after the task is executed
		super.onPreExecute();

	}

	@Override
	protected Void doInBackground(Void... params) {

		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("http://" + MainActivity.iSettings.getString(Preferences.IP_ADDRESS, "") +"/droidgo/app.php");
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
					2);
			nameValuePairs.add(new BasicNameValuePair("id", "5656"));
			nameValuePairs.add(new BasicNameValuePair("message",
					getMessage()));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			httpclient.execute(httppost);

		} catch (ClientProtocolException e) {
//			System.out.println(e);
		} catch (IOException e) {
//			System.out.println(e);
		}

		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);

	}

	public String getMessage() {
		return message;
	}

	public void setServer(String server) {
		this.message = server;
	}
}
