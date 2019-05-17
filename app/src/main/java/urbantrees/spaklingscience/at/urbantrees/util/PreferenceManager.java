package urbantrees.spaklingscience.at.urbantrees.util;

import android.app.Activity;
import android.content.SharedPreferences;

/**
 * Manages shared prefs.
 *
 * @author Laurenz Fiala
 * @since 2018/08/14
 */
public class PreferenceManager extends HasContext {

    /**
     *
     */
    private static final String PREFS_KEY = "urbantrees-prefs-main";

    /**
     *
     */
    private static final String FIRST_LAUNCH_PREF_KEY = "isFirstLaunch";

    /**
     *
     */
    private static final String COLLECT_DATA_PREF_KEY = "isTreeDataCollect";

    private SharedPreferences prefs;

    private SharedPreferences.Editor prefsEditor;

    public PreferenceManager(Activity context) {
        super(context, null);

        this.prefs = context.getSharedPreferences(PREFS_KEY, 0);
        this.prefsEditor = this.prefs.edit();
    }

    public void setFirstLaunch(final boolean isFirstLaunch) {
        this.prefsEditor.putBoolean(FIRST_LAUNCH_PREF_KEY, isFirstLaunch);
        this.prefsEditor.commit();
    }

    public void setTreeDataCollect(final boolean isCollectData) {
        this.prefsEditor.putBoolean(COLLECT_DATA_PREF_KEY, isCollectData);
        this.prefsEditor.commit();
    }

    public boolean isFirstLaunch() {
        return this.prefs.getBoolean(FIRST_LAUNCH_PREF_KEY, true);
    }

    public boolean isTreeDataCollect() {
        return this.prefs.getBoolean(COLLECT_DATA_PREF_KEY, true);
    }

}
