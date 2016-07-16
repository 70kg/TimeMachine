package me.drakeet.transformer;

import android.app.Application;
import java.util.concurrent.Executor;
import me.drakeet.timemachine.TimeKey;

import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * @author drakeet
 */
public class App extends Application {

    public static final Executor networkExecutor = newFixedThreadPool(5);
    public static final Executor calculationExecutor = newFixedThreadPool(5);


    @Override public void onCreate() {
        super.onCreate();
        TimeKey.install(getString(R.string.app_name), "drakeet");
    }
}
