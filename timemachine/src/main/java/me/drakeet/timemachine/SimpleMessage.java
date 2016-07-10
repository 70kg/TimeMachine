package me.drakeet.timemachine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.Date;
import java.util.UUID;

/**
 * @author drakeet
 */
public class SimpleMessage implements Message<String> {

    @NonNull private final String id;
    @NonNull private final String content;
    @NonNull private final String fromUserId;
    @NonNull private final String toUserId;
    @NonNull private final Date createdAt;
    @Nullable private final String extra;


    private SimpleMessage(@NonNull String id, @NonNull String content,
                          @NonNull String fromUserId, @NonNull String toUserId,
                          @NonNull Date createdAt, @Nullable String extra) {
        this.id = id;
        this.content = content;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.createdAt = createdAt;
        this.extra = extra;
    }


    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SimpleMessage that = (SimpleMessage) o;
        return id.equals(that.id) && content.equals(that.content) &&
            fromUserId.equals(that.fromUserId) &&
            toUserId.equals(that.toUserId) &&
            createdAt.equals(that.createdAt);
    }


    @Override public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + content.hashCode();
        result = 31 * result + fromUserId.hashCode();
        result = 31 * result + toUserId.hashCode();
        result = 31 * result + createdAt.hashCode();
        return result;
    }


    @Override public String toString() {
        return "SimpleMessage {" +
            "id='" + id + '\'' +
            ", content='" + content + '\'' +
            ", fromUserId='" + fromUserId + '\'' +
            ", toUserId='" + toUserId + '\'' +
            ", createdAt=" + createdAt +
            ", extra='" + extra + '\'' +
            '}';
    }


    @NonNull public String getId() {
        return id;
    }


    @NonNull @Override public String getContent() {
        return this.content;
    }


    @NonNull @Override public String getFromUserId() {
        return this.fromUserId;
    }


    @NonNull @Override public String getToUserId() {
        return this.toUserId;
    }


    @NonNull @Override public Date getCreatedAt() {
        return this.createdAt;
    }


    public static class Builder implements me.drakeet.timemachine.Builder<SimpleMessage> {

        private String id;
        private String content;
        private String fromUserId;
        private String toUserId;
        private Date createdAt;
        private String extra;


        public Builder setId(@NonNull String id) {
            this.id = id;
            return this;
        }


        public Builder setContent(@NonNull String content) {
            this.content = content;
            return this;
        }


        public Builder setFromUserId(@NonNull String fromUserId) {
            this.fromUserId = fromUserId;
            return this;
        }


        public Builder setToUserId(@NonNull String toUserId) {
            this.toUserId = toUserId;
            return this;
        }


        public Builder setCreatedAt(@NonNull Date createdAt) {
            this.createdAt = createdAt;
            return this;
        }


        public Builder setCreatedAt(long milliseconds) {
            this.createdAt = new Date(milliseconds);
            return this;
        }


        public Builder setExtra(@Nullable String extra) {
            this.extra = extra;
            return this;
        }


        public SimpleMessage thenCreateAtNow() {
            this.createdAt = new Now();
            return build();
        }


        @Override public SimpleMessage build() {
            if (this.id == null) {
                this.id = UUID.randomUUID().toString();
            }
            return new SimpleMessage(id, content, fromUserId, toUserId,
                createdAt, extra);
        }
    }
}
