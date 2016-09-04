package me.drakeet.transformer;

import android.support.annotation.NonNull;

import static me.drakeet.timemachine.Objects.requireNonNull;

/**
 * @author drakeet
 */
public abstract class MessageServiceDelegate {

    @NonNull private final TranslationService service;


    public MessageServiceDelegate(@NonNull TranslationService service) {
        this.service = requireNonNull(service);
    }


    @NonNull public TranslationService getService() {
        return service;
    }


    @NonNull protected ObservableHelper getObservableHelper() {
        return service.helper;
    }


    protected abstract void prepare();

    protected abstract void handleContent(@NonNull String content);
}
