package com.fatfish.chengjian.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fatfish.chengjian.detectx.App;
import com.fatfish.chengjian.detectx.R;

import java.util.Objects;


public class PreferenceCfgActivity extends AppCompatActivity {
    private final static String TAG = PreferenceCfgActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getFragmentManager().beginTransaction().replace(R.id.content, new SettingsFragment()).commit();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                super.onOptionsItemSelected(item);
        }
        return true;
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        public SettingsFragment() {
            Log.d(TAG, "constructing SettingsFragment");
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_settings_layout);
            setSummaries();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Context context = getActivity().getApplicationContext();
            if (key.equals(context.getString(R.string.enable_logging_out_img_logs_key))) {
                setSummaryForEnableSaving();
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        private void setSummaries() {
            setSummaryForEnableSaving();
        }


        private void setSummaryForEnableSaving() {
            boolean value = getPref_enableSaving(getContext());
            Preference preference = findPreference(getContext().getResources().getString(R.string.enable_logging_out_img_logs_key));
            if (value)
                preference.setSummary("images saved to:" + App.EXTERNAL_LOCATION_ROOT);
            else
                preference.setSummary("images won't be saved");
        }

        public boolean getPref_enableSaving(Context context) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String key = context.getResources().getString(R.string.enable_logging_out_img_logs_key);
            boolean defaultVal = context.getResources().getBoolean(R.bool.enable_logging_out_img_logs_default_value);
            return preferences.getBoolean(key, defaultVal);
        }


    }


}