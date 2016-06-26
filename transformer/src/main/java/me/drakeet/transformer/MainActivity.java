package me.drakeet.transformer;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.agera.Repository;
import com.google.android.agera.Updatable;
import me.drakeet.timemachine.*;

import java.util.ArrayList;
import java.util.List;

import static me.drakeet.transformer.Services.messageService;
import static me.drakeet.transformer.SimpleMessagesStore.messagesStore;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener,
    CoreContract.Delegate,
    Updatable {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawer;
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


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupDrawerLayout(toolbar);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        CoreFragment fragment = CoreFragment.newInstance();
        fragment.setDelegate(this);
        fragment.setService(messageService());
        transaction.add(R.id.core_container, fragment).commitAllowingStateLoss();
        final SimpleMessagesStore store = messagesStore(getApplicationContext());
        storeMessages = store.getSimpleMessagesRepository();
        storeMessages.addUpdatable(this);
    }


    @Override public void setPresenter(CoreContract.Presenter presenter) {
        this.presenter = presenter;
        this.presenter.start();
    }


    @Override public void update() {
        messages.addAll(storeMessages.get());
        presenter.notifyDataSetChanged();
        storeMessages.removeUpdatable(this);
    }


    private void setupDrawerLayout(Toolbar toolbar) {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open,
            R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_yin) {
            SimpleMessage message = new SimpleMessage.Builder()
                .setContent("求王垠的最新文章")
                .setFromUserId(TimeKey.userId)
                .setToUserId(MessageService.SELF)
                .thenCreateAtNow();
            presenter.addNewOut(message);
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override public List<Message> provideInitialMessages() {
        return messages;
    }


    @Override public void onNewOut(Message message) {
        Log.v(TAG, "onNewOut: " + message.toString());
    }


    @Override public void onNewIn(Message message) {
        Notifications.simple(this, "New message",
            message.getContent().toString(),
            R.drawable.ic_notification, this.getClass());
    }


    @Override public void onMessageClick(Message message) {
        Log.v(TAG, "onMessageClicked: " + message.toString());
    }


    @Override public void onMessageLongClick(Message message) {
        Log.v(TAG, "onMessageLongClicked: " + message.toString());
    }


    @Override public boolean onLeftActionClick() {
        return false;
    }


    @Override public boolean onRightActionClick() {
        return false;
    }


    @Override public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            drawer.removeDrawerListener(toggle);
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
        }
        return super.onOptionsItemSelected(item);
    }


    @Override protected void onDestroy() {
        super.onDestroy();
        presenter.destroy();
    }
}
