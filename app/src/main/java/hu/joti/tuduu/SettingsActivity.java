package hu.joti.tuduu;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;

public class SettingsActivity extends AppCompatPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.settings);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        setResult(RESULT_CANCELED);
        finish();
        break;
    }
    return true;
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    Preference pref = findPreference(key);

    if (pref instanceof ListPreference) {
      ListPreference listPref = (ListPreference) pref;
      pref.setSummary(listPref.getEntry());
    }
  }
}
