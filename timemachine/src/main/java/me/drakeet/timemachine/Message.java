package me.drakeet.timemachine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.gson.Gson;

/**
 * @author drakeet
 */
public class Message {

    @NonNull public String id;
    @NonNull public String fromUserId;
    @NonNull public String toUserId;
    @NonNull public long createdTime;
    @Nullable public long receivedTime;
    @Nullable public String extra;

    @NonNull public Content content;


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
        this.extra = extra;
        this.content = content;
    }


    @Override public String toString() {
        return new Gson().toJson(this);
    }
}
