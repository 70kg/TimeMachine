package me.drakeet.timemachine;

/**
 * @author drakeet
 */
public class MessageDispatcher implements Dispatcher, LifeCycle {

    private CoreContract.View view;
    private CoreContract.Service service;


    public MessageDispatcher(CoreContract.View view, CoreContract.Service service) {
        this.view = view;
        this.service = service;
    }


    @Override public void addNewIn(Message message) {
        this.view.onNewIn(message);
    }


    @Override public void addNewOut(Message message) {
        this.view.onNewOut(message);
        this.service.onNewOut(message);
    }


    @Override public void notifyDataSetChanged() {
        view.notifyDataSetChanged();
    }


    @Override public void start() {
        service.start();
    }


    @Override public void destroy() {
        service.destroy();
    }
}
