package com.bignerdranch.android.nerdtweet.controller;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.bignerdranch.android.nerdtweet.account.Authenticator;
import com.bignerdranch.android.nerdtweet.web.AuthenticationInterface;
import com.bignerdranch.android.nerdtweet.web.AuthorizationInterceptor;
import com.bignerdranch.android.nerdtweet.web.TwitterOauthHelper;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AuthenticationActivity extends AccountAuthenticatorActivity {

    private static final String TAG = "AuthenticationActivity";
    private static final String EXTRA_ACCOUNT_TYPE =
            "com.bignerdranch.android.nerdtweet.ACCOUNT_TYPE";
    private static final String EXTRA_AUTH_TYPE =
            "com.bignerdranch.android.nerdtweet.AUTH_TYPE";
    private static final String TWITTER_ENDPOINT = "https://api.twitter.com/";
    private static final String TWITTER_OAUTH_ENDPOINT =
            "https://api.twitter.com/oauth/authorize";
    private static final String CALLBACK_URL = "https://www.bignerdranch.com";
    public static final String OAUTH_TOKEN_SECRET_KEY =
            "com.bignerdranch.android.nerdtweet.OAUTH_TOKEN_SECRET";

    private WebView mWebView;
    private Retrofit mRetrofit;
    private TwitterOauthHelper mTwitterOauthHelper;
    private AuthenticationInterface mAuthenticationInterface;

    public static Intent newIntent(Context context, String accountType, String authTokenType) {
        Intent intent = new Intent(context, AuthenticationActivity.class);
        intent.putExtra(EXTRA_ACCOUNT_TYPE, accountType);
        intent.putExtra(EXTRA_AUTH_TYPE, authTokenType);
        return intent;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWebView = new WebView(this);
        setContentView(mWebView);
        mWebView.setWebViewClient(mWebViewClient);

        mTwitterOauthHelper = TwitterOauthHelper.get();
        mTwitterOauthHelper.resetOauthToken();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(new AuthorizationInterceptor())
                .build();

        mRetrofit = new Retrofit.Builder()
                .baseUrl(TWITTER_ENDPOINT)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(new Gson()))
                .build();

        mAuthenticationInterface =
                mRetrofit.create(AuthenticationInterface.class);
        mAuthenticationInterface.fetchRequestToken("")
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call,
                                           Response<ResponseBody> response) {
                        Uri uri = getResponseUri(response.body());
                        String oauthToken = uri.getQueryParameter("oauth_token");
                        Uri twitterOauthUri = Uri.parse(TWITTER_OAUTH_ENDPOINT).buildUpon()
                                .appendQueryParameter("oauth_token", oauthToken)
                                .build();
                        mWebView.loadUrl(twitterOauthUri.toString());
                    }
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e(TAG, "Failed to fetch request token", t);
                    }
                });
    }

    private Uri getResponseUri(ResponseBody response) {
        String responseBody = "";
        try {
            if (response != null) {
                responseBody = new String(response.bytes());
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to get response body", e);
        }
        String parseUrl = "http://localhost?" + responseBody;
        return Uri.parse(parseUrl);
    }


    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String url) {
            if (!url.contains(CALLBACK_URL)) {
                return true;
            }
            Uri callbackUri = Uri.parse(url);
            String oauthToken = callbackUri.getQueryParameter("oauth_token");
            String oauthVerifier = callbackUri.getQueryParameter("oauth_verifier");
            mTwitterOauthHelper.setOauthToken(oauthToken, null);
            mAuthenticationInterface.fetchAccessToken(oauthVerifier)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call,
                                               Response<ResponseBody> response) {
                            Uri uri = getResponseUri(response.body());
                            String oauthToken = uri.getQueryParameter("oauth_token");
                            String oauthTokenSecret =
                                    uri.getQueryParameter("oauth_token_secret");
                            mTwitterOauthHelper.setOauthToken(oauthToken, oauthTokenSecret);
                            setupAccount(oauthToken, oauthTokenSecret);
                            final Intent intent = createAccountManagerIntent(oauthToken);
                            setAccountAuthenticatorResult(intent.getExtras());
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                        } });
            return true;
        }
    };

    private void setupAccount(String oauthToken, String oauthTokenSecret) {
        String accountType = getIntent().getStringExtra(EXTRA_ACCOUNT_TYPE);
        final Account account = new Account(Authenticator.ACCOUNT_NAME, accountType);
        String authTokenType = getIntent().getStringExtra(EXTRA_AUTH_TYPE);
        AccountManager accountManager =
                AccountManager.get(AuthenticationActivity.this);
        accountManager.addAccountExplicitly(account, null, null);
        accountManager.setAuthToken(account, authTokenType, oauthToken);
        accountManager.setUserData(account, OAUTH_TOKEN_SECRET_KEY, oauthTokenSecret);
    }

    private Intent createAccountManagerIntent(String oauthToken) {
        final Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, Authenticator.ACCOUNT_NAME);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE,
                getIntent().getStringExtra(EXTRA_ACCOUNT_TYPE));
        intent.putExtra(AccountManager.KEY_AUTHTOKEN, oauthToken);
        return intent;
    }
}
