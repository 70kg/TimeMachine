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

/**
 * @author drakeet
 */
public class InTextMessageViewProvider
    extends MessageViewProvider<TextContent, InTextMessageViewProvider.ViewHolder> {

    @NonNull @Override
    protected ViewHolder onCreateViewHolder(
        @NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        View root = inflater.inflate(R.layout.item_message_in, parent, false);
        return new ViewHolder(root);
    }


    @Override
    protected void onBindViewHolder(
        @NonNull ViewHolder holder, @NonNull TextContent content, @NonNull Message message) {
        holder.text.setText(content.text);
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        @NonNull final TextView text;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.text = (TextView) itemView.findViewById(R.id.content);
        }
    }
}
