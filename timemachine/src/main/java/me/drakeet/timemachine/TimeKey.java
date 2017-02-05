package me.drakeet.timemachine;

import android.support.annotation.NonNull;
import me.drakeet.timemachine.message.TextContent;

import static java.util.Objects.requireNonNull;

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
     * Initialize the time machine SDK,
     * and register some base Message Content e.g. {@link TextContent}
     *
     * @param appName your app name
     * @param userId your current user uuid.
     */
    public static void install(@NonNull final String appName, @NonNull final String userId) {
        TimeKey.appName = requireNonNull(appName);
        TimeKey.userId = requireNonNull(userId);
    }


    public static boolean isCurrentUser(@NonNull final String userId) {
        requireNonNull(userId);
        requireNonNull(TimeKey.userId, "TimeKey.userId is null, did you initialize the TimeKey?");
        return TimeKey.userId.equals(userId);
    }
}
