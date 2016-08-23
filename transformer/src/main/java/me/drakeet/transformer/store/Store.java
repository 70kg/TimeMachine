package me.drakeet.transformer.store;

import android.support.annotation.NonNull;

/**
 * @author drakeet
 */
interface Store<T> {

    void insert(@NonNull T data);
    void insert(@NonNull T data, @NonNull ResultObserver observer);
    void delete(@NonNull T data);
    void clear();
}
