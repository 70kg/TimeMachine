package me.drakeet.transformer.entity;

import android.support.annotation.NonNull;
import java.io.File;

import static me.drakeet.timemachine.Objects.requireNonNull;
import static me.drakeet.transformer.entity.Step.OnConfirm;
import static me.drakeet.transformer.entity.Step.OnDone;
import static me.drakeet.transformer.entity.Step.OnStop;

/**
 * @author drakeet
 */
public class Translation implements Cloneable {

    private Step step;
    public String current;
    public int currentIndex;
    public String[] sources;
    // TODO: 16/7/31
    public String[] results;

    public File from;
    public File to;


    public Translation(@NonNull Step step, @NonNull String current) {
        setup(step, current);
    }


    private void setup(@NonNull Step step, @NonNull String current) {
        this.step = requireNonNull(step);
        this.current = requireNonNull(current);
    }


    public void confirm(@NonNull final String text) {
        this.setup(OnConfirm, text);
    }


    public void done(@NonNull final String current) {
        this.setup(OnDone, current);
    }


    public void stop(@NonNull final String current) {
        this.setup(OnStop, current);
    }


    public void next() {
        this.step = this.step.next();
    }


    @NonNull public Step getStep() {
        return step;
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
