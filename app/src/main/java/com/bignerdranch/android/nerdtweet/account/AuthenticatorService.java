package com.bignerdranch.android.nerdtweet.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by gguntupalli on 25/01/17.
 */

public class AuthenticatorService extends Service {
    private Authenticator mAuthenticator;
    public AuthenticatorService() {
        mAuthenticator = new Authenticator(this);
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
