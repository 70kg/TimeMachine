package me.drakeet.transformer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

/**
 * @author drakeet
 */
public class Preferences {

    private static SharedPreferences preferences;


    @NonNull public static SharedPreferences defaultPreferences(Context context) {
        if (preferences != null) {
            return preferences;
        }
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences;
    }


    private Preferences() {
        throw new AssertionError();
    }
}
