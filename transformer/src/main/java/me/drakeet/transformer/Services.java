package me.drakeet.transformer;

import android.support.annotation.NonNull;

/**
 * @author drakeet
 */
public class Services {

    private Services() {
        throw new AssertionError();
    }


    @NonNull public static MessageService messageService() {
        return new MessageService();
    }
}
