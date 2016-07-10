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
        void setService(@NonNull Service service);
        void onNewIn(@NonNull Message message);
        void onNewOut(@NonNull Message message);
        void setInputText(@NonNull CharSequence text);
        void onDataSetChanged();
        void onClean();
    }


    interface Delegate {
        @NonNull List<Message> provideInitialMessages();
        void setPresenter(@NonNull Presenter presenter);
        void onNewOut(@NonNull Message message);
        void onNewIn(@NonNull Message message);
        void onMessageClick(@NonNull Message message);
        void onMessageLongClick(@NonNull Message message);
        boolean onLeftActionClick();
        boolean onRightActionClick();
    }


    interface Service extends LifeCycle {
        void setPresenter(@NonNull Presenter presenter);
        void onNewOut(@NonNull Message message);
        void onClean();
        boolean onInterceptNewOut(@NonNull Message message);
    }


    interface Presenter extends LifeCycle {
        void addNewIn(@NonNull Message message);
        void addNewOut(@NonNull Message message);
        void setInputText(@NonNull CharSequence text);
        void notifyDataSetChanged();
        void clean();
        boolean onInterceptNewOut(Message message);
    }
}
