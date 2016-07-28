package me.drakeet.transformer;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import static me.drakeet.transformer.Objects.requireNonNull;

/**
 * Non self UI Fragment, to be a drawer delegate of its parent.
 *
 * @author drakeet
 */
public class DrawerDelegate extends Fragment {

    private static final String FRAG_TAG = DrawerDelegate.class.getCanonicalName();

    private ActionBarDrawerToggle toggle;
    private SyncDrawerLayout drawer;


    /**
     * Attach the fragment to hold the drawer and toolbar
     *
     * @param parent a Activity that extends AppCompatActivity & OnNavigationItemSelectedListener
     * @return {@link DrawerDelegate}
     */
    public static <ParentActivity extends AppCompatActivity & OnNavigationItemSelectedListener> DrawerDelegate attach(
        @Nullable ParentActivity parent) {
        return attach(parent.getSupportFragmentManager());
    }


    private static DrawerDelegate attach(@Nullable FragmentManager fragmentManager) {
        DrawerDelegate delegate = (DrawerDelegate) fragmentManager.findFragmentByTag(FRAG_TAG);
        if (delegate == null) {
            delegate = new DrawerDelegate();
            fragmentManager.beginTransaction().add(delegate, FRAG_TAG).commit();
        }
        return delegate;
    }


    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        setupDrawerLayout(toolbar);
    }


    private void setupDrawerLayout(@NonNull Toolbar toolbar) {
        // TODO: 16/7/28 Maybe we should not handle the toolbar?
        requireNonNull(toolbar, "DrawerDelegate need a toolbar");
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        drawer = (SyncDrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(getActivity(), drawer, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
        drawer.setNavigationView(navigationView, getParent());
        navigationView.setItemIconTintList(null);
    }


    @Nullable private OnNavigationItemSelectedListener getParent() {
        Activity activity = getActivity();
        if (activity instanceof OnNavigationItemSelectedListener) {
            return (OnNavigationItemSelectedListener) activity;
        }
        return null;
    }


    /**
     * On back key pressed, should be call in
     * its parent Activity's onBackPressed
     *
     * @return true means should not call parent's
     * super onBackPressed, otherwise false
     */
    public boolean onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return true;
        } else {
            drawer.removeDrawerListener(toggle);
            return false;
        }
    }
}