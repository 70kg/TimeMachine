package me.drakeet.timemachine;

import android.support.annotation.NonNull;
import java.util.List;

/**
 * @author drakeet
 */
public interface CoreContract {

    interface View {
        @NonNull Presenter initPresenter(@NonNull Service service);
        void changePresenter(@NonNull Presenter presenter);
        void setDelegate(@NonNull Delegate delegate);
        void setMessageObserver(@NonNull MessageObserver observer);
        void setService(@NonNull Service service);
        void onNewIn(@NonNull Message message);
        void onNewOut(@NonNull Message message);
        void setInputText(@NonNull CharSequence text);
        void onDataSetChanged();
        void onClear();
    }


    interface Delegate {
        @NonNull List<Message> provideInitialMessages();
        void setPresenter(@NonNull Presenter presenter);
        boolean onLeftActionClick();
        boolean onRightActionClick();
    }


    interface Service extends LifeCycle {
        void setPresenter(@NonNull Presenter presenter);
        void onNewOut(@NonNull Message message);
        void onNewIn(@NonNull Message message);
        void onClear();
        /**
         * Implement this method to intercept all new message events. This allows you to
         * watch events as they are dispatched to your Service, and judges whether go on or not.
         *
         * @param message a new message
         * @return true to intercept and stop dispatching, otherwise false
         */
        boolean onInterceptNewOut(@NonNull Message message);
    }


    interface Presenter extends LifeCycle {
        void addNewIn(@NonNull Message message);
        void addNewOut(@NonNull Message message);
        void setInputText(@NonNull CharSequence text);
        void notifyDataSetChanged();
        void clear();
        /**
         * Implement this method to intercept all new message events. This allows you to
         * watch events as they are dispatched to your Presenter then to your Service,
         * and judges whether go on or not.
         *
         * @param message a new message
         * @return true to intercept and stop dispatching, otherwise false
         */
        boolean onInterceptNewOut(@NonNull Message message);
    }
}
