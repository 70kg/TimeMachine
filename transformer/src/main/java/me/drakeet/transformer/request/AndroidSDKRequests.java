package me.drakeet.transformer.request;

import android.support.annotation.NonNull;
import com.google.android.agera.Function;
import com.google.android.agera.Functions;
import com.google.android.agera.Repository;
import com.google.android.agera.Reservoir;
import com.google.android.agera.Result;
import com.google.android.agera.Supplier;
import com.google.android.agera.net.HttpResponse;

import static com.google.android.agera.Repositories.repositoryWithInitialValue;
import static com.google.android.agera.RepositoryConfig.SEND_INTERRUPT;
import static me.drakeet.transformer.Executors.calculationExecutor;
import static me.drakeet.transformer.Executors.networkExecutor;
import static me.drakeet.transformer.Requests.urlToResponse;

/**
 * @author drakeet
 */
public class AndroidSDKRequests {

    private final static Supplier<String> URL
        = () -> "https://dl.google.com/android/repository/repository-11.xml";


    @NonNull public static Repository<Result<String>> async(Reservoir<String> reaction) {
        return repositoryWithInitialValue(Result.<String>absent())
            .observe(reaction)
            .onUpdatesPerLoop()
            .attemptGetFrom(reaction).orSkip()
            .goTo(networkExecutor)
            .getFrom(URL)
            .attemptTransform(urlToResponse())
            .orEnd(Result::failure)
            .goTo(calculationExecutor)
            .thenTransform(mapResponse())
            .onDeactivation(SEND_INTERRUPT)
            .compile();
    }


    @NonNull public static Repository<Result<String>> sync() {
        return repositoryWithInitialValue(Result.<String>absent())
            .observe()
            .onUpdatesPerLoop()
            .getFrom(URL)
            .attemptTransform(urlToResponse())
            .orEnd(Result::failure)
            .thenTransform(mapResponse())
            .onDeactivation(SEND_INTERRUPT)
            .compile();
    }


    @NonNull private static Function<HttpResponse, Result<String>> mapResponse() {
        return Functions.functionFrom(HttpResponse.class)
            .apply(input -> new String(input.getBody()))
            .apply(body -> {
                int sourceTag = body.indexOf("<sdk:source>");
                if (sourceTag != -1) {
                    String apiLevelStart = "<sdk:api-level>";
                    String apiLevelEnd = "</sdk:api-level>";
                    int targetStart = body.indexOf(apiLevelStart, sourceTag) +
                        apiLevelStart.length();
                    int targetEnd = body.indexOf(apiLevelEnd, targetStart);
                    return body.substring(targetStart, targetEnd);
                }
                return "24";
            })
            .thenApply(version -> {
                return Result.success("为你找到最新的 Android SDK Source 是第 " + version + " 版");
            });
    }
}
