package me.drakeet.timemachine;

import java.util.Date;

/**
 * @author drakeet
 */
@SuppressWarnings("ALL")
public final class Now extends Date {

    public Now() {
    }


    private Now(int year, int month, int day) {
        throwException();
    }


    private Now(int year, int month, int day, int hour, int minute) {
        throwException();
    }


    private Now(int year, int month, int day, int hour, int minute, int second) {
        throwException();
    }


    private Now(long milliseconds) {
        throwException();
    }


    private Now(String string) {
        throwException();
    }


    private void throwException() {
        throw new RuntimeException("Cannot set time for Now.");
    }


    @Override public void setTime(long milliseconds) {
        throwException();
    }


    @Override public void setDate(int day) {
        throwException();
    }


    @Override public void setHours(int hour) {
        throwException();
    }


    @Override public void setMinutes(int minute) {
        throwException();
    }


    @Override public void setMonth(int month) {
        throwException();
    }


    @Override public void setSeconds(int second) {
        throwException();
    }


    @Override public void setYear(int year) {
        throwException();
    }
}
