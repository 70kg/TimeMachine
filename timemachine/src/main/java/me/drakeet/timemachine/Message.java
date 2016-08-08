package me.drakeet.timemachine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.gson.Gson;
import me.drakeet.multitype.ItemContent;
import me.drakeet.multitype.TypeItem;

/**
 * @author drakeet
 */
public class Message extends TypeItem {

    @NonNull public String id;
    @NonNull public String fromUserId;
    @NonNull public String toUserId;
    @NonNull public long createdTime;
    @Nullable public long receivedTime;

    public Message() {
    }


    public Message(
        @NonNull String id,
        @NonNull String fromUserId,
        @NonNull String toUserId,
        @NonNull long createdTime, long receivedTime, String extra,
        @NonNull ItemContent content) {
        super(content, extra);
        this.id = id;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.createdTime = createdTime;
        this.receivedTime = receivedTime;
    }


    @Override public String toString() {
        return new Gson().toJson(this);
    }
}
