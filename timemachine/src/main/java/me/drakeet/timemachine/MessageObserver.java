package me.drakeet.timemachine;

import android.support.annotation.NonNull;

/**
 * @author drakeet
 */
public interface MessageObserver {

    void onNewOut(@NonNull Message message);
    void onNewIn(@NonNull Message message);
    void onMessageClick(@NonNull Message message);
    void onMessageLongClick(@NonNull Message message);
}
