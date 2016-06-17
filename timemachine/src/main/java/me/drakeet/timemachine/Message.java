package me.drakeet.timemachine;

import java.util.Date;

/**
 * @author drakeet
 */
public interface Message<T> {

    T getContent();
    String getFromUserId();
    String getToUserId();
    Date getCreatedAt();
}
