package me.drakeet.transformer.entity;

import android.support.annotation.NonNull;

import static me.drakeet.transformer.Objects.requireNonNull;
import static me.drakeet.transformer.entity.Step.OnCreate;
import static me.drakeet.transformer.entity.Step.OnStop;

/**
 * @author drakeet
 */
public class Translation {

    public static final String LIGHT_AND_DARK_GATE_OPEN = "混沌世界: 开启!\n请发送一篇你需要翻译的内容";
    public static final String LIGHT_AND_DARK_GATE_CLOSE = "混沌世界: 关闭!";

    @NonNull public Step step;
    @NonNull public String text;


    private Translation(@NonNull Step step, @NonNull String text) {
        this.step = requireNonNull(step);
        this.text = requireNonNull(text);
    }


    @NonNull public static Translation create() {
        return new Translation(OnCreate, LIGHT_AND_DARK_GATE_OPEN);
    }


    @NonNull public static Translation working(@NonNull final String text) {
        return new Translation(Step.OnWorking, text);
    }


    @NonNull public static Translation stop() {
        return new Translation(OnStop, LIGHT_AND_DARK_GATE_CLOSE);
    }


    @Override public String toString() {
        return "Translation {" +
            "step=" + step +
            ", text='" + text + '\'' +
            '}';
    }


    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Translation that = (Translation) o;
        return step == that.step && text.equals(that.text);
    }


    @Override public int hashCode() {
        int result = step.hashCode();
        result = 31 * result + text.hashCode();
        return result;
    }
}
