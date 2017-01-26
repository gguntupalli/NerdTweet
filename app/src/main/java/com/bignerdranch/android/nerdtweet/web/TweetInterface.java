package com.bignerdranch.android.nerdtweet.web;

import com.bignerdranch.android.nerdtweet.model.TweetSearchResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by gguntupalli on 26/01/17.
 */

public interface TweetInterface {
    @GET("search/tweets.json")
    Call<TweetSearchResponse> searchTweets(@Query("q") String query);
}
