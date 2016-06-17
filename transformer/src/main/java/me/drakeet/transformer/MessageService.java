package me.drakeet.transformer;

import com.google.android.agera.Repository;
import com.google.android.agera.Result;
import com.google.android.agera.Updatable;
import me.drakeet.agera.eventbus.AgeraBus;
import me.drakeet.timemachine.BaseService;
import me.drakeet.timemachine.CoreContract;
import me.drakeet.timemachine.Message;
import me.drakeet.timemachine.SimpleMessage;
import me.drakeet.timemachine.TimeKey;

/**
 * @author drakeet
 */
public class MessageService extends BaseService implements Updatable {

    public static final String SELF = MessageService.class.getSimpleName();

    private Repository<Result<String>> repository;
    private Updatable newInEvent;


    public MessageService(CoreContract.View view) {
        super(view);
    }


    @Override public void start() {
        newInEvent = () -> {
            if (AgeraBus.repository().get() instanceof NewInEvent) {
                NewInEvent event = (NewInEvent) AgeraBus.repository().get();
                addNewIn(event.get());
            }
        };
        AgeraBus.repository().addUpdatable(newInEvent);
    }


    @Override public void destroy() {
        AgeraBus.repository().removeUpdatable(newInEvent);
    }


    @Override public void onNewOut(final Message _message) {
        // TODO: 16/6/17 instanceof
        SimpleMessage message = (SimpleMessage) _message;
        switch (message.getContent()) {
            case "滚":
                addNewIn(new SimpleMessage.Builder()
                    .setContent("但是...但是...")
                    .setFromUserId(SELF)
                    .setToUserId(TimeKey.userId)
                    .thenCreateAtNow());
                break;
            case "求王垠的最新文章":
                repository = Requests.requestYinAsync();
                repository.addUpdatable(this);
                break;
            default:
                // echo
                SimpleMessage simpleMessage = new SimpleMessage.Builder()
                    .setContent(message.getContent())
                    .setFromUserId(SELF)
                    .setToUserId(TimeKey.userId)
                    .thenCreateAtNow();
                addNewIn(simpleMessage);
                break;
        }
    }


    @Override public void update() {
        repository.get().ifSucceededSendTo(value -> {
            SimpleMessage message = new SimpleMessage.Builder()
                .setContent(value)
                .setFromUserId(SELF)
                .setToUserId(TimeKey.userId)
                .thenCreateAtNow();
            addNewIn(message);
        });
        repository.removeUpdatable(this);
    }
}
