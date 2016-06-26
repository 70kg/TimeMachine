package me.drakeet.timemachine;

/**
 * @author drakeet
 */
public class MessagePresenter implements CoreContract.Presenter {

    private CoreContract.View view;
    private CoreContract.Service service;


    public MessagePresenter(CoreContract.View view, CoreContract.Service service) {
        this.view = view;
        this.service = service;
        service.setPresenter(this);
    }


    @Override public void addNewIn(Message message) {
        this.view.onNewIn(message);
    }


    @Override public void addNewOut(Message message) {
        this.view.onNewOut(message);
        this.service.onNewOut(message);
    }


    @Override public void clean() {
        view.onClean();
        service.onClean();
    }


    @Override public void notifyDataSetChanged() {
        view.onDataSetChanged();
    }


    @Override public void start() {
        service.start();
    }


    @Override public void destroy() {
        service.destroy();
    }

}
