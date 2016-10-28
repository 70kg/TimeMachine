package me.drakeet.transformer;

import android.app.Application;
import com.squareup.leakcanary.LeakCanary;
import me.drakeet.library.CrashWoodpecker;
import me.drakeet.timemachine.TimeKey;

/**
 * @author drakeet
 */
public class App extends Application {

    @Override public void onCreate() {
        super.onCreate();
        CrashWoodpecker.flyTo(this).withKeys("me.drakeet");
        LeakCanary.install(this);
        TimeKey.install(getString(R.string.app_name), "drakeet");
    }
}
