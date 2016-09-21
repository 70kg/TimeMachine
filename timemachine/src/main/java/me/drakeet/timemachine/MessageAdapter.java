package me.drakeet.timemachine;

import android.support.annotation.NonNull;
import java.util.List;
import me.drakeet.multitype.Item;
import me.drakeet.multitype.MultiTypeAdapter;

/**
 * @author drakeet
 */
public class MessageAdapter extends MultiTypeAdapter {

    public MessageAdapter(@NonNull List<Message> messages) {
        super(messages);
    }


    @NonNull @Override public Class onFlattenClass(@NonNull Item message) {
        return ((Message) message).content.getClass();
    }
}
