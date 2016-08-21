package me.drakeet.timemachine.message;

import android.support.annotation.NonNull;

/**
 * @author drakeet
 */
public class OutTextContent extends TextContent {

    public OutTextContent(@NonNull byte[] data) {
        super(data);
    }


    public OutTextContent(@NonNull String text) {
        super(text);
    }


    @NonNull @Override public String describe() {
        return "OutText";
    }
}
