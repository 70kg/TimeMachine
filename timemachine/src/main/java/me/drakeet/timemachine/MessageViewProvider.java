package me.drakeet.timemachine;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import me.drakeet.multitype.ItemViewProvider;

/**
 * @author drakeet
 */
public abstract class MessageViewProvider<C extends Content, V extends RecyclerView.ViewHolder>
    extends ItemViewProvider<Message, V> {

    @SuppressWarnings("unchecked") @Override
    protected void onBindViewHolder(@NonNull V holder, @NonNull Message message) {
        onBindViewHolder(holder, (C) message.content, (Message) message);
    }


    protected abstract void onBindViewHolder(
        @NonNull V holder, @NonNull C content, @NonNull Message message);
}
