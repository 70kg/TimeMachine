package me.drakeet.timemachine;

import java.util.List;

/**
 * @author drakeet
 */
public interface CoreContract {

    interface View {
        Presenter initPresenter(Service service);
        void changePresenter(Presenter presenter);
        void setDelegate(Delegate delegate);
        void setService(Service service);
        void onNewIn(Message message);
        void onNewOut(Message message);
        void onDataSetChanged();
        void onClean();
    }


    interface Delegate {
        List<Message> provideInitialMessages();
        void setPresenter(Presenter presenter);
        void onNewOut(Message message);
        void onNewIn(Message message);
        void onMessageClick(Message message);
        void onMessageLongClick(Message message);
        boolean onLeftActionClick();
        boolean onRightActionClick();
    }


    interface Service extends LifeCycle {
        void setPresenter(Presenter presenter);
        void onNewOut(Message message);
        void onClean();
    }


    interface Presenter extends LifeCycle {
        void addNewIn(Message message);
        void addNewOut(Message message);
        void notifyDataSetChanged();
        void clean();
    }
}
