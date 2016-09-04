package me.drakeet.transformer;

import android.support.annotation.NonNull;
import me.drakeet.transformer.entity.Translation;

import static me.drakeet.transformer.entity.Step.OnCreate;
import static me.drakeet.transformer.entity.Translation.LIGHT_AND_DARK_GATE_OPEN;

/**
 * @author drakeet
 */
public class Translations {

    @NonNull public static Translation create() {
        return new Translation(OnCreate, LIGHT_AND_DARK_GATE_OPEN);
    }
}
