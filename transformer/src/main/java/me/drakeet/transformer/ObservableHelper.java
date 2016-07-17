package me.drakeet.transformer;

import android.support.annotation.NonNull;
import com.google.android.agera.Observable;
import com.google.android.agera.Updatable;
import java.util.HashMap;

/**
 * @author drakeet
 */
public class ObservableHelper {

    private final HashMap<Observable, Updatable> map = new HashMap<>();


    public void addToObservable(
        @NonNull final Observable observable, @NonNull Updatable updatable) {
        map.put(observable, updatable);
        observable.addUpdatable(updatable);
    }


    public void removeObservables() {
        for (Observable observable : map.keySet()) {
            observable.removeUpdatable(map.get(observable));
        }
        map.clear();
    }
}
