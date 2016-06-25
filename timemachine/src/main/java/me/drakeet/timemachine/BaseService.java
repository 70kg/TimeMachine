package me.drakeet.timemachine;

/**
 * @author drakeet
 */
public abstract class BaseService implements CoreContract.Service, CoreContract.Presenter {

    private CoreContract.View view;


    public BaseService(CoreContract.View view) {
        view.setService(this);
        this.view = view;
    }


    @Override
    public void addNewIn(Message message) {
        view.onNewIn(message);
    }


    @Override public void addNewOut(Message message) {
        view.onNewOut(message);
        this.onNewOut(message);
    }


    @Override public void notifyDataSetChanged() {
        view.notifyDataSetChanged();
    }


    @Override public void clear() {
        view.clear();
    }
}
