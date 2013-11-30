package com.example.bookoverflowv1;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
 
public class PrefsActivity extends PreferenceActivity{
        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                addPreferencesFromResource(R.xml.prefs);
                setTheme(android.R.style.Animation_Translucent);
                MainActivity.keep_track=false;
                //findViewById(android.R.id.list).setBackgroundColor(Color.WHITE);
                /*ListPreference ser = (ListPreference) findPreference("ServerAddress");
                ser.setValueIndex(0);*/
        }
}