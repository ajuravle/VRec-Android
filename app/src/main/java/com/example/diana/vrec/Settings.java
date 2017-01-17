package com.example.diana.vrec;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by Diana on 05.01.2017.
 */

public class Settings extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.my_preferences);
    }
}
