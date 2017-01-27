package com.bignerdranch.android.nerdtweet.controller;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import com.bignerdranch.android.nerdtweet.contentprovider.TweetCursorWrapper;
import com.bignerdranch.android.nerdtweet.contentprovider.UserCursorWrapper;
import com.bignerdranch.android.nerdtweet.model.PreferenceStore;
import com.bignerdranch.android.nerdtweet.model.Tweet;
import com.bignerdranch.android.nerdtweet.model.User;
import com.bumptech.glide.Glide;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class TweetListFragment extends Fragment {

    private static final String TAG = "TweetListFragment";

    private String mAccessToken;
    private Account mAccount;
    private RecyclerView mRecyclerView;
    private TweetAdapter mTweetAdapter;
    private boolean mSyncingPeriodically;

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
    public void onStop() {
        super.onStop();
        if(mSyncingPeriodically) {
            ContentResolver.removePeriodicSync(
                    mAccount, DatabaseContract.AUTHORITY, Bundle.EMPTY);
        }
    }

    private HashMap<String, User> getUserMap() {
        Cursor userCursor = getContext().getContentResolver().query(
                DatabaseContract.User.CONTENT_URI, null, null, null, null);
        UserCursorWrapper userCursorWrapper = new UserCursorWrapper(userCursor);
        HashMap<String, User> userMap = new HashMap<>();
        User user;
        userCursorWrapper.moveToFirst();
        while(!userCursorWrapper.isAfterLast()) {
            user = userCursorWrapper.getUser();
            userMap.put(user.getServerId(), user);
            userCursorWrapper.moveToNext();
        }
        userCursor.close();
        return userMap;
    }
    private List<Tweet> getTweetList() {
        HashMap<String, User> userMap = getUserMap();
        Cursor tweetCursor = getContext().getContentResolver().query(
                DatabaseContract.Tweet.CONTENT_URI, null, null, null, null);
        TweetCursorWrapper tweetCursorWrapper = new TweetCursorWrapper(tweetCursor);
        tweetCursorWrapper.moveToFirst();
        Tweet tweet;
        User tweetUser;
        List<Tweet> tweetList = new ArrayList<>();
        while (!tweetCursorWrapper.isAfterLast()) {
            tweet = tweetCursorWrapper.getTweet();
            tweetUser = userMap.get(tweet.getUserId());
            tweet.setUser(tweetUser);
            tweetList.add(tweet);
            tweetCursorWrapper.moveToNext();
        }
        tweetCursor.close();
        return tweetList;
    }

    private void initRecyclerView() {
        if (!isAdded()) {
            return; }
        List<Tweet> tweetList = getTweetList();
        mTweetAdapter.setTweetList(tweetList);
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
                        initRecyclerView();
                        setupFirebaseMessaging();
                        getContext().getContentResolver().registerContentObserver(
                                DatabaseContract.Tweet.CONTENT_URI, true, mContentObserver );
                    }

                }, null);
    }

    private void setupFirebaseMessaging() {
        PreferenceStore preferenceStore = PreferenceStore.get(getContext());
        String currentToken = preferenceStore.getFirebaseToken();
        if(currentToken == null) {
            new FirebaseRegistrationTask().execute();
        } else {
            Log.d(TAG, "Have current token: " + currentToken);
        }
    }

    private class FirebaseRegistrationTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            if (getContext() == null) {
                return null;
            }

            return FirebaseInstanceId.getInstance().getToken();
        }
        @Override
        protected void onPostExecute(String token) {
            if (token == null) {
                setupPeriodicSync();
                return; }
            Log.d(TAG, "Have new token: " + token);
            PreferenceStore.get(getContext()).setFirebaseToken(token);
        }
    }

    private void setupPeriodicSync() {
        mSyncingPeriodically = true;
        ContentResolver.setIsSyncable(mAccount, DatabaseContract.AUTHORITY, 1);
        ContentResolver.setSyncAutomatically(
                mAccount, DatabaseContract.AUTHORITY, true);
        ContentResolver.addPeriodicSync(
                mAccount, DatabaseContract.AUTHORITY, Bundle.EMPTY, 30);
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

    private ContentObserver mContentObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            initRecyclerView();
        }
    };
}
