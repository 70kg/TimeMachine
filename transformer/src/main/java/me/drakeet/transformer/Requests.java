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
import com.google.gson.Gson;
import java.util.regex.Pattern;
import me.drakeet.transformer.entity.YouDao;

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
import static me.drakeet.transformer.Objects.requireNonNull;
import static me.drakeet.transformer.Requests.Preferences.defaultPreferences;
import static me.drakeet.transformer.Strings.toUtf8URLEncode;

/**
 * @author drakeet
 */
public class Requests {

    public static final String LIGHT_AND_DARK_GATE = "light_and_dark_gate";
    public static final String LIGHT_AND_DARK_GATE_OPEN = "混沌世界: 开启!";
    public static final String LIGHT_AND_DARK_GATE_CLOSE = "混沌世界: 关闭!";

    public final static Supplier<String> YIN = () -> "http://www.yinwang.org";
    public final static Supplier<String> YOU_DAO
        = () -> String.format(
        "http://fanyi.youdao.com/openapi.do?keyfrom=%s&key=%s" +
            "&type=data&doctype=json&version=1.1&only=translate&q=",
        BuildVars.YOUDAO_TRANSLATE_KEY_FROM,
        BuildVars.YOUDAO_TRANSLATE_KEY);


    @NonNull public static Function<String, Result<HttpResponse>> urlToResponse() {
        return Functions.functionFrom(String.class)
            .apply(requestInterceptor())
            .apply(url -> httpGetRequest(url).compile())
            .apply(httpFunction())
            .thenApply(responseInterceptor());
    }


    @NonNull public static Repository<Result<String>> requestYinAsync() {
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


    @NonNull public static Repository<Result<String>> requestYinSync() {
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


    @NonNull public static Function<HttpResponse, Result<String>> yinResponseToResult() {
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


    @NonNull
    public static Repository<Result<String>> requestTranslate(@NonNull final String content) {
        requireNonNull(content);
        return repositoryWithInitialValue(Result.<String>absent())
            .observe()
            .onUpdatesPerLoop()
            .goTo(networkExecutor)
            .getFrom(YOU_DAO)
            .transform(input -> input + toUtf8URLEncode(content))
            .attemptTransform(urlToResponse())
            .orEnd(Result::failure)
            .goTo(calculationExecutor)
            .thenTransform(youdaoResponseToResult())
            .onDeactivation(SEND_INTERRUPT)
            .compile();
    }


    @NonNull private static Function<HttpResponse, Result<String>> youdaoResponseToResult() {
        return Functions.functionFrom(HttpResponse.class)
            .apply(input -> new String(input.getBody()))
            .thenApply(json -> {
                YouDao youDao = new Gson().fromJson(json, YouDao.class);
                if (youDao.isSuccessful() && youDao.translation.size() > 0) {
                    return Result.success(youDao.translation.get(0));
                }
                return Result.failure();
            });
    }


    static class Preferences {

        private static SharedPreferences preferences;


        @NonNull static SharedPreferences defaultPreferences() {
            if (preferences != null) {
                return preferences;
            }
            preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            return preferences;
        }


        private Preferences() {
            throw new AssertionError();
        }
    }


    @NonNull public static Repository<Result<String>> observeLightAndDarkGate() {
        final SharedPreferences preferences = defaultPreferences();
        return Repositories.repositoryWithInitialValue(Result.<String>absent())
            .observe(sharedPreferencesObservable(preferences, LIGHT_AND_DARK_GATE))
            .onUpdatesPerLoop()
            .goLazy()
            .transform(input -> preferences.getBoolean(LIGHT_AND_DARK_GATE, true))
            .thenTransform(open -> {
                if (open) {
                    return Result.success(LIGHT_AND_DARK_GATE_OPEN);
                } else {
                    return Result.success(LIGHT_AND_DARK_GATE_CLOSE);
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


    private Requests() {
        throw new AssertionError();
    }
}
