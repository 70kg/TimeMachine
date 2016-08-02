package me.drakeet.transformer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import static android.support.v4.app.NotificationCompat.PRIORITY_MAX;
import static me.drakeet.timemachine.Objects.requireNonNull;

/**
 * Created by drakeet on 16/6/21.
 */

public class Notifications {

    private static final int SIMPLE_ID = 5238;


    private Notifications() {
        throw new AssertionError();
    }


    public static void simple(@NonNull final Context context,
                              @NonNull final CharSequence title,
                              @NonNull final CharSequence message,
                              int smallIconResId,
                              @NonNull final Class<?> targetActivity) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
            .setPriority(PRIORITY_MAX)
            .setContentTitle(requireNonNull(title))
            .setContentText(requireNonNull(message))
            .setVibrate(new long[] { 0 })
            .setAutoCancel(true)
            .setSmallIcon(smallIconResId);
        Intent intent = new Intent(context, requireNonNull(targetActivity));
        // Sets the Activity to start in a new, empty task
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent notifyPendingIntent = PendingIntent
            .getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(notifyPendingIntent);
        NotificationManager notificationManager =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(SIMPLE_ID, builder.build());
    }
}
