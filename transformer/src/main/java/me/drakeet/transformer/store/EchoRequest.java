package me.drakeet.transformer.store;

/**
 * @author drakeet
 */
final class EchoRequest {

    Object request;
    ResultObserver observer;


    public EchoRequest(Object request, ResultObserver observer) {
        this.request = request;
        this.observer = observer;
    }
}
