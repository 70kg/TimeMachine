package me.drakeet.timemachine;

import android.support.annotation.NonNull;

/**
 * @author drakeet
 */
public class MessagePresenter implements CoreContract.Presenter {

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


    @Override public void setInputText(@NonNull CharSequence text) {
        view.setInputText(text);
    }


    @Override public void clean() {
        view.onClean();
        service.onClean();
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
