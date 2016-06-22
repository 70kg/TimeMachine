package me.drakeet.timemachine;

/**
 * @author drakeet
 */
public interface Dispatcher {

    void addNewIn(Message message);
    void addNewOut(Message message);
    void notifyDataSetChanged();
}
