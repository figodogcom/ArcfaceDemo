package com.arcsoft.arcfacedemo.activity;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.arcsoft.arcfacedemo.R;

public class SettingsActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }



    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        public static String PREVIEW_5PERCENT = "preview_5percent";
        public static String SQUARE_10PERCENT = "Square_10percent";



        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

//            final SwitchPreferenceCompat switchPreferenceCompat = findPreference("alive");
//            final ListPreference listPreference = findPreference("percent");

//            SharedPreferences sharedPreferences = getContext().getSharedPreferences("data", Context.MODE_PRIVATE);
//            final SharedPreferences.Editor editor = sharedPreferences.edit();
//             getPreferenceManager().getSharedPreferences();


//            switchPreferenceCompat.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//                @Override
//                public boolean onPreferenceChange(Preference preference, Object newValue) {
//                    Log.i("xxxxx","switchPreferenceCompat = " + (Boolean)newValue);
////                    editor.putBoolean("check_alive",(Boolean)newValue);
////                    editor.commit();
////                    return false;
//                    return true;
//                }
//            });
//
//
//            listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//                @Override
//                public boolean onPreferenceChange(Preference preference, Object newValue) {
//                    Log.i("xxxxx","listPreference = " + (String)newValue);
////                    editor.putString("percent",(String)newValue);
////                    editor.commit();
////                    return false;
//                    return true;
//
//                }
//            });


        }
    }
}