package me.drakeet.transformer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.agera.Function;
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
import me.drakeet.timemachine.Savable;
import me.drakeet.timemachine.TimeKey;
import me.drakeet.timemachine.message.TextContent;
import me.drakeet.timemachine.store.MessageStore;
import me.drakeet.transformer.entity.Step;
import me.drakeet.transformer.entity.Translation;
import me.drakeet.transformer.module.AndroidSDKDelegate;
import me.drakeet.transformer.module.EchoDelegate;
import me.drakeet.transformer.module.TomatoDelegate;
import me.drakeet.transformer.module.YinDelegate;

import static com.google.android.agera.Repositories.repositoryWithInitialValue;
import static com.google.android.agera.RepositoryConfig.SEND_INTERRUPT;
import static me.drakeet.timemachine.Objects.requireNonNull;
import static me.drakeet.timemachine.store.MessageStore.messagesStore;
import static me.drakeet.transformer.Executors.calculationExecutor;
import static me.drakeet.transformer.Executors.networkExecutor;
import static me.drakeet.transformer.Requests.urlToResponse;
import static me.drakeet.transformer.entity.Step.OnConfirm;
import static me.drakeet.transformer.entity.Step.OnWorking;
import static me.drakeet.transformer.request.TranslateRequests.YOU_DAO;
import static me.drakeet.transformer.request.TranslateRequests.current2UrlMerger;
import static me.drakeet.transformer.request.TranslateRequests.youdaoResponseToResult;

/**
 * @author drakeet
 */
public class TransformService extends BaseService {

    static final String YIN = "YIN";
    static final String TRANSFORMER = "transformer";

    private final MessageStore store;
    private MessageFactory inMessageFactory;
    private CoreContract.Presenter presenter;
    private boolean translateMode;
    private boolean isConfirmMessage;
    /* the translation snowball */
    private Translation translationToken;

    ObservableHelper helper;
    private Updatable newInEvent;
    private Reservoir<Translation> translateReaction;

    // TODO: 2016/11/13 Delegate List
    private EchoDelegate echoDelegate;
    private YinDelegate yinDelegate;
    private AndroidSDKDelegate androidSDKDelegate;
    private TomatoDelegate tomatoDelegate;


    TransformService(Context context) {
        super(context);
        this.store = messagesStore(getContext().getApplicationContext());
        this.helper = new ObservableHelper();
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
        echoDelegate = new EchoDelegate(this);
        yinDelegate = new YinDelegate(this);
        androidSDKDelegate = new AndroidSDKDelegate(this);
        tomatoDelegate = new TomatoDelegate(this);
        echoDelegate.prepare();
        yinDelegate.prepare();
        androidSDKDelegate.prepare();
        tomatoDelegate.prepare();
        this.prepare();
    }


    @SuppressWarnings("unchecked") private void prepare() {
        translateReaction = Reservoirs.<Translation>reactionReservoir();
        Repository<Result<String>> translationRepo = repositoryWithInitialValue(
            Result.<String>absent())
            .observe(translateReaction)
            .onUpdatesPerLoop()
            .attemptGetFrom(translateReaction).orSkip()
            .goTo(networkExecutor)
            .check(input -> input.getStep() == OnConfirm)
            .orEnd((Function<Translation, Result<String>>) input -> {
                Log.e("check-confirm", input.toString());
                return Result.failure();
            })
            .mergeIn(YOU_DAO, current2UrlMerger())
            .attemptTransform(urlToResponse())
            .orEnd(Result::failure)
            .goTo(calculationExecutor)
            .transform(youdaoResponseToResult())
            .goLazy()
            .thenTransform(input -> input)
            .onDeactivation(SEND_INTERRUPT)
            .compile();
        translationRepo.addUpdatable(() -> translationRepo.get()
            .ifSucceededSendTo(this::handleConfirm)
            .ifFailedSendTo(errorHandler()));
    }


    @Override public void onNewIn(@NonNull final Message message) {
        store.insert(message);
    }


    /**
     * Transform the message's content to next sentence.
     * before sending to {@link TransformService#onNewOut(Message)}.
     *
     * @param message a new message.
     * @return False if the service would not like to intercept the message.
     * and prevent {@link TransformService#onNewOut(Message)} from receiving it.
     */
    @Override public boolean onInterceptNewOut(@NonNull final Message message) {
        requireNonNull(message);
        if (isConfirmMessage) {
            isConfirmMessage = false;
            loop(translationToken);
            return false;
        }
        return false;
    }


