package me.drakeet.timemachine;

import android.support.annotation.NonNull;
import java.util.Date;

/**
 * @author drakeet
 */
public interface Message<T> {

    @NonNull T getContent();
    @NonNull String getFromUserId();
    @NonNull String getToUserId();
    @NonNull Date getCreatedAt();
}
