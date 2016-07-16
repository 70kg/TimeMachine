package me.drakeet.transformer;

import android.support.annotation.NonNull;
import com.google.android.agera.Supplier;
import me.drakeet.timemachine.SimpleMessage;

import static me.drakeet.transformer.Objects.requireNonNull;

/**
 * @author drakeet
 */
public class NewInEvent implements Supplier<SimpleMessage> {

    @NonNull public final SimpleMessage message;


    public NewInEvent(@NonNull final SimpleMessage in) {
        this.message = requireNonNull(in);
    }


    @NonNull @Override public SimpleMessage get() {
        return message;
    }
}
