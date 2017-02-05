package me.drakeet.timemachine.message;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import me.drakeet.timemachine.ContentViewHolder;
import me.drakeet.timemachine.Message;
import me.drakeet.timemachine.MessageViewProvider;
import me.drakeet.timemachine.R;
import me.drakeet.timemachine.TimeKey;

/**
 * @author drakeet
 */
public class TextMessageViewProvider
    extends MessageViewProvider<TextContent, TextMessageViewProvider.ViewHolder> {

    @Override
    protected ContentViewHolder onCreateContentViewHolder(
        @NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return new ViewHolder(inflater.inflate(R.layout.item_message_text, parent, false));
    }


    @Override
    protected void onBindContentViewHolder(
        @NonNull ViewHolder holder, @NonNull TextContent textContent, @NonNull Message message) {
        holder.text.setText(textContent.text);
        final Context context = holder.text.getContext();
        if (TimeKey.isCurrentUser(message.fromUserId)) {
            holder.text.setTextColor(ContextCompat.getColor(context, R.color.light));
        } else {
            holder.text.setTextColor(ContextCompat.getColor(context, R.color.textColorPrimary));
        }
    }


    static class ViewHolder extends ContentViewHolder {

        @NonNull final TextView text;


        ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.text = (TextView) itemView.findViewById(R.id.content);
        }
    }
}
