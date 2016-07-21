package me.drakeet.transformer.entity;

import android.support.annotation.NonNull;

import static me.drakeet.transformer.Objects.requireNonNull;
import static me.drakeet.transformer.entity.Step.OnCreate;
import static me.drakeet.transformer.request.TranslateRequests.LIGHT_AND_DARK_GATE_OPEN;

/**
 * @author drakeet
 */
public class Translation {

    @NonNull public Step step;
    @NonNull public String text;


    private Translation(@NonNull Step step, @NonNull String text) {
        this.step = requireNonNull(step);
        this.text = requireNonNull(text);
    }


    @NonNull public static Translation create() {
        return new Translation(OnCreate, LIGHT_AND_DARK_GATE_OPEN);
    }


    @NonNull public static Translation working(@NonNull String text) {
        return new Translation(Step.OnWorking, text);
    }
}
