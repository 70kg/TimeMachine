package me.drakeet.transformer;

/**
 * @author drakeet
 */

public interface Savable {

    void init(byte[] bytes);
    byte[] toBytes();
}
