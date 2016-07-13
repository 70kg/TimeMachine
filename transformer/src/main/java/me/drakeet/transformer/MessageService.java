package me.drakeet.transformer;

import android.support.annotation.NonNull;
import com.google.android.agera.Receiver;
import com.google.android.agera.Repository;
import com.google.android.agera.Result;
import com.google.android.agera.Updatable;
import me.drakeet.agera.eventbus.AgeraBus;
import me.drakeet.timemachine.CoreContract;
import me.drakeet.timemachine.Message;
import me.drakeet.timemachine.SimpleMessage;
import me.drakeet.timemachine.TimeKey;

import static me.drakeet.transformer.Requests.LIGHT_AND_DARK_GATE_CLOSE;
import static me.drakeet.transformer.Requests.LIGHT_AND_DARK_GATE_OPEN;
import static me.drakeet.transformer.SimpleMessagesStore.messagesStore;
import static me.drakeet.transformer.Strings.empty;

/**
 * @author drakeet
 */
public class MessageService implements CoreContract.Service, Updatable {

    public static final String YIN = "YIN";
    public static final String TRANSFORMER = "transformer";
    public static final String DEFAULT = "default";

    private Repository<Result<String>> transientRepo;
    private Updatable newInEvent;
    private final SimpleMessagesStore store;

    private CoreContract.Presenter presenter;
    private boolean translateMode;
    // TODO: 16/7/10 to improve
    private boolean isConfirmMessage;


    public MessageService() {
        store = messagesStore(App.getContext());
    }


    @Override public void setPresenter(@NonNull final CoreContract.Presenter presenter) {
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


    @Override public boolean onInterceptNewOut(@NonNull final Message message) {
        if (isConfirmMessage) {
            insertNewIn(new SimpleMessage.Builder()
                .setContent((String) message.getContent())
                .setFromUserId(TRANSFORMER)
                .setToUserId(TimeKey.userId)
                .setExtra("Confirmed")
                .thenCreateAtNow()
            );
            presenter.setInputText(empty());
            isConfirmMessage = false;
            return true;
        }
        return false;
    }


    @Override public void onNewOut(@NonNull final Message _message) {
        if (!(_message instanceof SimpleMessage)) {
            throw new IllegalArgumentException("Only supports SimpleMessage currently.");
        }
        final SimpleMessage message = (SimpleMessage) _message;
        final String content = message.getContent();
        if (translateMode && !content.equals("关闭混沌世界")) {
            transientRepo = Requests.requestTranslate(content);
            transientRepo.addUpdatable(
                () -> transientRepo.get()
                    .ifSucceededSendTo(value -> confirmTranslation(value)));
        } else {
            handleContent(content);
        }

        store.insert(message);
    }


    private void handleContent(String content) {
        switch (content) {
            case "滚":
                insertNewIn(new SimpleMessage.Builder()
                    .setContent("但是...但是...")
                    .setFromUserId(DEFAULT)
                    .setToUserId(TimeKey.userId)
                    .thenCreateAtNow());
                break;
            case "求王垠的最新文章":
                transientRepo = Requests.requestYinAsync();
                transientRepo.addUpdatable(this);
                break;
            case "发动魔法卡——混沌仪式!":
            case "混沌仪式":
                Requests.lightAndDarkGateTerminal(true);
                stringReceiver().accept(LIGHT_AND_DARK_GATE_OPEN);
                this.translateMode = true;
                break;
            case "关闭混沌仪式":
            case "关闭混沌世界":
                Requests.lightAndDarkGateTerminal(false);
                stringReceiver().accept(LIGHT_AND_DARK_GATE_CLOSE);
                this.translateMode = false;
                break;
            default:
                // echo
                insertNewIn(new SimpleMessage.Builder()
                    .setContent(content)
                    .setFromUserId(DEFAULT)
                    .setToUserId(TimeKey.userId)
                    .thenCreateAtNow());
                break;
        }
    }


    private void insertNewIn(@NonNull final SimpleMessage simpleMessage) {
        presenter.addNewIn(simpleMessage);
        store.insert(simpleMessage);
    }


    private void confirmTranslation(@NonNull String value) {
        presenter.setInputText(value);
        isConfirmMessage = true;
        // TODO: 16/7/10 save to file
    }


    @Override public void update() {
        transientRepo.get()
            .ifSucceededSendTo(stringReceiver())
            .ifFailedSendTo(value ->
                stringReceiver().accept(
                    (value.getMessage() != null) ? value.getMessage() : "网络异常, 请重试"));
        transientRepo.removeUpdatable(this);
    }


    @NonNull private Receiver<String> stringReceiver() {
        return value -> insertNewIn(new SimpleMessage.Builder()
            .setContent(value)
            .setFromUserId(YIN)
            .setToUserId(TimeKey.userId)
            .thenCreateAtNow());
    }


    @Override public void onClear() {
        store.clear();
    }
}
