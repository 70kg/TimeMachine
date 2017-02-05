package me.drakeet.timemachine;

import android.support.annotation.NonNull;
import java.util.List;
import me.drakeet.multitype.FlatTypeClassAdapter;
import me.drakeet.multitype.MultiTypeAdapter;

/**
 * @author drakeet
 */
public class MessageAdapter extends MultiTypeAdapter {

    public MessageAdapter(@NonNull List<Message> messages) {
        super(messages);
        setFlatTypeAdapter(new MessageFlattenAdapter());
    }


    private static class MessageFlattenAdapter extends FlatTypeClassAdapter {

        @NonNull @Override public Class onFlattenClass(@NonNull Object message) {
            return ((Message) message).content.getClass();
        }
    }
}
