package me.drakeet.transformer;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.view.Menu;
import me.drakeet.timemachine.CoreFragment;
import me.drakeet.timemachine.widget.DrawerActivity;

/**
 * @author drakeet
 */
public class MainActivity extends DrawerActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    @VisibleForTesting
    CountingIdlingResource idlingResource = new CountingIdlingResource(TAG, BuildConfig.DEBUG);


    @Override public String provideServiceId() {
        return TranslationService.TRANSFORMER;
    }


    @Override protected void onCoreFragmentCreated(@NonNull final CoreFragment fragment) {
        fragment.setMessageObserver(new TransformObserver(this));
        fragment.setService(Services.messageService(this));
    }


    @Override public boolean onCreateDrawerOptionsMenu(Menu menu) {
        // TODO: 16/9/6  
        return false;
    }
}
