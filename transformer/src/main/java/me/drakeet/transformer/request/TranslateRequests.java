package me.drakeet.transformer.request;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;
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
import me.drakeet.transformer.StringRes;
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
import static me.drakeet.transformer.StringRes.CLOSE_TRANSLATION_ERROR;
import static me.drakeet.transformer.Strings.toUtf8URLEncode;
import static me.drakeet.transformer.entity.Step.OnConfirm;
import static me.drakeet.transformer.entity.Translation.LIGHT_AND_DARK_GATE_CLOSE;
import static me.drakeet.transformer.entity.Translation.LIGHT_AND_DARK_GATE_OPEN;

/**
 * @author drakeet
 */
public class TranslateRequests {

    private static final String LIGHT_AND_DARK_GATE = "light_and_dark_gate";

    @NonNull private final static Supplier<String> YOU_DAO
        = () -> String.format(
        "http://fanyi.youdao.com/openapi.do?keyfrom=%s&key=%s" +
            "&type=data&doctype=json&version=1.1&only=translate&q=",
        BuildVars.YOUDAO_TRANSLATE_KEY_FROM,
        BuildVars.YOUDAO_TRANSLATE_KEY);


    public static void loop(@NonNull final Translation translation) {
        translation.step = translation.step.next();
    }


    @NonNull private static Function<Translation, Result<Translation>> onCreateFunction() {
        return Functions.functionFrom(Translation.class)
            .thenApply(Result::success);
    }


    @NonNull private static Function<Translation, Result<Translation>> onStartFunction() {
        return Functions.functionFrom(Translation.class)
            .thenApply(input -> {
                Log.d("onStartFunction", input.toString());
                // TODO: 16/7/24 split just mock for test
                // 必须要新对象
                final Translation result = input.clone();
                result.current = StringRes.TRANSLATION_START_RULE;
                result.sources = input.current.split("。");
                result.last = input;
                return Result.success(result);
            });
    }


    @NonNull private static Function<Translation, Result<Translation>> onWorkingFunction() {
        return Functions.functionFrom(Translation.class)
            .thenApply(input -> {
                Log.d("onWorkingFunction", input.toString());
                // 必须要新对象
                final Translation result = input.clone();
                if (result.sources != null && result.currentIndex < result.sources.length) {
                    result.current = result.sources[result.currentIndex];
                    result.currentIndex += 1;
                }
                result.last = input;
                return Result.success(result);
            });
    }


    @NonNull private static Function<Translation, Result<Translation>> onStopFunction() {
        return Functions.functionFrom(Translation.class)
            .thenApply(input -> {
                if (input.last == null) {
                    return Result.failure(new Throwable(CLOSE_TRANSLATION_ERROR));
                } else {
                    return Result.success(Translation.stop());
                }
            });
    }


    @NonNull private static Merger<Translation, String, String> urlMerger() {
        return (input, baseUrl) -> {
            final String source = requireNonNull(input.current);
            return baseUrl + toUtf8URLEncode(source);
        };
    }


    /**
     * Handle the steps except {@link Step#OnConfirm}
     *
     * @return Functions apply result
     */
    @NonNull private static Function<Translation, Result<Translation>> uncaughtStepHandler() {
        return input -> {
            Log.d("uncaughtStepHandler", input.toString());
            switch (input.step) {
                case OnCreate:
                    return onCreateFunction().apply(input);
                case OnStart:
                    return onStartFunction().apply(input);
                case OnWorking:
                    return onWorkingFunction().apply(input);
                case OnStop:
                    return onStopFunction().apply(input);
                default:
                    return Result.failure();
            }
        };
    }


    @NonNull public static Repository<Result<Translation>> translation(
        @NonNull Reservoir<Translation> reaction) {
        requireNonNull(reaction);
        return repositoryWithInitialValue(Result.<Translation>absent())
            .observe(reaction)
            .onUpdatesPerLoop()
            .attemptGetFrom(reaction).orSkip()
            .goTo(networkExecutor)
            .check(input -> input.step == OnConfirm)
            .orEnd(uncaughtStepHandler())
            .mergeIn(YOU_DAO, urlMerger())
            .attemptTransform(urlToResponse())
            .orEnd(Result::failure)
            .goTo(calculationExecutor)
            .transform(youdaoResponseToResult())
            .goLazy()
            .thenTransform(input -> {
                if (input.succeeded()) {
                    return Result.success(Translation.confirm(input.get()));
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
