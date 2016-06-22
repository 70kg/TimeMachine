package me.drakeet.transformer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import static android.support.v4.app.NotificationCompat.PRIORITY_MAX;

/**
 * Created by drakeet on 16/6/21.
 */

public class Notifications {

    private static final int SIMPLE_ID = 5238;

    private Notifications() {
    }

    public static void simple(@NonNull Context context,
            @NonNull CharSequence title,
            @NonNull CharSequence message,
            int smallIconResId, Class<?> targetActivity) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setPriority(PRIORITY_MAX)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSmallIcon(smallIconResId);
        Intent intent = new Intent(context, targetActivity);
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
