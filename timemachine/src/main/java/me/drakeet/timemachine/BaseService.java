package me.drakeet.timemachine;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

/**
 * @author drakeet
 */
public abstract class BaseService implements CoreContract.Service {

    private final Context context;


    protected BaseService(Context context) {this.context = context;}


    @NonNull public Context getContext() {
        return context;
    }


    @NonNull protected String getString(@StringRes int resId) {
        return getContext().getString(resId);
    }
}
