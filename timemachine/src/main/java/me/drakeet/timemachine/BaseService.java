package me.drakeet.timemachine;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * @author drakeet
 */
public abstract class BaseService implements CoreContract.Service {

    private final Context context;


    protected BaseService(Context context) {this.context = context;}


    @NonNull protected Context getContext() {
        return context;
    }
}
