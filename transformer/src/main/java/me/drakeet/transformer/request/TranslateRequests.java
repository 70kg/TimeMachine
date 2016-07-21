package me.drakeet.transformer.request;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import com.google.android.agera.Function;
import com.google.android.agera.Functions;
import com.google.android.agera.Merger;
import com.google.android.agera.Repositories;
import com.google.android.agera.Repository;
import com.google.android.agera.Reservoir;
import com.google.android.agera.Result;
import com.google.android.agera.Supplier;
import com.google.android.agera.net.HttpResponse;
import com.google.gson.Gson;
import me.drakeet.transformer.BuildVars;
import me.drakeet.transformer.entity.Step;
import me.drakeet.transformer.entity.Translation;
import me.drakeet.transformer.entity.YouDao;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.google.android.agera.Repositories.repositoryWithInitialValue;
import static com.google.android.agera.RepositoryConfig.SEND_INTERRUPT;
import static com.google.android.agera.content.ContentObservables.sharedPreferencesObservable;
import static me.drakeet.transformer.App.calculationExecutor;
import static me.drakeet.transformer.App.networkExecutor;
import static me.drakeet.transformer.Objects.requireNonNull;
import static me.drakeet.transformer.Requests.urlToResponse;
import static me.drakeet.transformer.Strings.toUtf8URLEncode;

/**
 * @author drakeet
 */
public class TranslateRequests {

    private static final String LIGHT_AND_DARK_GATE = "light_and_dark_gate";
    public static final String LIGHT_AND_DARK_GATE_OPEN = "混沌世界: 开启!\n请发送一篇你需要翻译的内容";
    public static final String LIGHT_AND_DARK_GATE_CLOSE = "混沌世界: 关闭!";

    private final static Supplier<String> YOU_DAO
        = () -> String.format(
        "http://fanyi.youdao.com/openapi.do?keyfrom=%s&key=%s" +
            "&type=data&doctype=json&version=1.1&only=translate&q=",
        BuildVars.YOUDAO_TRANSLATE_KEY_FROM,
        BuildVars.YOUDAO_TRANSLATE_KEY);


    private static Function<Translation, Result<Translation>> onCreateFunction() {
        return Functions.functionFrom(Translation.class)
            .thenApply(Result::success);
    }


    private static Function<Translation, Result<Translation>> onStartFunction() {
        return Functions.functionFrom(Translation.class)
            .thenApply(input -> {
                input.text = "start";
                input.step = input.step.next();
                return Result.success(input);
            });
    }


    private static Merger<Translation, String, String> urlMerger() {
        return (input, baseUrl) -> {
            final String source = requireNonNull(input.text);
            return baseUrl + toUtf8URLEncode(source);
        };
    }


    @NonNull
    public static Repository<Result<Translation>> translation(
        @NonNull Reservoir<Translation> reaction) {
        requireNonNull(reaction);
        return repositoryWithInitialValue(Result.<Translation>absent())
            .observe(reaction)
            .onUpdatesPerLoop()
            .attemptGetFrom(reaction).orSkip()
            .goTo(networkExecutor)
            .check(input -> input.step == Step.OnWorking)
            .orEnd(input -> {
                switch (input.step) {
                    case OnCreate:
                        return onCreateFunction().apply(input);
                    case OnStart:
                        return onStartFunction().apply(input);
                    case OnDone:
                        // TODO: 16/7/19
                        return Result.failure();
                    default:
                        // TODO: 16/7/19
                        return Result.failure();
                }
            })
            .mergeIn(YOU_DAO, urlMerger())
            .attemptTransform(urlToResponse())
            .orEnd(Result::failure)
            .goTo(calculationExecutor)
            .transform(youdaoResponseToResult())
            .thenTransform(input -> {
                if (input.succeeded()) {
                    return Result.success(Translation.working(input.get()));
                } else {
                    return Result.failure(input.getFailure());
                }
            })
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


    public static void lightAndDarkGateTerminal(@NonNull Context context, final boolean open) {
        final SharedPreferences preferences = getDefaultSharedPreferences(context);
        preferences.edit()
            .putBoolean(LIGHT_AND_DARK_GATE, open)
            .apply();
    }


    @NonNull
    public static Repository<Result<String>> observeLightAndDarkGate(@NonNull Context context) {
        final SharedPreferences preferences = getDefaultSharedPreferences(context);
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


    private TranslateRequests() {
        throw new AssertionError();
    }

}
