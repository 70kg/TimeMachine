package me.drakeet.timemachine;

import android.support.annotation.NonNull;
import android.util.Log;

/**
 * @author drakeet
 */
public class MessagePresenter implements CoreContract.Presenter {

    private static final String TAG = MessagePresenter.class.getSimpleName();

    private CoreContract.View view;
    private CoreContract.Service service;


    public MessagePresenter(
        @NonNull final CoreContract.View view,
        @NonNull final CoreContract.Service service) {
        this.view = view;
        this.service = service;
        service.setPresenter(this);
    }


    @Override public void addNewIn(@NonNull final Message message) {
        this.view.onNewIn(message);
    }


    @Override public void addNewOut(@NonNull final Message message) {
        view.onNewOut(message);
        service.onNewOut(message);
    }


    @Override public boolean onInterceptNewOut(@NonNull final Message message) {
        if (service.onInterceptNewOut(message)) {
            return true;
        } else {
            Log.d(TAG, "The new out Message(" + message.getContent() +
                ") has been intercepted by Service");
        }
        return false;
    }


    @Override public void setInputText(@NonNull final CharSequence text) {
        view.setInputText(text);
    }


    @Override public void clear() {
        view.onClear();
        service.onClear();
    }


    @Override public void notifyDataSetChanged() {
        view.onDataSetChanged();
    }


    @Override public void start() {
        service.start();
    }


    @Override public void stop() {
        service.stop();
    }

}
