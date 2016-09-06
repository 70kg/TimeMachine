package me.drakeet.timemachine.store;

/**
 * @author drakeet
 */
public interface ResultObserver {

    void onReturn(boolean succeeded);
}
