package me.drakeet.transformer.request;

import android.support.annotation.NonNull;
import com.google.android.agera.Function;
import com.google.android.agera.Functions;
import com.google.android.agera.Merger;
import com.google.android.agera.Result;
import com.google.android.agera.Supplier;
import com.google.android.agera.net.HttpResponse;
import com.google.gson.Gson;
import me.drakeet.transformer.BuildVars;
import me.drakeet.transformer.entity.Translation;
import me.drakeet.transformer.entity.YouDao;

import static me.drakeet.timemachine.Objects.requireNonNull;
import static me.drakeet.transformer.Strings.toUtf8URLEncode;

/**
 * @author drakeet
 */
public class TranslateRequests {

    @NonNull public final static Supplier<String> YOU_DAO
        = () -> String.format(
        "http://fanyi.youdao.com/openapi.do?keyfrom=%s&key=%s" +
            "&type=data&doctype=json&version=1.1&only=translate&q=",
        BuildVars.YOUDAO_TRANSLATE_KEY_FROM,
        BuildVars.YOUDAO_TRANSLATE_KEY);


    @NonNull public static Merger<Translation, String, String> current2UrlMerger() {
        return (input, baseUrl) -> {
            final String source = requireNonNull(input.current);
            return baseUrl + toUtf8URLEncode(source);
        };
    }


    @NonNull public static Function<HttpResponse, Result<String>> youdaoResponseToResult() {
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


    private TranslateRequests() {
        throw new AssertionError();
    }
}
