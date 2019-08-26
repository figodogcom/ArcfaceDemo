package com.arcsoft.arcfacedemo.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.arcsoft.arcfacedemo.activity.SettingsActivity;
import com.arcsoft.face.FaceFeature;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class Preference {
    private SharedPreferences sharedPreferences;

    public Preference(Context context) {
        Log.i(TAG, "xxxxx: " + context.getPackageName());
//        sharedPreferences = context.getSharedPreferences(context.getPackageName() + "_preferences",Context.MODE_PRIVATE);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);


    }

    public Boolean getPreviewAlive() {
        return sharedPreferences.getBoolean("preview_alive", true);
    }

    public Boolean getPreviewFivePercent() {
        return sharedPreferences.getBoolean("preview_preview_5percent", true);
    }

    public Boolean getPreviewSquareTenPercent() {
        return sharedPreferences.getBoolean("preview_square_10percent", true);
    }


//        <SwitchPreferenceCompat
//    app:key="preview_preview_5percent"
//    app:title="面积占preview5%" />
//
//        <SwitchPreferenceCompat
//    app:key="preview_square_10percent"
//    app:title="面积占正方体5%" />
//
//        <!--<ListPreference-->
//            <!--app:defaultValue="占preview5%"-->
//            <!--app:entries="@array/reply_entries"-->
//            <!--app:entryValues="@array/reply_values"-->
//            <!--app:key="percent"-->
//            <!--app:title="preview的百分比"-->
//            <!--app:useSimpleSummaryProvider="true" />-->
//
//
//    </PreferenceCategory>
//
//    <PreferenceCategory app:title="活体设置">
//        <SwitchPreferenceCompat
//    app:key="preview_alive"
//    app:title="是否活体" />

//        <SwitchPreferenceCompat
//    app:key="preview_alive"
//    app:title="是否活体" />
//
//    <SwitchPreferenceCompat
//    app:key="preview_preview_5percent"
//    app:title="面积占preview5%" />
//
//    <SwitchPreferenceCompat
//    app:key="preview_square_10percent"
//    app:title="面积占正方体5%" />


}
