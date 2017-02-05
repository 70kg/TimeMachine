package me.drakeet.transformer.module;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;
import me.drakeet.agera.eventbus.AgeraBus;
import me.drakeet.timemachine.Message;
import me.drakeet.timemachine.MessageFactory;
import me.drakeet.timemachine.TimeKey;
import me.drakeet.timemachine.message.TextContent;
import me.drakeet.transformer.MainActivity;
import me.drakeet.transformer.MessageServiceDelegate;
import me.drakeet.transformer.NewInEvent;
import me.drakeet.transformer.Notifications;
import me.drakeet.transformer.R;
import me.drakeet.transformer.TranslationService;

import static me.drakeet.timemachine.Objects.requireNonNull;
import static me.drakeet.timemachine.store.MessageStore.messagesStore;

/**
 * @author drakeet
 */
public class TomatoDelegate extends MessageServiceDelegate {

    private static final String ACTION_TOMATO = "transformer.intent.action.TOMATO";
    private static final String TOMATO = "TOMATO";

    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;
    private final Context context;


    public TomatoDelegate(@NonNull TranslationService service) {
        super(service);
        this.context = service.getContext();
    }


    @Override public void prepare() {
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(ACTION_TOMATO);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
    }


    @Override public void handleContent(@NonNull String content) {
        // TODO: 2016/11/13 cancel
        final long interval = 60 * 60 * 1000;
        long startTime = SystemClock.elapsedRealtime() + interval;
        alarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            startTime, interval,
            alarmIntent);
    }


    public static class AlarmReceiver extends BroadcastReceiver {

        @Override public void onReceive(Context context, Intent intent) {
            if (ACTION_TOMATO.equals(intent.getAction())) {
                postMessage(context.getApplicationContext(), "距离上次提醒，已经过了一小时了");
            }
        }


        private void postMessage(Context appContext, String content) {
            MessageFactory factory = new MessageFactory.Builder()
                .setFromUserId(TOMATO)
                .setToUserId(TimeKey.userId)
                .build();
            final String id = String.valueOf(System.currentTimeMillis());
            final Message in = factory.newMessage(new TextContent(content), id);
            messagesStore(appContext).insert(in, succeeded -> {
                Log.d("insert", "result: " + succeeded);
                if (succeeded) {
                    if (AgeraBus.repository().hasObservers()) {
                        AgeraBus.repository().accept(new NewInEvent(in));
                    } else {
                        Log.d(TOMATO, "DeadEvent");
                        notify(appContext, in);
                    }
                }
            });
        }

        private void notify(Context context, @NonNull final Message message) {
            requireNonNull(message);
            String title = message.fromUserId;
            String content = ((TextContent) message.content).text;
            Notifications.simple(context, title, content,
                R.drawable.ic_notification, MainActivity.class);
        }
    }
}
