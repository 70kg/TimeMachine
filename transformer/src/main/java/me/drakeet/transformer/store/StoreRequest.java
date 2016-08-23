package me.drakeet.transformer.store;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static me.drakeet.timemachine.Objects.requireNonNull;

/**
 * @author drakeet
 */
final class StoreRequest {

    @NonNull Object request;
    @Nullable ResultObserver observer;


    StoreRequest(@NonNull Object request) {
        this.request = requireNonNull(request);
    }


    StoreRequest(@NonNull Object request, @Nullable ResultObserver observer) {
        this.request = requireNonNull(request);
        this.observer = observer;
    }
}
