package me.drakeet.transformer.entity;

import android.support.annotation.NonNull;
import java.io.File;

import static me.drakeet.transformer.Objects.requireNonNull;
import static me.drakeet.transformer.entity.Step.OnCreate;
import static me.drakeet.transformer.entity.Step.OnStop;

/**
 * @author drakeet
 */
public class Translation implements Cloneable {

    public static final String LIGHT_AND_DARK_GATE_OPEN = "混沌世界: 开启!\n请发送一篇你需要翻译的内容";
    public static final String LIGHT_AND_DARK_GATE_CLOSE = "混沌世界: 关闭!";

    @NonNull public Step step;
    @NonNull public String current;
    public int currentIndex;
    public Translation last;
    public String[] sources;
    public String[] results;

    @NonNull public File from;
    // TODO: 16/7/24
    @NonNull public File to;


    private Translation(@NonNull Step step, @NonNull String current) {
        this.step = requireNonNull(step);
        this.current = requireNonNull(current);
    }


    @NonNull public static Translation create() {
        return new Translation(OnCreate, LIGHT_AND_DARK_GATE_OPEN);
    }


    @NonNull public static Translation confirm(@NonNull final String text) {
        return new Translation(Step.OnConfirm, text);
    }


    @NonNull public static Translation stop() {
        return new Translation(OnStop, LIGHT_AND_DARK_GATE_CLOSE);
    }


    @Override public String toString() {
        return "Translation {" +
            "step=" + step +
            ", text='" + current + '\'' +
            '}';
    }


    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Translation that = (Translation) o;
        return step == that.step && current.equals(that.current);
    }


    @Override public int hashCode() {
        int result = step.hashCode();
        result = 31 * result + current.hashCode();
        return result;
    }


    @NonNull @Override public Translation clone() {
        try {
            return (Translation) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return this;
    }
}
