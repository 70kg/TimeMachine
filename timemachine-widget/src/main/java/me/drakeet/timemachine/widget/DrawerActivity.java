package me.drakeet.timemachine.widget;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
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
import me.drakeet.timemachine.message.TextContent;
import me.drakeet.timemachine.store.MessageStore;

import static me.drakeet.timemachine.Objects.requireNonNull;

public abstract class DrawerActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener,
    CoreContract.Delegate {

    private static final String TAG = DrawerActivity.class.getSimpleName();
    protected MessageFactory messageFactory4User;

    private List<Message> messages = new ArrayList<>(100);
    protected CoreContract.Presenter presenter;
    private Repository<List<Message>> messagesRepository;
    private Updatable dataUpdatable;
    private DrawerDelegate drawerDelegate;


    public abstract String provideServiceId();

    protected abstract void onCoreFragmentCreated(@NonNull CoreFragment fragment);
    /**
     * TODO
     *
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.(Not used currently)
     **/
    public abstract boolean onCreateDrawerOptionsMenu(@NonNull DrawerDelegate drawer);
    /**
     * Called when an item in the drawer navigation menu is selected.
     *
     * @param item The selected item
     * @return true to display the item as the selected item
     */
    public abstract boolean onDrawerItemSelected(@NonNull final MenuItem item);


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawerDelegate = DrawerDelegate.attach(this);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        CoreFragment fragment = CoreFragment.newInstance();
        fragment.setDelegate(this);
        onCoreFragmentCreated(fragment);
        transaction.add(R.id.core_container, fragment).commitNow();

        onLoadData();
    }


    protected void onLoadData() {
        final MessageStore store = MessageStore.messagesStore(getApplicationContext());
        messagesRepository = store.getSimpleMessagesRepository();
        // TODO: 16/8/9 double notify!
        dataUpdatable = new Updatable() {
            @Override public void update() {
                messages.addAll(messagesRepository.get());
                presenter.notifyDataSetChanged();
                // Maybe we have another graceful way
                messagesRepository.removeUpdatable(dataUpdatable);
                dataUpdatable = null;
            }
        };
        messagesRepository.addUpdatable(dataUpdatable);
        messageFactory4User = new MessageFactory.Builder()
            .setFromUserId(TimeKey.userId)
            .setToUserId(provideServiceId())
            .build();
        MessageFactory messageFactory4Service = new MessageFactory.Builder()
            .setFromUserId(provideServiceId())
            .setToUserId(TimeKey.userId)
            .build();
        TextContent welcome = new TextContent("Can I help you?");
        messages.add(messageFactory4Service.newMessage(welcome));
    }


    @Override public void setPresenter(@NonNull final CoreContract.Presenter presenter) {
        this.presenter = requireNonNull(presenter);
    }


    @Override public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        return onDrawerItemSelected(item);
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
        onCreateDrawerOptionsMenu(drawerDelegate);
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