    @Override public void onNewOut(@NonNull final Message message) {
        requireNonNull(message);
        if (!(message.content instanceof TextContent)) {
            if (message.content instanceof Savable) {
                store.insert(message);
            }
            return;
        }
        final String content = ((TextContent) message.content).text;
        if (translateMode && !content.equals(getString(R.string.close_translation_mode))) {
            translationToken.current = content;
            handleNextStep();
        } else {
            handleContent(content);
        }
        store.insert(message);
    }


    private void handleContent(@NonNull final String content) {
        // TODO: 2016/11/13 matcher
        switch (requireNonNull(content)) {
            case "滚":
                newInReceiver().accept("但是...但是...");
                break;
            case "求王垠的最新文章":
                yinDelegate.handleContent(content);
                break;
            case "ASS":
            case "请告诉我最新的 Android SDK Source 版本":
                androidSDKDelegate.handleContent(content);
                break;
            case "发动魔法卡——混沌仪式!":
            case "混沌仪式":
                createStep();
                break;
            case "关闭混沌仪式":
            case "关闭混沌世界":
                if (translationToken != null) {
                    translationToken.stop(getString(R.string.light_and_dark_gate_close));
                    handleNextStep();
                } else {
                    insertNewIn(getString(R.string.tip_token_does_not_create));
                }
                break;
            case "每隔一小时提醒我":
                tomatoDelegate.handleContent(content);
                break;
            default:
                echoDelegate.handleContent(content);
                break;
        }
    }


    private void handleNextStep() {
        switch (translationToken.getStep()) {
            case OnStart:
                onStartStep();
                break;
            case OnWorking:
                onWorkingStep();
                break;
            case OnConfirm:
                onConfirm(translationToken);
                break;
            case OnDone:
                onDoneStep();
                break;
            case OnStop:
                onStopStep();
                break;
        }
    }


    private synchronized void loop(@NonNull final Translation input) {
        if (input.getStep() == OnWorking) {
            if (input.sources != null && input.currentIndex < input.sources.length) {
                input.current = input.sources[input.currentIndex];
                input.currentIndex += 1;
                input.next();
            } else {
                input.done(getString(R.string.translation_done));
            }
        } else {
            input.next();
        }
    }


    /**
     * Create Translation token and loop it to onStart,
     * then wait for user send the content to start.
     */
    private void createStep() {
        this.translationToken = Translations.create(getString(R.string.light_and_dark_gate_open));
        insertNewIn(translationToken.current);
        loop(translationToken);
        this.translateMode = true;
    }


    /**
     * Show guide and start the translation for the first sentence.
     * TODO: 16/7/24 split just mock for test
     */
    private void onStartStep() {
        translationToken.sources = translationToken.current.split("。");
        translationToken.current = getString(R.string.rule_translation_start);
        insertNewIn(translationToken.current);
        loop(translationToken);
        handleNextStep();
    }


    /**
     * Get the sentence, {@link TransformService#insertNewIn(String)}
     * and handle the confirm step if {@link Step#OnConfirm} after looped,
     * otherwise handle the done step.
     *
     * @see TransformService#loop(Translation)
     */
    private void onWorkingStep() {
        loop(translationToken);
        if (translationToken.getStep() == Step.OnConfirm) {
            insertNewIn(translationToken.current);
        }
        handleNextStep();
    }


    // TODO: 16/9/11 the name of the method may cause confusion.
    private void onConfirm(@NonNull final Translation token) {
        translateReaction.accept(token);
    }


    private void handleConfirm(@NonNull final String result) {
        final String text = requireNonNull(result);
        Log.d("handleConfirm", result);
        presenter.setInputText(text);
        isConfirmMessage = true;
    }


    private void onDoneStep() {
        insertNewIn(translationToken.current);
        this.translateMode = false;
    }


    private void onStopStep() {
        insertNewIn(translationToken.current);
        this.translateMode = false;
    }


    private void insertNewIn(@NonNull final String value) {
        insertNewIn(inMessageFactory.newMessage(new TextContent(value)));
    }


    private void insertNewIn(@NonNull final Message message) {
        presenter.addNewIn(message);
    }


    @NonNull public Receiver<String> newInReceiver() {
        return this::insertNewIn;
    }


    @NonNull public Receiver<Throwable> errorHandler() {
        return value -> {
            final String error;
            if (value.getMessage() != null) {
                error = value.getMessage();
            } else {
                value.printStackTrace();
                error = getString(R.string.tip_network_error_retry);
            }
            Log.e("errorHandler", error);
            newInReceiver().accept(error);
        };
    }


    @Override public void onClear() {
        store.clear();
    }


    @Override public void stop() {
        AgeraBus.repository().removeUpdatable(newInEvent);
        helper.removeObservables();
    }
}
