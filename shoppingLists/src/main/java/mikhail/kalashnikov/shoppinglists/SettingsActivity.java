package mikhail.kalashnikov.shoppinglists;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {
	final static String KEY_PREF_USE_CATEGORY = "pref_use_category";
	final static String KEY_PREF_USE_CATEGORY_ON_MAIN_SCREEN = "pref_category_main_screen";
	final static String KEY_PREF_RECIPE_USE_DEFAULT = "pref_recipe_use_default";
	final static String KEY_PREF_RECIPE_DEFAULT_LIST = "pref_recipe_default_list";
	final static String KEY_PREF_RECIPE_USE_RECIPE_NAME_FOR_LIST = "pref_recipe_use_recipe_name_for_list";
	final static String KEY_PREF_RECIPE_DEFAULT_CATEGORY = "pref_recipe_default_category";

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
        CheckBoxPreference useRecipeAsList = (CheckBoxPreference) findPreference(KEY_PREF_RECIPE_USE_RECIPE_NAME_FOR_LIST);
        EditTextPreference listName = (EditTextPreference) findPreference(KEY_PREF_RECIPE_DEFAULT_LIST);
        listName.setEnabled(!useRecipeAsList.isChecked());
	}

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if(key.equals(KEY_PREF_RECIPE_USE_RECIPE_NAME_FOR_LIST)){
            @SuppressWarnings("deprecation")
            EditTextPreference listName = (EditTextPreference) findPreference(KEY_PREF_RECIPE_DEFAULT_LIST);
            listName.setEnabled(!sharedPreferences.getBoolean(KEY_PREF_RECIPE_USE_RECIPE_NAME_FOR_LIST, false));
        }

    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
