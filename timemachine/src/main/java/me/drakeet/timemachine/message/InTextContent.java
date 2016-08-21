package me.drakeet.timemachine.message;

import android.support.annotation.NonNull;

/**
 * @author drakeet
 */
public class InTextContent extends TextContent {

    public InTextContent(@NonNull byte[] data) {
        super(data);
    }


    public InTextContent(@NonNull String text) {
        super(text);
    }


    @NonNull @Override public String describe() {
        return "InText";
    }
}
