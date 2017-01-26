package com.bignerdranch.android.nerdtweet.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by gguntupalli on 26/01/17.
 */

public class SyncService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new SyncAdapter(this, true).getSyncAdapterBinder();
    }
}
