package me.drakeet.transformer.module;

import android.support.annotation.NonNull;
import com.google.android.agera.Repository;
import com.google.android.agera.Reservoir;
import com.google.android.agera.Result;
import me.drakeet.transformer.MessageServiceDelegate;
import me.drakeet.transformer.Reservoirs;
import me.drakeet.transformer.TransformService;
import me.drakeet.transformer.request.AndroidSDKRequests;

/**
 * Query the latest Android SDK source version.
 *
 * @author drakeet
 */
public class AndroidSDKDelegate extends MessageServiceDelegate {

    private Reservoir<String> reaction;


    public AndroidSDKDelegate(@NonNull TransformService service) {
        super(service);
    }


    @Override public void prepare() {
        reaction = Reservoirs.<String>reactionReservoir();
        Repository<Result<String>> sdkRepo = AndroidSDKRequests.async(reaction);
        getObservableHelper().addToObservable(sdkRepo, () -> sdkRepo.get()
            .ifSucceededSendTo(getService().newInReceiver())
            .ifFailedSendTo(getService().errorHandler()));
    }


    @Override public void handleContent(@NonNull String content) {
        reaction.accept(content);
    }
}
