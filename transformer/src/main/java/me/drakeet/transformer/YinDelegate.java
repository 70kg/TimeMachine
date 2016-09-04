package me.drakeet.transformer;

import android.support.annotation.NonNull;
import com.google.android.agera.Repository;
import com.google.android.agera.Reservoir;
import com.google.android.agera.Result;
import me.drakeet.transformer.request.YinRequests;

/**
 * @author drakeet
 */
public class YinDelegate extends MessageServiceDelegate {

    private Reservoir<String> yinReaction;


    public YinDelegate(@NonNull TranslationService service) {
        super(service);
    }


    @Override protected void prepare() {
        yinReaction = Reservoirs.<String>reactionReservoir();
        Repository<Result<String>> yinRepo = YinRequests.async(yinReaction);
        getObservableHelper().addToObservable(yinRepo, () -> yinRepo.get()
            .ifSucceededSendTo(getService().newInReceiver())
            .ifFailedSendTo(getService().errorHandler()));

    }


    @Override protected void handleContent(@NonNull String content) {
        yinReaction.accept(content);
    }
}
