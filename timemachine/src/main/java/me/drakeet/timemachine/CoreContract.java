package me.drakeet.timemachine;

import java.util.List;

/**
 * @author drakeet
 */
public interface CoreContract {

    interface View {
        void setDelegate(Delegate delegate);
        void setService(Service service);
        void onNewIn(Message message);
        void onNewOut(Message message);
        void notifyDataSetChanged();
        // TODO: 16/6/24
        void clear();
    }


    interface Delegate {
        List<Message> provideInitialMessages();
        void onNewOut(Message message);
        void onNewIn(Message message);
        void onMessageClick(Message message);
        void onMessageLongClick(Message message);
        boolean onLeftActionClick();
        boolean onRightActionClick();
    }


    interface Service extends LifeCycle {
        void onNewOut(Message message);
        void clear();
    }


    interface Presenter {
        void addNewIn(Message message);
        void addNewOut(Message message);
        void notifyDataSetChanged();
    }
}
