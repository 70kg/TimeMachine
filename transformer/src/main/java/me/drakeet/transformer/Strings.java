package me.drakeet.transformer;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author drakeet
 */
public class Strings {

    @Nullable public static String toUtf8URLEncode(@NonNull final String origin) {
        try {
            return URLEncoder.encode(origin, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }


    @NonNull public static CharSequence empty() {
        return "";
    }
}
