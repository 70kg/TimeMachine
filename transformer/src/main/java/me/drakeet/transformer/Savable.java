package me.drakeet.transformer;

import android.support.annotation.NonNull;

/**
 * @author drakeet
 */

public interface Savable {

    void init(@NonNull byte[] bytes);
    @NonNull byte[] toBytes();
}
