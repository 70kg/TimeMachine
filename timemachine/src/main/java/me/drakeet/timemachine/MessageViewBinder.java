package me.drakeet.timemachine;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import me.drakeet.multitype.ItemViewBinder;

/**
 * @author drakeet
 */
public abstract class MessageViewBinder
    <C extends Content, SubViewHolder extends ContentViewHolder>
    extends ItemViewBinder<Message, MessageViewBinder.FrameHolder> {

    protected abstract ContentViewHolder onCreateContentViewHolder(
        @NonNull LayoutInflater inflater, @NonNull ViewGroup parent);

    protected abstract void onBindContentViewHolder(
        @NonNull SubViewHolder holder, @NonNull C messageContent, @NonNull Message message);


    @NonNull @Override
    protected FrameHolder onCreateViewHolder(
        @NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        View root = inflater.inflate(R.layout.item_frame_message, parent, false);
        ContentViewHolder subViewHolder = onCreateContentViewHolder(inflater, parent);
        return new FrameHolder(root, subViewHolder);
    }


    @SuppressWarnings("unchecked")
    @Override protected void onBindViewHolder(
        @NonNull FrameHolder holder, @NonNull Message message) {
        /* init the avatar or date time here */
        onBindContentViewHolder((SubViewHolder) holder.subViewHolder, (C) message.content, message);
        // TODO: 2017/2/5 We may have a more graceful implement.
        if (TimeKey.isCurrentUser(message.fromUserId)) {
            holder.startContainer.removeAllViews();
            holder.endContainer.removeAllViews();
            holder.endContainer.addView(holder.subViewHolder.itemView);
            holder.startContainer.setVisibility(View.GONE);
            holder.endContainer.setVisibility(View.VISIBLE);
        } else {
            holder.startContainer.removeAllViews();
            holder.endContainer.removeAllViews();
            holder.startContainer.addView(holder.subViewHolder.itemView);
            holder.startContainer.setVisibility(View.VISIBLE);
            holder.endContainer.setVisibility(View.GONE);
        }
    }


    static class FrameHolder extends RecyclerView.ViewHolder {

        private FrameLayout startContainer;
        private FrameLayout endContainer;
        private ContentViewHolder subViewHolder;


        FrameHolder(View itemView, final ContentViewHolder subViewHolder) {
            super(itemView);
            startContainer = (FrameLayout) findViewById(R.id.container_start);
            endContainer = (FrameLayout) findViewById(R.id.container_end);
            this.subViewHolder = subViewHolder;
        }


        private View findViewById(@IdRes int resId) {
            return itemView.findViewById(resId);
        }
    }
}