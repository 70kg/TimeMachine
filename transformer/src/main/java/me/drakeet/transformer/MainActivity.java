package me.drakeet.transformer;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.agera.Repository;
import com.google.android.agera.Updatable;
import java.util.ArrayList;
import java.util.List;
import me.drakeet.timemachine.CoreContract;
import me.drakeet.timemachine.CoreFragment;
import me.drakeet.timemachine.Message;
import me.drakeet.timemachine.SimpleMessage;
import me.drakeet.timemachine.TimeKey;

import static me.drakeet.transformer.MessageService.TRANSFORMER;
import static me.drakeet.transformer.MessageService.YIN;
import static me.drakeet.transformer.Objects.requireNonNull;
import static me.drakeet.transformer.Services.messageService;
import static me.drakeet.transformer.SimpleMessagesStore.messagesStore;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener,
    CoreContract.Delegate {

    private static final String TAG = MainActivity.class.getSimpleName();

    private List<Message> messages = new ArrayList<Message>(100) {
        {
            add(new SimpleMessage.Builder()
                .setContent("Can I help you?")
                .setFromUserId("transformer")
                .setToUserId(TimeKey.userId)
                .thenCreateAtNow());
        }
    };
    private CoreContract.Presenter presenter;
    private Repository<List<SimpleMessage>> storeMessages;
    private DrawerDelegate drawerDelegate;


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawerDelegate = DrawerDelegate.attach(this);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        CoreFragment fragment = CoreFragment.newInstance();
        fragment.setDelegate(this);
        fragment.setService(messageService(this));
        transaction.add(R.id.core_container, fragment).commitAllowingStateLoss();
        final SimpleMessagesStore store = messagesStore(getApplicationContext());
        storeMessages = store.getSimpleMessagesRepository();
        storeMessages.addUpdatable(new Updatable() {
            @Override public void update() {
                messages.addAll(storeMessages.get());
                presenter.notifyDataSetChanged();
                storeMessages.removeUpdatable(this);
            }
        });
    }


    @Override public void setPresenter(@NonNull final CoreContract.Presenter presenter) {
        this.presenter = requireNonNull(presenter);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_yin) {
            SimpleMessage message = new SimpleMessage.Builder()
                .setContent("求王垠的最新文章")
                .setFromUserId(TimeKey.userId)
                .setToUserId(YIN)
                .thenCreateAtNow();
            presenter.addNewOut(message);
        } else if (id == R.id.nav_translate_open) {
            SimpleMessage message = new SimpleMessage.Builder()
                .setContent("发动魔法卡——混沌仪式!")
                .setFromUserId(TimeKey.userId)
                .setToUserId(TRANSFORMER)
                .thenCreateAtNow();
            presenter.addNewOut(message);
        } else if (id == R.id.nav_translate_close) {
            SimpleMessage message = new SimpleMessage.Builder()
                .setContent("关闭混沌世界")
                .setFromUserId(TimeKey.userId)
                .setToUserId(TRANSFORMER)
                .thenCreateAtNow();
            presenter.addNewOut(message);
        }
        return true;
    }


    @NonNull @Override public List<Message> provideInitialMessages() {
        return messages;
    }


    @Override public void onNewOut(@NonNull final Message message) {
        Log.v(TAG, "onNewOut: " + message.toString());
    }


    @Override public void onNewIn(@NonNull final Message message) {
        Log.v(TAG, "onNewIn: " + message.toString());
    }


    @Override public void onMessageClick(@NonNull final Message message) {
        Log.v(TAG, "onMessageClicked: " + message.toString());
    }


    @Override public void onMessageLongClick(@NonNull final Message message) {
        Log.v(TAG, "onMessageLongClicked: " + message.toString());
    }


    @Override public boolean onLeftActionClick() {
        return false;
    }


    @Override public boolean onRightActionClick() {
        return false;
    }


    @Override public void onBackPressed() {
        if (!drawerDelegate.onBackPressed()) {
            super.onBackPressed();
        }
    }


    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_clear) {
            presenter.clear();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override protected void onResume() {
        super.onResume();
        presenter.start();
    }


    @Override protected void onPause() {
        super.onPause();
        presenter.stop();
    }
}
