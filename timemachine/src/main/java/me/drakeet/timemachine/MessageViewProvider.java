package me.drakeet.timemachine;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import me.drakeet.multitype.ItemContent;
import me.drakeet.multitype.ItemViewProvider;
import me.drakeet.multitype.TypeItem;

/**
 * @author drakeet
 */
public abstract class MessageViewProvider<C extends ItemContent, V extends RecyclerView.ViewHolder>
    extends ItemViewProvider<C, V> {

    @Override
    protected void onBindViewHolder(@NonNull V holder, @NonNull C c, @NonNull TypeItem typeItem) {
        onBindViewHolder(holder, c, (Message) typeItem);
    }


    protected abstract void onBindViewHolder(
        @NonNull V holder, @NonNull C c, @NonNull Message message);
}
