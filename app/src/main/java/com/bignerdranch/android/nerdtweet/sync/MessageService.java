package com.bignerdranch.android.nerdtweet.sync;

import android.accounts.Account;
import android.content.ContentResolver;
import android.os.Bundle;
import android.util.Log;

import com.bignerdranch.android.nerdtweet.account.Authenticator;
import com.bignerdranch.android.nerdtweet.contentprovider.DatabaseContract;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by gguntupalli on 26/01/17.
 */

public class MessageService extends FirebaseMessagingService {
    private static final String TAG = "MessageService";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "Received Firebase message, request sync");
        Account account = new Account(
                Authenticator.ACCOUNT_NAME, Authenticator.ACCOUNT_TYPE);
        ContentResolver.setIsSyncable(account, DatabaseContract.AUTHORITY, 1);
        ContentResolver.setSyncAutomatically(
                account, DatabaseContract.AUTHORITY, true);
        ContentResolver.requestSync(
                account, DatabaseContract.AUTHORITY, Bundle.EMPTY);
    }
}
