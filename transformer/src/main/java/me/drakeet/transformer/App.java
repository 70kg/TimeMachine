package me.drakeet.transformer;

import android.app.Application;
import android.content.Context;
import java.util.concurrent.Executor;
import me.drakeet.timemachine.TimeKey;

import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * @author drakeet
 */
public class App extends Application {

    public static final Executor networkExecutor = newFixedThreadPool(5);
    public static final Executor calculationExecutor = newFixedThreadPool(5);
    private static Context context;


    @Override public void onCreate() {
        super.onCreate();
        TimeKey.install(getString(R.string.app_name), "drakeet");
        if (context == null) {
            context = this.getApplicationContext();
        }
    }


    public static Context getContext() {
        return context;
    }
}
