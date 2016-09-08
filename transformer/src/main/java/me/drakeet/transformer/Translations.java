package me.drakeet.transformer;

import android.support.annotation.NonNull;
import me.drakeet.transformer.entity.Translation;

import static me.drakeet.transformer.entity.Step.OnCreate;

/**
 * @author drakeet
 */
class Translations {

    @NonNull static Translation create(@NonNull final String initial) {
        return new Translation(OnCreate, initial);
    }
}
