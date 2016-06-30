package me.drakeet.transformer;

/**
 * @author drakeet
 */
public class Log {

    public static boolean debug;

    public static void d(String tag, String msg) {
        if (debug) {
            android.util.Log.d(tag, msg);
        }
    }
}
