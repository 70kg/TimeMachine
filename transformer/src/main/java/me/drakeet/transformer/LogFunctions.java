package me.drakeet.transformer;

import com.google.android.agera.Function;
import com.google.android.agera.Result;
import com.google.android.agera.net.HttpResponse;

/**
 * @author drakeet
 */
public class LogFunctions {

    public static Function<String, String> requestInterceptor() {
        return input -> {
            Log.d("Request Interceptor", input);
            return input;
        };
    }


    public static Function<Result<HttpResponse>, Result<HttpResponse>> responseInterceptor() {
        return input -> {
            Log.d("Response Interceptor", new String(input.get().getBody()));
            return input;
        };
    }
}
