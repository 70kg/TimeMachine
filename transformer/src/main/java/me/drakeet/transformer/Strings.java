package me.drakeet.transformer;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static me.drakeet.transformer.Objects.requireNonNull;

/**
 * @author drakeet
 */
public class Strings {

    @Nullable public static String toUtf8URLEncode(@NonNull final String origin) {
        String source = requireNonNull(origin);
        try {
            return URLEncoder.encode(source, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }


    @NonNull public static String empty() {
        return "";
    }
}
