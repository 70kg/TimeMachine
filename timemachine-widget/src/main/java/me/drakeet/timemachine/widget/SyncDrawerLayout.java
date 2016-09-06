package me.drakeet.timemachine.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * It's a synchronized DrawerLayout, it will close drawers after onNavigationItemSelected and
 * listener.onNavigationItemSelected will be called after onDrawerClosed for perfect animations
 *
 * @author drakeet
 */
public class SyncDrawerLayout extends DrawerLayout {

    @Nullable private MenuItem currentItem;


    public SyncDrawerLayout(Context context) {
        super(context);
    }


    public SyncDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public SyncDrawerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    /**
     * Set the NavigationView and its OnNavigationItemSelectedListener, so that it will close
     * drawers after onNavigationItemSelected and listener.onNavigationItemSelected will be
     * called after onDrawerClosed
     *
     * @param view NavigationView
     * @param listener NavigationView.OnNavigationItemSelectedListener
     */
    public void setNavigationView(NavigationView view, final NavigationView.OnNavigationItemSelectedListener listener) {
        final NavigationView navigationView = view;
        navigationView.setNavigationItemSelectedListener(
            new NavigationView.OnNavigationItemSelectedListener() {
                @Override public boolean onNavigationItemSelected(MenuItem item) {
                    currentItem = item;
                    closeDrawers();
                    return true;
                }
            });
        this.addDrawerListener(new DrawerListenerAdapter() {
            @Override public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (currentItem != null) {
                    listener.onNavigationItemSelected(currentItem);
                    currentItem = null;
                }
            }
        });
    }


    private class DrawerListenerAdapter implements DrawerLayout.DrawerListener {

        @Override public void onDrawerSlide(View drawerView, float slideOffset) {

        }


        @Override public void onDrawerOpened(View view) {
            InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (!imm.isActive()) {
                return;
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }


        @Override public void onDrawerClosed(View drawerView) {
        }


        @Override public void onDrawerStateChanged(int newState) {

        }
    }
}
