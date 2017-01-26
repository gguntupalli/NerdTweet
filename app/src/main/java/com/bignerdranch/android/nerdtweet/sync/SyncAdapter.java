package com.bignerdranch.android.nerdtweet.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.bignerdranch.android.nerdtweet.account.Authenticator;
import com.bignerdranch.android.nerdtweet.contentprovider.DatabaseContract;
import com.bignerdranch.android.nerdtweet.controller.AuthenticationActivity;
import com.bignerdranch.android.nerdtweet.model.Tweet;
import com.bignerdranch.android.nerdtweet.model.TweetSearchResponse;
import com.bignerdranch.android.nerdtweet.model.User;
import com.bignerdranch.android.nerdtweet.web.AuthorizationInterceptor;
import com.bignerdranch.android.nerdtweet.web.TweetInterface;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Created by gguntupalli on 26/01/17.
 */

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TWITTER_ENDPOINT = "https://api.twitter.com/1.1/";
    private static final String QUERY = "Android";

    private String mAccessTokenSecret;
    private String mAccessToken;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        AccountManager accountManager = AccountManager.get(context); Account account = new Account(
                Authenticator.ACCOUNT_NAME, Authenticator.ACCOUNT_TYPE);
        mAccessTokenSecret = accountManager.getUserData(
                account, AuthenticationActivity.OAUTH_TOKEN_SECRET_KEY);
        mAccessToken = accountManager.peekAuthToken(
                account, Authenticator.AUTH_TOKEN_TYPE);
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s,
                              ContentProviderClient contentProviderClient,
                              SyncResult syncResult) {
        List<Tweet> tweets = fetchTweets();
        insertTweetData(tweets);

    }

    private List<Tweet> fetchTweets() {
        List<Tweet> tweets = new ArrayList<>();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthorizationInterceptor())
                .addInterceptor(loggingInterceptor)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TWITTER_ENDPOINT)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(new Gson()))
                .build();
        TweetInterface tweetInterface = retrofit.create(TweetInterface.class);
        try {
            Response<TweetSearchResponse> response
                    = tweetInterface.searchTweets(QUERY).execute();
            tweets = response.body().getTweetList();
        } catch (IOException e) {
            Log.e(TAG, "Failed to fetch tweets", e);
        }
        return tweets;
    }

    private void insertTweetData(List<Tweet> tweets) {
        User user;
        for(Tweet tweet: tweets) {
            user = tweet.getUser();
            getContext().getContentResolver().insert(
                    DatabaseContract.User.CONTENT_URI, user.getContentValues());
            getContext().getContentResolver().insert(
                    DatabaseContract.Tweet.CONTENT_URI, tweet.getContentValues());
        }
    }
}
