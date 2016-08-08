package me.drakeet.timemachine.message;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.gson.Gson;
import me.drakeet.multitype.ItemContent;
import me.drakeet.multitype.Savable;

/**
 * @author drakeet
 */
public class TextContent implements ItemContent, Savable {

    @NonNull public String text;


    public TextContent(@NonNull String text) {
        this.text = text;
    }


    public TextContent(@NonNull byte[] data) {
        init(data);
    }


    @Override public void init(@NonNull byte[] data) {
        String json = new String(data);
        this.text = new Gson().fromJson(json, TextContent.class).text;
    }


    @Nullable @Override public byte[] toBytes() {
        return new Gson().toJson(this).getBytes();
    }
}
