package me.drakeet.transformer;

import android.content.Context;
import android.support.annotation.NonNull;
import com.google.android.agera.Receiver;
import com.google.android.agera.Repository;
import com.google.android.agera.Reservoir;
import com.google.android.agera.Result;
import com.google.android.agera.Updatable;
import me.drakeet.agera.eventbus.AgeraBus;
import me.drakeet.timemachine.BaseService;
import me.drakeet.timemachine.CoreContract;
import me.drakeet.timemachine.Message;
import me.drakeet.timemachine.SimpleMessage;
import me.drakeet.timemachine.TimeKey;
import me.drakeet.transformer.request.YinRequests;
import me.drakeet.transformer.entity.Translation;
import me.drakeet.transformer.entity.Step;
import me.drakeet.transformer.request.TranslateRequests;

import static com.google.android.agera.Repositories.repositoryWithInitialValue;
import static com.google.android.agera.RepositoryConfig.SEND_INTERRUPT;
import static me.drakeet.transformer.Objects.requireNonNull;
import static me.drakeet.transformer.SimpleMessagesStore.messagesStore;
import static me.drakeet.transformer.Strings.empty;
import static me.drakeet.transformer.request.TranslateRequests.LIGHT_AND_DARK_GATE_CLOSE;

/**
 * @author drakeet
 */
public class MessageService extends BaseService {

    public static final String YIN = "YIN";
    public static final String TRANSFORMER = "transformer";
    public static final String DEFAULT = "default";
    public static final String EMPTY = "";

    private Updatable newInEvent;
    private final SimpleMessagesStore store;
    private ObservableHelper helper;

    private CoreContract.Presenter presenter;
    private boolean translateMode;
    // TODO: 16/7/10 to improve
    private boolean isConfirmMessage;
    private Reservoir<String> echoReaction;
    private Reservoir<String> yinReaction;
    private Reservoir<Translation> translateReaction;


    public MessageService(Context context) {
        super(context);
        store = messagesStore(getContext().getApplicationContext());
        helper = new ObservableHelper();
    }


    @Override public void setPresenter(@NonNull final CoreContract.Presenter presenter) {
        this.presenter = requireNonNull(presenter);
    }


    @Override public void start() {
        newInEvent = () -> {
            if (AgeraBus.repository().get() instanceof NewInEvent) {
                NewInEvent event = (NewInEvent) AgeraBus.repository().get();
                presenter.addNewIn(event.get());
            }
        };
        AgeraBus.repository().addUpdatable(newInEvent);

        echoReaction = Reservoirs.<String>reactionReservoir();
        Repository<String> echoRepo = repositoryWithInitialValue(EMPTY)
            .observe(echoReaction)
            .onUpdatesPerLoop()
            .thenAttemptGetFrom(echoReaction).orSkip()
            .notifyIf((last, cur) -> !cur.isEmpty())
            .onDeactivation(SEND_INTERRUPT)
            .compile();
        // TODO: 16/7/17 add
        helper.addToObservable(echoRepo, () -> stringReceiver().accept(echoRepo.get()));

        yinReaction = Reservoirs.<String>reactionReservoir();
        Repository<Result<String>> yinRepo = YinRequests.async(yinReaction);
        helper.addToObservable(yinRepo,
            () -> yinRepo.get()
                .ifSucceededSendTo(stringReceiver())
                .ifFailedSendTo(value -> {
                    stringReceiver().accept(
                        (value.getMessage() != null) ? value.getMessage() : "网络异常, 请重试");
                })
        );

        translateReaction = Reservoirs.<Translation>reactionReservoir();
    }


    @Override public void stop() {
        AgeraBus.repository().removeUpdatable(newInEvent);
        helper.removeObservables();
    }


    @Override public boolean onInterceptNewOut(@NonNull final Message message) {
        requireNonNull(message);
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
        requireNonNull(_message);
        if (!(_message instanceof SimpleMessage)) {
            throw new IllegalArgumentException("Only supports SimpleMessage currently.");
        }
        final SimpleMessage message = (SimpleMessage) _message;
        final String content = message.getContent();
        if (translateMode && !content.equals("关闭混沌世界")) {
            translateReaction.accept(Translation.working(content));
        } else {
            handleContent(content);
        }

        store.insert(message);
    }


    private void handleContent(@NonNull String content) {
        switch (requireNonNull(content)) {
            case "滚":
                stringReceiver().accept("但是...但是...");
                break;
            case "求王垠的最新文章":
                yinReaction.accept(content);
                break;
            case "发动魔法卡——混沌仪式!":
            case "混沌仪式":
                translateReaction.accept(Translation.create());
                Repository<Result<Translation>> transientRepo = TranslateRequests.translation(
                    translateReaction);
                transientRepo.addUpdatable(() -> transientRepo.get()
                    .ifSucceededSendTo(value -> handleTranslation(value))
                    .ifFailedSendTo(failure -> stringReceiver().accept(failure.getMessage()))
                );
                this.translateMode = true;
                break;
            case "关闭混沌仪式":
            case "关闭混沌世界":
                TranslateRequests.lightAndDarkGateTerminal(getContext(), false);
                stringReceiver().accept(LIGHT_AND_DARK_GATE_CLOSE);
                this.translateMode = false;
                break;
            default:
                echoReaction.accept(content);
                break;
        }
    }


    private void insertNewIn(@NonNull final SimpleMessage simpleMessage) {
        requireNonNull(simpleMessage);
        presenter.addNewIn(simpleMessage);
        store.insert(simpleMessage);
    }


    @NonNull private Receiver<String> stringReceiver() {
        return value -> insertNewIn(new SimpleMessage.Builder()
            .setContent(value)
            .setFromUserId(DEFAULT)
            .setToUserId(TimeKey.userId)
            .thenCreateAtNow());
    }


    private void handleTranslation(@NonNull Translation value) {
        requireNonNull(value);
        final String result = requireNonNull(value.text);
        if (value.step == Step.OnCreate) {
            stringReceiver().accept(result);
        } else {
            presenter.setInputText(result);
            isConfirmMessage = true;
        }
    }


    @Override public void onClear() {
        store.clear();
    }
}
