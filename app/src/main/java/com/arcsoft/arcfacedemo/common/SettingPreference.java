package com.arcsoft.arcfacedemo.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class SettingPreference {
    private SharedPreferences sharedPreferences;

    public SettingPreference(Context context) {
        Log.i(TAG, "xxxxx: " + context.getPackageName());
//        sharedPreferences = context.getSharedPreferences(context.getPackageName() + "_preferences",Context.MODE_PRIVATE);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);


    }

    public boolean getPreviewAlive() {
        return sharedPreferences.getBoolean("preview_alive", true);
    }

    public String getPreviewPercent() {
        return sharedPreferences.getString("preview_preview_percent","0");
    }

    public String getPreviewSquarePercent() {
        return sharedPreferences.getString("preview_square_percent", "0");
    }

    public String getEngine(){
        return sharedPreferences.getString("engine","arcsoft");
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
