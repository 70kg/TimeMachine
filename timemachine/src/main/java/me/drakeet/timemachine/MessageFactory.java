package me.drakeet.timemachine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

/**
 * @author drakeet
 */
public class MessageFactory {

    @NonNull private final String id;
    @NonNull private final String fromUserId;
    @NonNull private final String toUserId;
    @Nullable private final String extra;


    private MessageFactory(@NonNull String id,
                           @NonNull String fromUserId,
                           @NonNull String toUserId,
                           @Nullable String extra) {
        this.id = id;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.extra = extra;
    }


    public Message newMessage(@NonNull Content content) {
        long createdTime = System.currentTimeMillis();
        return new Message(id, fromUserId, toUserId, createdTime, -1, extra, content);
    }

    // TODO: 16/8/2 Maybe we need a newTextMessage method


    public static class Builder {

        private String id;
        private String fromUserId;
        private String toUserId;
        private String extra;


        /**
         * Set the id of Message, it's optional,
         * will be set with {@code UUID.randomUUID()} when null at build
         *
         * @param id uuid
         * @return Builder self
         */
        public Builder setId(@NonNull String id) {
            this.id = requireNonNull(id);
            return this;
        }


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
            if (this.id == null) {
                this.id = UUID.randomUUID().toString();
            }
            return new MessageFactory(id, fromUserId, toUserId, extra);
        }
    }
}
