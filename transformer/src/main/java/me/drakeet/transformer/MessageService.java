package me.drakeet.transformer;

import com.google.android.agera.Repository;
import com.google.android.agera.Result;
import com.google.android.agera.Updatable;
import me.drakeet.agera.eventbus.AgeraBus;
import me.drakeet.timemachine.CoreContract;
import me.drakeet.timemachine.Message;
import me.drakeet.timemachine.SimpleMessage;
import me.drakeet.timemachine.TimeKey;

import static me.drakeet.transformer.SimpleMessagesStore.messagesStore;

/**
 * @author drakeet
 */
public class MessageService implements CoreContract.Service, Updatable {

    public static final String YIN = "YIN";
    public static final String DEFAULT = "default";

    private Repository<Result<String>> repository;
    private Updatable newInEvent;
    private final SimpleMessagesStore store;

    private CoreContract.Presenter presenter;


    public MessageService() {
        store = messagesStore(App.getContext());
    }


    @Override public void setPresenter(CoreContract.Presenter presenter) {
        this.presenter = presenter;
    }


    @Override public void start() {
        newInEvent = () -> {
            if (AgeraBus.repository().get() instanceof NewInEvent) {
                NewInEvent event = (NewInEvent) AgeraBus.repository().get();
                presenter.addNewIn(event.get());
            }
        };
        AgeraBus.repository().addUpdatable(newInEvent);
    }


    @Override public void stop() {
        AgeraBus.repository().removeUpdatable(newInEvent);
    }


    @Override public void onNewOut(final Message _message) {
        if (!(_message instanceof SimpleMessage)) {
            throw new IllegalArgumentException("Only supports SimpleMessage currently.");
        }
        final SimpleMessage message = (SimpleMessage) _message;
        switch (message.getContent()) {
            case "滚":
                insertNewIn(new SimpleMessage.Builder()
                        .setContent("但是...但是...")
                        .setFromUserId(DEFAULT)
                        .setToUserId(TimeKey.userId)
                        .thenCreateAtNow());
                break;
            case "求王垠的最新文章":
                repository = Requests.requestYinAsync();
                repository.addUpdatable(this);
                break;
            case "发动魔法卡——神圣的召唤!":
            case "神圣的召唤":

            default:
                // echo
                insertNewIn(new SimpleMessage.Builder()
                        .setContent(message.getContent())
                        .setFromUserId(DEFAULT)
                        .setToUserId(TimeKey.userId)
                        .thenCreateAtNow());
                break;
        }
        store.insert(message);
    }


    private void insertNewIn(SimpleMessage simpleMessage) {
        presenter.addNewIn(simpleMessage);
        store.insert(simpleMessage);
    }


    @Override public void update() {
        repository.get().ifSucceededSendTo(value -> {
            insertNewIn(new SimpleMessage.Builder()
                    .setContent(value)
                    .setFromUserId(YIN)
                    .setToUserId(TimeKey.userId)
                    .thenCreateAtNow());
        });
        repository.removeUpdatable(this);
    }


    @Override public void onClean() {
        store.clean();
    }
}
