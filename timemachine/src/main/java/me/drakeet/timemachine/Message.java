package me.drakeet.timemachine;

import android.support.annotation.NonNull;
import com.google.gson.Gson;
import me.drakeet.multitype.Item;

/**
 * @author drakeet
 */
public class Message implements Item {

    public String id;
    public String fromUserId;
    public String toUserId;
    public long createdTime;
    public long receivedTime;
    public Content content;
    public String extra;


    public Message() {
    }


    public Message(
        @NonNull String id,
        @NonNull String fromUserId,
        @NonNull String toUserId,
        @NonNull long createdTime, long receivedTime, String extra,
        @NonNull Content content) {
        this.id = id;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.createdTime = createdTime;
        this.receivedTime = receivedTime;
        this.content = content;
        this.extra = extra;
    }


    @Override public String toString() {
        return new Gson().toJson(this);
    }
}
