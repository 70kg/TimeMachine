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


    @NonNull protected TranslationService getService() {
        return service;
    }


    @NonNull protected ObservableHelper getObservableHelper() {
        return service.helper;
    }


    public abstract void prepare();

    public abstract void handleContent(@NonNull String content);
}
