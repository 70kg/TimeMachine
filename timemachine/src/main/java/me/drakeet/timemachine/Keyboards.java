package me.drakeet.timemachine;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import static java.util.Objects.requireNonNull;

/**
 * @author drakeet
 */
public class Keyboards {

    public static void show(@NonNull final View view) {
        requireNonNull(view);
        InputMethodManager inputManager = (InputMethodManager) view.getContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }


    public static boolean isShown(@NonNull final View view) {
        requireNonNull(view);
        InputMethodManager inputManager = (InputMethodManager) view.getContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE);
        return inputManager.isActive(view);
    }


    public static void hide(@NonNull final View view) {
        requireNonNull(view);
        InputMethodManager imm = (InputMethodManager) view.getContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (!imm.isActive()) {
            return;
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
