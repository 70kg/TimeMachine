package me.drakeet.timemachine;

import java.util.Date;

/**
 * @author drakeet
 */
public class SimpleMessage implements Message<String> {

    private final String content;
    private final String fromUserId;
    private final String toUserId;
    private final Date createdAt;


    private SimpleMessage(String content, String fromUserId, String toUserId, Date createdAt) {
        this.content = content;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.createdAt = createdAt;
    }


    public SimpleMessage(SimpleMessage message) {
        this.content = message.content;
        this.fromUserId = message.fromUserId;
        this.toUserId = message.toUserId;
        this.createdAt = message.createdAt;
    }


    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleMessage message = (SimpleMessage) o;
        if (!content.equals(message.content)) return false;
        if (!fromUserId.equals(message.fromUserId)) return false;
        if (toUserId != null ? !toUserId.equals(message.toUserId) : message.toUserId != null) {
            return false;
        }
        return createdAt.equals(message.createdAt);
    }


    @Override public int hashCode() {
        int result = content.hashCode();
        result = 31 * result + fromUserId.hashCode();
        result = 31 * result + (toUserId != null ? toUserId.hashCode() : 0);
        result = 31 * result + createdAt.hashCode();
        return result;
    }


    @Override public String toString() {
        return "Message{" +
            "content='" + content + '\'' +
            ", fromUserId='" + fromUserId + '\'' +
            ", toUserId='" + toUserId + '\'' +
            ", createdAt=" + createdAt +
            '}';
    }


    @Override public String getContent() {
        return this.content;
    }


    @Override public String getFromUserId() {
        return this.fromUserId;
    }


    @Override public String getToUserId() {
        return this.toUserId;
    }


    @Override public Date getCreatedAt() {
        return this.createdAt;
    }


    public static class Builder implements me.drakeet.timemachine.Builder<SimpleMessage> {
        private String content;
        private String fromUserId;
        private String toUserId;
        private Date createdAt;


        public Builder setContent(String content) {
            this.content = content;
            return this;
        }


        public Builder setFromUserId(String fromUserId) {
            this.fromUserId = fromUserId;
            return this;
        }


        public Builder setToUserId(String toUserId) {
            this.toUserId = toUserId;
            return this;
        }


        public Builder setCreatedAt(Date createdAt) {
            this.createdAt = createdAt;
            return this;
        }


        public SimpleMessage thenCreateAtNow() {
            this.createdAt = new Now();
            return build();
        }


        @Override public SimpleMessage build() {
            return new SimpleMessage(content, fromUserId, toUserId, createdAt);
        }
    }
}
