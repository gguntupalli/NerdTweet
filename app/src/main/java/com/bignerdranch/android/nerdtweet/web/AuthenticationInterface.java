package com.bignerdranch.android.nerdtweet.web;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by gguntupalli on 25/01/17.
 */

public interface AuthenticationInterface {

    @POST("oauth/request_token")
    Call<ResponseBody> fetchRequestToken(@Body String body);

    @FormUrlEncoded
    @POST("oauth/access_token")
    Call<ResponseBody> fetchAccessToken(@Field("oauth_verifier") String verifier);
}
