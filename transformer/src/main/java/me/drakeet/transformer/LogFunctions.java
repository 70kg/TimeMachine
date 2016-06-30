package me.drakeet.transformer;

import android.util.Log;
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
            if (input.succeeded()) {
                Log.d("Response Interceptor", new String(input.get().getBody()));
            } else {
                Log.e("Response Interceptor", "Failed", input.getFailure());
            }
            return input;
        };
    }
}
