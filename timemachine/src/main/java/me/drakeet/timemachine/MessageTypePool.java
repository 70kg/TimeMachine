package me.drakeet.timemachine;

import android.support.annotation.NonNull;
import java.util.ArrayList;

/**
 * @author drakeet
 */
public final class MessageTypePool {

    private static ArrayList<Class<? extends Content>> contents = new ArrayList<>();
    private static ArrayList<MessageViewProvider> viewProviders = new ArrayList<>();


    public static void register(
        @NonNull Class<? extends Content> messageContent, @NonNull MessageViewProvider provider) {
        contents.add(messageContent);
        viewProviders.add(provider);
    }


    @NonNull public static ArrayList<Class<? extends Content>> getContents() {
        return contents;
    }


    @NonNull public static ArrayList<MessageViewProvider> getProviders() {
        return viewProviders;
    }


    @NonNull public static MessageViewProvider getProviderByIndex(int index) {
        return viewProviders.get(index);
    }

}
