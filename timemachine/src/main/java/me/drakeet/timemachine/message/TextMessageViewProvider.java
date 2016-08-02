package me.drakeet.timemachine.message;

import android.support.annotation.NonNull;
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
public class TextMessageViewProvider extends MessageViewProvider<TextContent> {

    private static class ViewHolder extends MessageViewProvider.ViewHolder {
        @NonNull final TextView text;


        ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.text = (TextView) itemView.findViewById(R.id.content);
        }
    }


    @NonNull @Override protected View onCreateView(@NonNull ViewGroup parent) {
        View root = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_message_out, parent, false);
        ViewHolder holder = new ViewHolder(root);
        root.setTag(holder);
        return root;
    }


    @Override
    protected void onBindView(
        @NonNull View view, @NonNull TextContent content, @NonNull Message message) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.text.setText(content.text);
        if (TimeKey.isCurrentUser(message.fromUserId)) {
        } else {
        }
    }

}
