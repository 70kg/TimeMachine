package me.drakeet.transformer.request;

import android.support.annotation.NonNull;
import com.google.android.agera.Function;
import com.google.android.agera.Functions;
import com.google.android.agera.Repository;
import com.google.android.agera.Result;
import com.google.android.agera.Supplier;
import com.google.android.agera.net.HttpResponse;
import java.util.regex.Pattern;

import static com.google.android.agera.Repositories.repositoryWithInitialValue;
import static com.google.android.agera.RepositoryConfig.SEND_INTERRUPT;
import static me.drakeet.transformer.App.calculationExecutor;
import static me.drakeet.transformer.App.networkExecutor;
import static me.drakeet.transformer.Requests.urlToResponse;

/**
 * @author drakeet
 */
public class YinRequests {

    private final static Supplier<String> YIN = () -> "http://www.YinWang.org";


    @NonNull public static Repository<Result<String>> async() {
        return repositoryWithInitialValue(Result.<String>absent())
            .observe()
            .onUpdatesPerLoop()
            .goTo(networkExecutor)
            .getFrom(YIN)
            .attemptTransform(urlToResponse())
            .orEnd(Result::failure)
            .goTo(calculationExecutor)
            .thenTransform(yinResponseToResult())
            .onDeactivation(SEND_INTERRUPT)
            .compile();
    }


    @NonNull public static Repository<Result<String>> sync() {
        return repositoryWithInitialValue(Result.<String>absent())
            .observe()
            .onUpdatesPerLoop()
            .getFrom(YIN)
            .attemptTransform(urlToResponse())
            .orEnd(Result::failure)
            .thenTransform(yinResponseToResult())
            .onDeactivation(SEND_INTERRUPT)
            .compile();
    }


    @NonNull private static Function<HttpResponse, Result<String>> yinResponseToResult() {
        return Functions.functionFrom(HttpResponse.class)
            .apply(input -> new String(input.getBody()))
            .apply(body -> {
                String re = "title\">\\s+.+?href=\"([^\"]*)\">(.+?)</a>.+</li>";
                Pattern pattern = Pattern.compile(re, Pattern.DOTALL);
                return pattern.matcher(body);
            })
            .thenApply(matcher -> {
                if (matcher.find()) {
                    return Result.success("为你找到最新的一篇文章是: \n" +
                        matcher.group(2) + "\n" + matcher.group(1));
                } else {
                    return Result.absent();
                }
            });
    }
}
