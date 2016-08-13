package me.drakeet.timemachine;

import android.support.annotation.NonNull;
import me.drakeet.multitype.MultiTypePool;
import me.drakeet.timemachine.message.InTextContent;
import me.drakeet.timemachine.message.InTextMessageViewProvider;
import me.drakeet.timemachine.message.OutTextContent;
import me.drakeet.timemachine.message.OutTextMessageViewProvider;

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
     * and register some base Message Content e.g. {@link InTextContent} & {@link OutTextContent}
     *
     * @param appName your app name
     * @param userId your current user uuid.
     */
    public static void install(@NonNull final String appName, @NonNull final String userId) {
        TimeKey.appName = requireNonNull(appName);
        TimeKey.userId = requireNonNull(userId);
        /* Default registers */
        MultiTypePool.register(OutTextContent.class, new OutTextMessageViewProvider());
        MultiTypePool.register(InTextContent.class, new InTextMessageViewProvider());
    }


    public static boolean isCurrentUser(@NonNull final String userId) {
        requireNonNull(userId);
        requireNonNull(TimeKey.userId, "TimeKey.userId is null, did you initialize the TimeKey?");
        return userId != null && TimeKey.userId.equals(userId);
    }
}
