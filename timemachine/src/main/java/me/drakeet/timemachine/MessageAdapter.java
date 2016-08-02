package me.drakeet.timemachine;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;

/**
 * @author drakeet
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<Message> messages;


    public MessageAdapter(List<Message> list) {
        this.messages = list;
    }


    @Override public int getItemViewType(int position) {
        Content content = messages.get(position).content;
        int index = MessageTypePool.getContents().indexOf(content.getClass());
        Log.d("TM-index", String.valueOf(index));
        return index;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int indexViewType) {
        View root = MessageTypePool.getProviderByIndex(indexViewType).onCreateView(parent);
        ViewHolder holder = new ViewHolder(root);
        return holder;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int type = getItemViewType(position);
        Message message = messages.get(position);
        MessageTypePool.getProviderByIndex(type).onBindView(holder.itemView, message);
    }


    @Override public int getItemCount() {
        return messages.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}

