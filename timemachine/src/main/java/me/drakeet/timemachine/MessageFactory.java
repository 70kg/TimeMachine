package me.drakeet.timemachine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.UUID;
import me.drakeet.multitype.ItemContent;

import static java.util.Objects.requireNonNull;

/**
 * @author drakeet
 */
public class MessageFactory {

    @NonNull private String id;
    @NonNull private final String fromUserId;
    @NonNull private final String toUserId;
    @Nullable private final String extra;


    private MessageFactory(@NonNull String fromUserId,
                           @NonNull String toUserId,
                           @Nullable String extra) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.extra = extra;
    }


    public Message newMessage(@NonNull ItemContent content) {
        id = UUID.randomUUID().toString();
        return newMessage(content, id);
    }


    public Message newMessage(@NonNull ItemContent content, @NonNull String id) {
        long createdTime = System.currentTimeMillis();
        return new Message(id, fromUserId, toUserId, createdTime, -1, extra, content);
    }

    // TODO: 16/8/2 Maybe we need a newTextMessage method


    public static class Builder {

        private String fromUserId;
        private String toUserId;
        private String extra;


        public Builder setFromUserId(@NonNull String fromUserId) {
            this.fromUserId = requireNonNull(fromUserId);
            return this;
        }


        public Builder setToUserId(@NonNull String toUserId) {
            this.toUserId = requireNonNull(toUserId);
            return this;
        }


        public Builder setExtra(@Nullable String extra) {
            this.extra = requireNonNull(extra);
            return this;
        }


        public MessageFactory build() {
            return new MessageFactory(fromUserId, toUserId, extra);
        }
    }
}
