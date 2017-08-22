package me.drakeet.transformer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author drakeet
 */
public class KeepAliveReceiver extends BroadcastReceiver {

    @Override public void onReceive(Context context, Intent intent) {
        Intent restart = new Intent(context, Inhibitor.class);
        context.startService(restart);
    }
}
