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
import me.drakeet.timemachine.MessageFactory;
import me.drakeet.timemachine.TimeKey;
import me.drakeet.timemachine.message.InTextContent;
import me.drakeet.timemachine.message.OutTextContent;
import me.drakeet.timemachine.message.TextContent;

import static me.drakeet.timemachine.Objects.requireNonNull;
import static me.drakeet.transformer.MessageService.TRANSFORMER;
import static me.drakeet.transformer.MessagesStore.messagesStore;
import static me.drakeet.transformer.Services.messageService;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener,
    CoreContract.Delegate {

    private static final String TAG = MainActivity.class.getSimpleName();
    private MessageFactory messageFactory;

    private List<Message> messages = new ArrayList<>(100);
    private CoreContract.Presenter presenter;
    private Repository<List<Message>> storeMessages;
    private Updatable initialUpdatable;
    private DrawerDelegate drawerDelegate;


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawerDelegate = DrawerDelegate.attach(this);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        CoreFragment fragment = CoreFragment.newInstance();
        fragment.setDelegate(this);
        fragment.setService(messageService(this));
        transaction.add(R.id.core_container, fragment).commitNow();

        final MessagesStore store = messagesStore(getApplicationContext());
        storeMessages = store.getSimpleMessagesRepository();
        initialUpdatable = () -> {
            messages.addAll(storeMessages.get());
            presenter.notifyDataSetChanged();
        };
        storeMessages.addUpdatable(initialUpdatable);

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


    @Override protected void onDestroy() {
        super.onDestroy();
        // Maybe we have another graceful way
        storeMessages.removeUpdatable(initialUpdatable);
    }
}
