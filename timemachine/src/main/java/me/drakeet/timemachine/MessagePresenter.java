package me.drakeet.timemachine;

import android.support.annotation.NonNull;
import android.util.Log;

import static me.drakeet.timemachine.Objects.requireNonNull;

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
        this.view = requireNonNull(view);
        this.service = requireNonNull(service);
        service.setPresenter(this);
    }


    @Override public void addNewIn(@NonNull final Message message) {
        requireNonNull(message);
        view.onNewIn(message);
        service.onNewIn(message);
    }


    @Override public void addNewOut(@NonNull final Message message) {
        requireNonNull(message);
        view.onNewOut(message);
        service.onNewOut(message);
    }


    @Override public boolean onInterceptNewOut(@NonNull final Message message) {
        requireNonNull(message);
        if (service.onInterceptNewOut(message)) {
            Log.d(TAG, "The new out Message has been intercepted by Service: "
                + message.toString());
            return true;
        } else {
            return false;
        }
    }


    @Override public void setInputText(@NonNull final CharSequence text) {
        requireNonNull(text);
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
