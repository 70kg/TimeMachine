package me.drakeet.timemachine.store;

import android.support.annotation.NonNull;

/**
 * @author drakeet
 */
public interface Store<T> {

    void insert(@NonNull T data);
    void insert(@NonNull T data, @NonNull ResultObserver observer);
    void delete(@NonNull T data);
    void clear();
}
