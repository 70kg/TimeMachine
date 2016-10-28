package me.drakeet.transformer;

import android.support.annotation.NonNull;
import com.google.android.agera.Repository;
import com.google.android.agera.Reservoir;

import static com.google.android.agera.Repositories.repositoryWithInitialValue;
import static com.google.android.agera.RepositoryConfig.SEND_INTERRUPT;
import static me.drakeet.transformer.Strings.empty;

/**
 * @author drakeet
 */
public class EchoDelegate extends MessageServiceDelegate {

    private Reservoir<String> echoReaction;


    public EchoDelegate(@NonNull TranslationService service) {
        super(service);
    }


    @SuppressWarnings("unchecked")
    @Override protected void prepare() {
        echoReaction = Reservoirs.<String>reactionReservoir();
        Repository<String> echoRepo = repositoryWithInitialValue(empty())
            .observe(echoReaction)
            .onUpdatesPerLoop()
            .thenAttemptGetFrom(echoReaction).orSkip()
            .notifyIf((last, cur) -> !cur.isEmpty())
            .onDeactivation(SEND_INTERRUPT)
            .compile();
        getObservableHelper().addToObservable(echoRepo,
            () -> getService().newInReceiver().accept(echoRepo.get()));
    }


    @Override protected void handleContent(@NonNull String content) {
        echoReaction.accept(content);
    }
}
