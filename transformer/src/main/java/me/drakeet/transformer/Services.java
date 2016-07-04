package me.drakeet.transformer;

import android.support.annotation.NonNull;

/**
 * @author drakeet
 */
public class Services {

    private Services() {
    }


    @NonNull public static MessageService messageService() {
        return new MessageService();
    }
}
