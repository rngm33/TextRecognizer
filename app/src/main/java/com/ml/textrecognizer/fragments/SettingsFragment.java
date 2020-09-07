package com.ml.textrecognizer.fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.ml.textrecognizer.R;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SeekBarPreference skBarSpeed, skBarPitch;
    private ListPreference lp;

    public SettingsFragment() {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // To remove:
//            setDivider(null);

        // To change:
        setDivider(ContextCompat.getDrawable(getActivity(), R.drawable.ic_speaker));
        setDividerHeight(1);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_preferences);


        skBarSpeed = this.findPreference("key_speed");
        skBarPitch = this.findPreference("key_pitch");
        lp = findPreference("voice");
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);


    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("key_speed")) {
            int radius = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getInt("key_speed", 50);
            Log.d("voice", radius + "");
//                skBarSpeed.setSummary(radius + "");
            sharedPreferences.edit().putInt("keyspeed", radius).commit();
        }

        if (key.equals("key_pitch")) {
            int radius = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt("key_pitch", 1);
            Log.d("pitch", radius + "");
//                skBarPitch.setSummary(radius + "");
            sharedPreferences.edit().putInt("keypitch", radius).commit();
        }

        if (key.equals("voice")) {
            String value = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("voice", "-1");
//                lp.setSummary(value);
            sharedPreferences.edit().putString("keyvoice", value).commit();
        }
    }
}
