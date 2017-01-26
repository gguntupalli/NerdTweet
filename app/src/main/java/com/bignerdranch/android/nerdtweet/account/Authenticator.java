package com.bignerdranch.android.nerdtweet.account;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.bignerdranch.android.nerdtweet.controller.AuthenticationActivity;

/**
 * Created by gguntupalli on 25/01/17.
 */

public class Authenticator extends AbstractAccountAuthenticator {
    public static final String ACCOUNT_NAME = "NerdTweet";
    public static final String ACCOUNT_TYPE =
            "com.bignerdranch.android.nerdtweet.USER_ACCOUNT";
    public static final String AUTH_TOKEN_TYPE =
            "com.bignerdranch.android.nerdtweet.FULL_ACCESS";
    private Context mContext;
    public Authenticator(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        Intent intent = AuthenticationActivity.newIntent(
                mContext, accountType, authTokenType);
        Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        AccountManager accountManager = AccountManager.get(mContext);

        Bundle result = new Bundle();
        String authToken = accountManager.peekAuthToken(account, authTokenType);
        if(TextUtils.isEmpty(authToken)) {
            Intent intent = AuthenticationActivity.newIntent(
                    mContext, account.type, authTokenType);
            result.putParcelable(AccountManager.KEY_INTENT, intent);
        } else {
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
        }
        return result;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        return null;
    }
}
