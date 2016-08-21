package me.drakeet.transformer;

import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.agera.BaseObservable;
import com.google.android.agera.Reservoir;
import com.google.android.agera.Result;
import java.util.ArrayDeque;
import java.util.Queue;

import static me.drakeet.timemachine.Objects.requireNonNull;

/**
 * @author drakeet
 */
public class Reservoirs {

    private static final String TAG = Reservoirs.class.getSimpleName();


    @NonNull public static <T> Reservoir reactionReservoir() {
        return new ReactionReservoir<>(new ArrayDeque<T>());
    }


    private static class ReactionReservoir<T> extends BaseObservable implements Reservoir<T> {

        @NonNull private final Queue<T> queue;


        private ReactionReservoir(@NonNull final Queue<T> queue) {
            this.queue = requireNonNull(queue);
        }


        @Override public void accept(@NonNull T value) {
            synchronized (queue) {
                boolean success = queue.offer(value);
                if (BuildConfig.DEBUG && !success) {
                    throw new IllegalStateException("queue offer failed");
                }
            }
            dispatchUpdate();
        }


        @NonNull @Override public Result<T> get() {
            T nullableValue;
            boolean shouldDispatchUpdate;
            synchronized (queue) {
                nullableValue = queue.poll();
                shouldDispatchUpdate = !queue.isEmpty();
            }
            if (shouldDispatchUpdate) {
                dispatchUpdate();
            }
            Log.d(TAG, "get: " + nullableValue);
            return Result.absentIfNull(nullableValue);
        }


        @Override protected void observableActivated() {
            synchronized (queue) {
                if (queue.isEmpty()) {
                    return;
                }
            }
            dispatchUpdate();
        }
    }


    private Reservoirs() {
        throw new AssertionError();
    }
}
