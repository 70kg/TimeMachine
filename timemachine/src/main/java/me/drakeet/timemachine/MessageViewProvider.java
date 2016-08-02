package me.drakeet.timemachine;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author drakeet
 */
public abstract class MessageViewProvider<T extends Content> {

    @NonNull protected abstract View onCreateView(@NonNull ViewGroup parent);

    protected abstract void onBindView(@NonNull View view, @NonNull T t, @NonNull Message message);


    public final void onBindView(@NonNull View view, @NonNull Message data) {
        this.onBindView(view, (T) data.content, data);
    }


    public static class ViewHolder {
        @NonNull public final View itemView;


        public ViewHolder(@NonNull View itemView) {
            if (itemView == null) {
                throw new IllegalArgumentException("itemView may not be null");
            }
            this.itemView = itemView;
        }
    }
}
