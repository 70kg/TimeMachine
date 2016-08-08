package me.drakeet.timemachine.message;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import me.drakeet.timemachine.Message;
import me.drakeet.timemachine.MessageViewProvider;
import me.drakeet.timemachine.R;
import me.drakeet.timemachine.TimeKey;

/**
 * @author drakeet
 */
public class TextMessageViewProvider
    extends MessageViewProvider<TextContent, TextMessageViewProvider.ViewHolder> {

    @NonNull @Override
    protected ViewHolder onCreateViewHolder(
        @NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        View root = inflater.inflate(R.layout.item_message_out, parent, false);
        return new ViewHolder(root);
    }


    @Override
    protected void onBindViewHolder(
        @NonNull ViewHolder holder, @NonNull TextContent content, @NonNull Message message) {
        holder.text.setText(content.text);
        if (TimeKey.isCurrentUser(message.fromUserId)) {
            // TODO: 16/8/8  
        } else {
            // TODO: 16/8/8  
        }
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        @NonNull final TextView text;


        ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.text = (TextView) itemView.findViewById(R.id.content);
        }
    }
}
