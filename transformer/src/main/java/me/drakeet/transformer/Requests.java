package me.drakeet.transformer;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import com.google.android.agera.Function;
import com.google.android.agera.Functions;
import com.google.android.agera.Repositories;
import com.google.android.agera.Repository;
import com.google.android.agera.Result;
import com.google.android.agera.Supplier;
import com.google.android.agera.net.HttpResponse;
import java.util.regex.Pattern;

import static com.google.android.agera.Repositories.repositoryWithInitialValue;
import static com.google.android.agera.RepositoryConfig.SEND_INTERRUPT;
import static com.google.android.agera.content.ContentObservables.sharedPreferencesObservable;
import static com.google.android.agera.net.HttpFunctions.httpFunction;
import static com.google.android.agera.net.HttpRequests.httpGetRequest;
import static me.drakeet.transformer.App.calculationExecutor;
import static me.drakeet.transformer.App.getContext;
import static me.drakeet.transformer.App.networkExecutor;
import static me.drakeet.transformer.LogFunctions.requestInterceptor;
import static me.drakeet.transformer.LogFunctions.responseInterceptor;
import static me.drakeet.transformer.Requests.Preferences.defaultPreferences;

/**
 * @author drakeet
 */
public class Requests {

    public static final String LIGHT_AND_DARK_GATE = "light_and_dark_gate";

    public static Supplier<String> yin = () -> "http://www.yinwang.org";


    public static Function<String, Result<HttpResponse>> urlToResponse() {
        return Functions.functionFrom(String.class)
            .apply(requestInterceptor())
            .apply(url -> httpGetRequest(url).compile())
            .apply(httpFunction())
            .thenApply(responseInterceptor());
    }


    public static Repository<Result<String>> requestYinAsync() {
        return repositoryWithInitialValue(Result.<String>absent())
            .observe()
            .onUpdatesPerLoop()
            .goTo(networkExecutor)
            .getFrom(yin)
            .attemptTransform(urlToResponse())
            .orEnd(Result::failure)
            .goTo(calculationExecutor)
            .thenTransform(yinResponseToResult())
            .onDeactivation(SEND_INTERRUPT)
            .compile();
    }


    public static Repository<Result<String>> requestYinSync() {
        return repositoryWithInitialValue(Result.<String>absent())
            .observe()
            .onUpdatesPerLoop()
            .getFrom(yin)
            .attemptTransform(urlToResponse())
            .orEnd(Result::failure)
            .thenTransform(yinResponseToResult())
            .onDeactivation(SEND_INTERRUPT)
            .compile();
    }


    public static Function<HttpResponse, Result<String>> yinResponseToResult() {
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


    public static class Preferences {

        private static SharedPreferences preferences;


        @NonNull public static SharedPreferences defaultPreferences() {
            if (preferences != null) {
                return preferences;
            }
            preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            return preferences;
        }


        private Preferences() {}
    }


    @NonNull public static Repository<Result<String>> observeLightAndDarkGate() {
        final SharedPreferences preferences = defaultPreferences();
        return Repositories.repositoryWithInitialValue(Result.<String>absent())
            .observe(sharedPreferencesObservable(preferences, LIGHT_AND_DARK_GATE))
            .onUpdatesPerLoop()
            .goLazy()
            .transform(input -> preferences.getBoolean(LIGHT_AND_DARK_GATE, true))
            .thenTransform(input -> {
                if (input) {
                    return Result.success("混沌世界: 开启!");
                } else {
                    return Result.success("混沌世界: 关闭!");
                }
            })
            .onDeactivation(SEND_INTERRUPT)
            .compile();
    }


    public static void lightAndDarkGateTerminal(final boolean open) {
        final SharedPreferences preferences = defaultPreferences();
        preferences.edit()
            .putBoolean(LIGHT_AND_DARK_GATE, open)
            .apply();
    }


    private Requests() {}
}
