package me.drakeet.transformer;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import java.util.concurrent.Executor;
import me.drakeet.timemachine.*;

import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * @author drakeet
 */
public class App extends Application {

    static final Executor networkExecutor = newFixedThreadPool(5);
    static final Executor calculationExecutor = newFixedThreadPool(5);
    @SuppressLint("StaticFieldLeak")
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
