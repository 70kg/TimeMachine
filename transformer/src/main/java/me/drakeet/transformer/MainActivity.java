package me.drakeet.transformer;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.view.MenuItem;
import me.drakeet.timemachine.CoreFragment;
import me.drakeet.timemachine.Message;
import me.drakeet.timemachine.message.OutTextContent;
import me.drakeet.timemachine.message.TextContent;
import me.drakeet.timemachine.widget.DrawerActivity;
import me.drakeet.timemachine.widget.DrawerDelegate;

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


    @Override public boolean onCreateDrawerOptionsMenu(@NonNull DrawerDelegate drawer) {
        drawer.inflateMenu(R.menu.activity_main_drawer);
        return true;
    }


    @Override public boolean onDrawerItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_yin) {
            TextContent content = new OutTextContent("求王垠的最新文章");
            Message message = messageFactory.newMessage(content);
            presenter.addNewOut(message);
        } else if (id == R.id.nav_translate_open) {
            TextContent content = new OutTextContent("发动魔法卡——混沌仪式!");
            Message message = messageFactory.newMessage(content);
            presenter.addNewOut(message);
        } else if (id == R.id.nav_translate_close) {
            TextContent content = new OutTextContent("关闭混沌世界");
            Message message = messageFactory.newMessage(content);
            presenter.addNewOut(message);
        } else if (id == R.id.nav_android_sdk_source) {
            TextContent content = new OutTextContent("请告诉我最新的 Android SDK Source 版本");
            Message message = messageFactory.newMessage(content);
            presenter.addNewOut(message);
        }
        return true;
    }
}
