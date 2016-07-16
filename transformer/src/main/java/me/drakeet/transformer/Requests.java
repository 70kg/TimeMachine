package me.drakeet.transformer;

import android.support.annotation.NonNull;
import com.google.android.agera.Function;
import com.google.android.agera.Functions;
import com.google.android.agera.Result;
import com.google.android.agera.net.HttpResponse;

import static com.google.android.agera.net.HttpFunctions.httpFunction;
import static com.google.android.agera.net.HttpRequests.httpGetRequest;
import static me.drakeet.transformer.LogFunctions.requestInterceptor;
import static me.drakeet.transformer.LogFunctions.responseInterceptor;

/**
 * @author drakeet
 */
public class Requests {

    @NonNull public static Function<String, Result<HttpResponse>> urlToResponse() {
        return Functions.functionFrom(String.class)
            .apply(requestInterceptor())
            .apply(url -> httpGetRequest(url).compile())
            .apply(httpFunction())
            .thenApply(responseInterceptor());
    }
}
