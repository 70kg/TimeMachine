package me.drakeet.transformer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import me.drakeet.timemachine.Message;
import me.drakeet.timemachine.MessageObserver;
import me.drakeet.timemachine.Objects;

import static android.support.v7.widget.StaggeredGridLayoutManager.TAG;

/**
 * @author drakeet
 */
public class TransformObserver implements MessageObserver {

    @NonNull private final Context context;


    public TransformObserver(@NonNull final Context context) {
        this.context = Objects.requireNonNull(context);
    }


    @Override public void onNewOut(@NonNull final Message message) {
        Log.v(TAG, "onNewOut: " + message.toString());
    }


    @Override public void onNewIn(@NonNull final Message message) {
        Log.v(TAG, "onNewIn: " + message.toString());
    }


    @Override public void onMessageClick(@NonNull final Message message) {
        Log.v(TAG, "onMessageClicked: " + message.toString());
    }


    @Override public void onMessageLongClick(@NonNull final Message message) {
        Log.v(TAG, "onMessageLongClicked: " + message.toString());
    }
}
