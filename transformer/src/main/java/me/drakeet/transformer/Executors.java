package me.drakeet.transformer;

import java.util.concurrent.Executor;

import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * @author drakeet
 */
public class Executors {

    public static final Executor networkExecutor = newFixedThreadPool(5);
    public static final Executor calculationExecutor = newFixedThreadPool(5);
}
