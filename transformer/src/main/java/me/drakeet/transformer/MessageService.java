package me.drakeet.transformer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.agera.Receiver;
import com.google.android.agera.Repository;
import com.google.android.agera.Reservoir;
import com.google.android.agera.Result;
import com.google.android.agera.Updatable;
import me.drakeet.agera.eventbus.AgeraBus;
import me.drakeet.timemachine.BaseService;
import me.drakeet.timemachine.CoreContract;
import me.drakeet.timemachine.Message;
import me.drakeet.timemachine.MessageFactory;
import me.drakeet.timemachine.TimeKey;
import me.drakeet.timemachine.message.TextContent;
import me.drakeet.transformer.entity.Step;
import me.drakeet.transformer.entity.Translation;
import me.drakeet.transformer.request.TranslateRequests;
import me.drakeet.transformer.request.YinRequests;

import static com.google.android.agera.Repositories.repositoryWithInitialValue;
import static com.google.android.agera.RepositoryConfig.SEND_INTERRUPT;
import static me.drakeet.timemachine.Objects.requireNonNull;
import static me.drakeet.transformer.MessagesStore.messagesStore;
import static me.drakeet.transformer.Strings.empty;

/**
 * @author drakeet
 */
public class MessageService extends BaseService {

    public static final String YIN = "YIN";
    public static final String TRANSFORMER = "transformer";
    public static final String DEFAULT = "default";

    private final MessagesStore store;
    private MessageFactory inMessageFactory;
    private CoreContract.Presenter presenter;
    private boolean translateMode;
    private boolean isConfirmMessage;
    /* the translation snowball */
    private Translation translationToken;

    private ObservableHelper helper;
    private Updatable newInEvent;
    private Reservoir<String> echoReaction;
    private Reservoir<String> yinReaction;
    private Reservoir<Translation> translateReaction;


    public MessageService(Context context) {
        super(context);
        this.store = messagesStore(getContext().getApplicationContext());
        this.helper = new ObservableHelper();
        this.translationToken = Translation.create();
        inMessageFactory = new MessageFactory.Builder()
            .setFromUserId(TRANSFORMER)
            .setToUserId(TimeKey.userId)
            .build();
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
        Repository<String> echoRepo = repositoryWithInitialValue(empty())
            .observe(echoReaction)
            .onUpdatesPerLoop()
            .thenAttemptGetFrom(echoReaction).orSkip()
            .notifyIf((last, cur) -> !cur.isEmpty())
            .onDeactivation(SEND_INTERRUPT)
            .compile();
        helper.addToObservable(echoRepo, () -> newInReceiver().accept(echoRepo.get()));

        yinReaction = Reservoirs.<String>reactionReservoir();
        Repository<Result<String>> yinRepo = YinRequests.async(yinReaction);
        helper.addToObservable(yinRepo, () -> yinRepo.get()
            .ifSucceededSendTo(newInReceiver())
            .ifFailedSendTo(errorHandler()));

        translateReaction = Reservoirs.<Translation>reactionReservoir();
        Repository<Result<Translation>> transientRepo =
            TranslateRequests.translation(translateReaction);
        transientRepo.addUpdatable(() -> transientRepo.get()
            .ifSucceededSendTo(this::handleTranslation)
            .ifFailedSendTo(errorHandler()));
    }


    @Override public boolean onInterceptNewOut(@NonNull final Message message) {
        requireNonNull(message);
        if (isConfirmMessage) {
            isConfirmMessage = false;
            TranslateRequests.loop(translationToken);
            return false;
        }
        return false;
    }


    @Override public void onNewOut(@NonNull final Message message) {
        requireNonNull(message);
        if (!(message.content instanceof TextContent)) {
            throw new IllegalArgumentException("Only supports TextContent currently.");
        }
        final String content = ((TextContent) message.content).text;
        if (translateMode && !content.equals("关闭混沌世界")) {
            translationToken.current = content;
            translateReaction.accept(translationToken);
        } else {
            handleContent(content);
        }
        store.insert(message);
    }


    private void handleContent(@NonNull String content) {
        switch (requireNonNull(content)) {
            case "滚":
                newInReceiver().accept("但是...但是...");
                break;
            case "求王垠的最新文章":
                yinReaction.accept(content);
                break;
            case "发动魔法卡——混沌仪式!":
            case "混沌仪式":
                translationToken.step = Step.OnCreate;
                translateReaction.accept(translationToken);
                this.translateMode = true;
                break;
            case "关闭混沌仪式":
            case "关闭混沌世界":
                translationToken.step = Step.OnStop;
                translateReaction.accept(translationToken);
                this.translateMode = false;
                break;
            default:
                echoReaction.accept(content);
                break;
        }
    }


    private void insertNewIn(@NonNull final Message message) {
        requireNonNull(message);
        presenter.addNewIn(message);
        store.insert(message);
    }


    @NonNull private Receiver<String> newInReceiver() {
        return value -> insertNewIn(inMessageFactory.newMessage(new TextContent(value)));
    }


    private Receiver<Throwable> errorHandler() {
        return value -> {
            String error = (value.getMessage() != null) ?
                           value.getMessage() :
                           "网络异常, 请重试";
            Log.e("errorHandler", error);
            newInReceiver().accept(error);
        };
    }


    private void handleTranslation(@NonNull Translation result) {
        final String text = requireNonNull(result.current);
        Log.d("handleTranslation", result.toString());
        switch (result.step) {
            case OnCreate:
            case OnStop:
                newInReceiver().accept(text);
                TranslateRequests.loop(translationToken);
                break;
            case OnDone:
                newInReceiver().accept(text);
                this.translateMode = false;
                break;
            case OnStart:
                newInReceiver().accept(text);
                translationToken = result;
                TranslateRequests.loop(translationToken);
                /* goto OnWorking */
                translateReaction.accept(translationToken);
                break;
            case OnWorking:
                newInReceiver().accept(text);
                translationToken = result;
                TranslateRequests.loop(translationToken);
                /* goto OnConfirm */
                translateReaction.accept(translationToken);
                break;
            case OnConfirm:
                presenter.setInputText(text);
                isConfirmMessage = true;
                break;
        }
    }


    @Override public void onClear() {
        store.clear();
    }


    @Override public void stop() {
        AgeraBus.repository().removeUpdatable(newInEvent);
        helper.removeObservables();
    }
}
