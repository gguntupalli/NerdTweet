package com.bignerdranch.android.nerdtweet.controller;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bignerdranch.android.nerdtweet.R;
import com.bignerdranch.android.nerdtweet.account.Authenticator;
import com.bignerdranch.android.nerdtweet.contentprovider.DatabaseContract;
import com.bignerdranch.android.nerdtweet.model.Tweet;
import com.bignerdranch.android.nerdtweet.model.User;
import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class TweetListFragment extends Fragment {

    private static final String TAG = "TweetListFragment";

    private String mAccessToken;
    private Account mAccount;
    private RecyclerView mRecyclerView;
    private TweetAdapter mTweetAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tweet_list, container, false);
        mRecyclerView = (RecyclerView)
                view.findViewById(R.id.fragment_tweet_list_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mTweetAdapter = new TweetAdapter(new ArrayList<Tweet>());
        mRecyclerView.setAdapter(mTweetAdapter);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        fetchAccessToken();
    }

    @Override
    public void onResume() {
        super.onResume();
        clearDb();
        testInsert();
        testQuery();
    }
    private void clearDb() {
        getContext().getContentResolver()
                .delete(DatabaseContract.User.CONTENT_URI, null, null);
        getContext().getContentResolver()
                .delete(DatabaseContract.Tweet.CONTENT_URI, null, null);
    }

    private void testInsert() {
        User user = new User("server_id", "My screen name", "my photo url");
        Tweet tweet = new Tweet("server_id", "My first tweet", 0, 0, user);
        Uri userUri = getContext().getContentResolver()
                .insert(DatabaseContract.User.CONTENT_URI, user.getContentValues());
        Log.d(TAG, "Inserted user into uri: " + userUri);
        Uri tweetUri = getContext().getContentResolver()
                .insert(DatabaseContract.Tweet.CONTENT_URI, tweet.getContentValues());
        Log.d(TAG, "Inserted tweet into uri: " + tweetUri);
    }

    private void testQuery() {
        Cursor userCursor = getContext().getContentResolver()
                .query(DatabaseContract.User.CONTENT_URI, null, null, null, null);
        Log.d(TAG, "Have user cursor: " + userCursor);
        userCursor.close();
        Cursor tweetCursor = getContext().getContentResolver()
                .query(DatabaseContract.Tweet.CONTENT_URI, null, null, null, null);
        Log.d(TAG, "Have tweet cursor: " + tweetCursor);
        tweetCursor.close();
    }

    private void fetchAccessToken() {
        AccountManager accountManager = AccountManager.get(getContext());
        mAccount = new Account(Authenticator.ACCOUNT_NAME,
                Authenticator.ACCOUNT_TYPE);
        accountManager.getAuthToken(
                mAccount, Authenticator.AUTH_TOKEN_TYPE, null, getActivity(),
                new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        try {
                            Bundle bundle = future.getResult();
                            mAccessToken = bundle.getString(
                                    AccountManager.KEY_AUTHTOKEN);
                            Log.d(TAG, "Have access token: " + mAccessToken);
                        } catch (AuthenticatorException |
                                OperationCanceledException |
                                IOException e) {
                            Log.e(TAG, "Got an exception", e);
                        }
                    }

                }, null);
    }


    private class TweetAdapter extends RecyclerView.Adapter<TweetHolder> {
        private List<Tweet> mTweetList;

        public TweetAdapter(List<Tweet> tweetList) {
            mTweetList = tweetList;
        }

        public void setTweetList(List<Tweet> tweetList) {
            mTweetList = tweetList;
            notifyDataSetChanged();
        }

        @Override
        public TweetHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View view = inflater.inflate(R.layout.list_item_tweet, parent, false);
            return new TweetHolder(view);
        }

        @Override
        public void onBindViewHolder(TweetHolder holder, int position) {
            Tweet tweet = mTweetList.get(position);
            holder.bindTweet(tweet);
        }

        @Override
        public int getItemCount() {
            return mTweetList.size();
        }
    }

    private class TweetHolder extends RecyclerView.ViewHolder {
        private ImageView mProfileImageView;
        private TextView mTweetTextView;
        private TextView mScreenNameTextView;

        public TweetHolder(View itemView) {
            super(itemView);
            mProfileImageView = (ImageView) itemView
                    .findViewById(R.id.list_item_tweet_user_profile_image);
            mTweetTextView = (TextView) itemView
                    .findViewById(R.id.list_item_tweet_tweet_text_view);
            mScreenNameTextView = (TextView) itemView
                    .findViewById(R.id.list_item_tweet_user_screen_name_text_view);
        }

        public void bindTweet(Tweet tweet) {
            mTweetTextView.setText(tweet.getText());
            if (tweet.getUser() != null) {
                mScreenNameTextView.setText(tweet.getUser().getScreenName());
                Glide.with(getContext())
                        .load(tweet.getUser().getPhotoUrl()).into(mProfileImageView);
            }
        }
    }
}
