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

    private SharedPreferences prefs;

    private SharedPreferences.Editor prefsEditor;

    public PreferenceManager(Activity context) {
        super(context);

        this.prefs = context.getSharedPreferences(PREFS_KEY, 0);
        this.prefsEditor = this.prefs.edit();
    }

    public void setFirstLaunch(final boolean isFirstLaunch) {
        this.prefsEditor.putBoolean(FIRST_LAUNCH_PREF_KEY, isFirstLaunch);
        this.prefsEditor.commit();
    }

    public boolean isFirstLaunch() {
        return this.prefs.getBoolean(FIRST_LAUNCH_PREF_KEY, true);
    }

}
