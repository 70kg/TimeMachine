package me.drakeet.timemachine;

import android.support.annotation.NonNull;

/**
 * The entrance of time machine SDK
 *
 * @author drakeet
 */
public class TimeKey {

    public static String appName;
    public static String userId;


    private TimeKey() throws IllegalAccessException {
        throw new IllegalAccessException();
    }


    /**
     * Initialize the time machine SDK
     *
     * @param appName your app name
     * @param userId your current user uuid.
     */
    public static void install(@NonNull final String appName, @NonNull final String userId) {
        TimeKey.appName = appName;
        TimeKey.userId = userId;
    }


    public static boolean isCurrentUser(@NonNull final String userId) {
        if (TimeKey.userId == null) {
            throw new RuntimeException("TimeKey.userId is null, did you initialize the TimeKey?");
        } else {
            return userId != null && TimeKey.userId.equals(userId);
        }
    }
}
