package me.drakeet.timemachine.message;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.gson.Gson;
import me.drakeet.timemachine.Content;
import me.drakeet.timemachine.Savable;

/**
 * @author drakeet
 */
public class TextContent implements Content, Savable {

    @NonNull public String text;


    public TextContent() {}


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


    // TODO: 16/8/21 shall be ab abstract?
    @NonNull @Override public String describe() {
        return "Text";
    }


    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TextContent content = (TextContent) o;
        return text.equals(content.text);
    }


    @Override public int hashCode() {
        return text.hashCode();
    }
}
