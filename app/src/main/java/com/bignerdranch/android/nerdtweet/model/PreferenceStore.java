package com.bignerdranch.android.nerdtweet.model;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by gguntupalli on 26/01/17.
 */

public class PreferenceStore {
    private static final String FIREBASE_TOKEN_KEY =
            "com.bignerdranch.android.nerdtweet.FIREBASE_TOKEN";
    private static PreferenceStore sPreferenceStore;
    private Context mContext;
    public static PreferenceStore get(Context context) {
        if (sPreferenceStore == null) {
            sPreferenceStore = new PreferenceStore(context);
        }
        return sPreferenceStore;
    }
    private PreferenceStore(Context context) {
        mContext = context.getApplicationContext();
    }
    public String getFirebaseToken() {
        return PreferenceManager.getDefaultSharedPreferences(mContext)
                .getString(FIREBASE_TOKEN_KEY, null);
    }
    public void setFirebaseToken(String token) {
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .edit()
                .putString(FIREBASE_TOKEN_KEY, token)
                .apply();
    }
}
