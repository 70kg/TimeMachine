package me.drakeet.transformer;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.NavigationView;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.agera.Repository;
import com.google.android.agera.Updatable;
import java.util.ArrayList;
import java.util.List;
import me.drakeet.timemachine.CoreContract;
import me.drakeet.timemachine.CoreFragment;
import me.drakeet.timemachine.Message;
import me.drakeet.timemachine.MessageFactory;
import me.drakeet.timemachine.TimeKey;
import me.drakeet.timemachine.message.InTextContent;
import me.drakeet.timemachine.message.OutTextContent;
import me.drakeet.timemachine.message.TextContent;
import me.drakeet.transformer.store.MessageStore;

import static me.drakeet.timemachine.Objects.requireNonNull;
import static me.drakeet.transformer.BuildConfig.DEBUG;
import static me.drakeet.transformer.MessageService.TRANSFORMER;
import static me.drakeet.transformer.Services.messageService;
import static me.drakeet.transformer.store.MessageStore.messagesStore;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener,
    CoreContract.Delegate {

    private static final String TAG = MainActivity.class.getSimpleName();
    private MessageFactory messageFactory;

    private List<Message> messages = new ArrayList<>(100);
    private CoreContract.Presenter presenter;
    private Repository<List<Message>> messagesRepository;
    private Updatable dataUpdatable;
    private DrawerDelegate drawerDelegate;

    @VisibleForTesting
    CountingIdlingResource idlingResource = new CountingIdlingResource(TAG, DEBUG);


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawerDelegate = DrawerDelegate.attach(this);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        CoreFragment fragment = CoreFragment.newInstance();
        fragment.setDelegate(this);
        fragment.setMessageObserver(new TransformObserver(this));
        fragment.setService(messageService(this));
        transaction.add(R.id.core_container, fragment).commitNow();

        onLoadData();
    }


    protected void onLoadData() {
        final MessageStore store = messagesStore(getApplicationContext());
        messagesRepository = store.getSimpleMessagesRepository();
        // TODO: 16/8/9 double notify!
        dataUpdatable = () -> {
            messages.addAll(messagesRepository.get());
            presenter.notifyDataSetChanged();
            // Maybe we have another graceful way
            messagesRepository.removeUpdatable(dataUpdatable);
            dataUpdatable = null;
        };
        messagesRepository.addUpdatable(dataUpdatable);
        messageFactory = new MessageFactory.Builder()
            .setFromUserId(TimeKey.userId)
            .setToUserId(TRANSFORMER)
            .build();
        TextContent welcome = new InTextContent("Can I help you?");
        messages.add(messageFactory.newMessage(welcome));
    }


    @Override public void setPresenter(@NonNull final CoreContract.Presenter presenter) {
        this.presenter = requireNonNull(presenter);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override public boolean onNavigationItemSelected(MenuItem item) {
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
        }
        return true;
    }


    @NonNull @Override public List<Message> provideInitialMessages() {
        return messages;
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


    @Override protected void onDestroy() {
        super.onDestroy();
        // TODO: 16/8/20  Maybe we have another graceful way
        if (dataUpdatable != null) {
            messagesRepository.removeUpdatable(dataUpdatable);
        }
    }
}
