package me.drakeet.timemachine;

import java.util.Date;

/**
 * @author drakeet
 */
public interface Message {

    String getContent();
    String getFromUserId();
    String getToUserId();
    Date getCreatedAt();
}
