package me.drakeet.timemachine;

import android.support.annotation.NonNull;
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


    private SimpleMessage(@NonNull String id, @NonNull String content,
                          @NonNull String fromUserId, @NonNull String toUserId,
                          @NonNull Date createdAt) {
        this.id = id;
        this.content = content;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.createdAt = createdAt;
    }


    private SimpleMessage(@NonNull String id, @NonNull SimpleMessage message) {
        this.id = id;
        this.content = message.content;
        this.fromUserId = message.fromUserId;
        this.toUserId = message.toUserId;
        this.createdAt = message.createdAt;
    }


    @NonNull
    public static SimpleMessage simpleMessage(@NonNull String id, @NonNull String content,
                                              @NonNull String fromUserId, @NonNull String toUserId,
                                              @NonNull long createdAtTime) {
        return new SimpleMessage(id, content, fromUserId, toUserId, new Date(createdAtTime));
    }


    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleMessage that = (SimpleMessage) o;
        if (!id.equals(that.id)) return false;
        if (!content.equals(that.content)) return false;
        if (!fromUserId.equals(that.fromUserId)) return false;
        if (!toUserId.equals(that.toUserId)) return false;
        return createdAt.equals(that.createdAt);

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
        return "SimpleMessage{" +
            "id=" + id +
            ", content='" + content + '\'' +
            ", fromUserId='" + fromUserId + '\'' +
            ", toUserId='" + toUserId + '\'' +
            ", createdAt=" + createdAt +
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
        @NonNull private String content;
        @NonNull private String fromUserId;
        @NonNull private String toUserId;
        @NonNull private Date createdAt;


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


        public SimpleMessage thenCreateAtNow() {
            this.createdAt = new Now();
            return build();
        }


        @Override public SimpleMessage build() {
            return new SimpleMessage(UUID.randomUUID().toString(), content, fromUserId, toUserId,
                createdAt);
        }
    }
}
