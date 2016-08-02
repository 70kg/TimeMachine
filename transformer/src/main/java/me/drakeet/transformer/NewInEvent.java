package me.drakeet.transformer;

import android.support.annotation.NonNull;
import com.google.android.agera.Supplier;
import me.drakeet.timemachine.Message;

import static me.drakeet.timemachine.Objects.requireNonNull;

/**
 * @author drakeet
 */
public class NewInEvent implements Supplier<Message> {

    @NonNull public final Message message;


    public NewInEvent(@NonNull final Message in) {
        this.message = requireNonNull(in);
    }


    @NonNull @Override public Message get() {
        return message;
    }
}
