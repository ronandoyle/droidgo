package com.droidgo.settings;
 
import com.droidgo.DROIDGO;
import com.droidgo.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;
 
public class Preferences extends PreferenceActivity {

	private static final String TAG = "droidgo.prefs";
	public static final String IP_ADDRESS = "ipAddress";
	public static final String PORT = "8080";
	public static final String VIDEO_STREAM = "videoStream";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
		PreferenceManager.setDefaultValues(Preferences.this, R.xml.preferences, false);
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		intent.setClass(Preferences.this, DROIDGO.class);
		startActivity(intent);
		finish();
		return;
	}


}