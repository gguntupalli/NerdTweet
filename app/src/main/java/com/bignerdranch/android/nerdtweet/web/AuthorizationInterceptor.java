package com.bignerdranch.android.nerdtweet.web;

import android.util.Log;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by gguntupalli on 25/01/17.
 */

public class AuthorizationInterceptor implements Interceptor {
    private static final String TAG = "AuthInterceptor";
    private static final String AUTH_HEADER = "Authorization";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        TwitterOauthHelper oauthHelper = TwitterOauthHelper.get();

        try {
            String autheHeaderString =
                    oauthHelper.getAuthorizationHeaderString(request);
            request = request.newBuilder()
                    .addHeader(AUTH_HEADER, autheHeaderString)
                    .build();
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            Log.e(TAG, "Failed to get auth header",e);
        }
        return chain.proceed(request);
    }
}
