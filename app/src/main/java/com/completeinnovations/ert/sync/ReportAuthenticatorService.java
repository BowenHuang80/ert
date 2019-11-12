package com.completeinnovations.ert.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ReportAuthenticatorService extends Service {

    private ReportAuthenticator reportAuthenticator;

    @Override
    public void onCreate() {
        reportAuthenticator = new ReportAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return reportAuthenticator.getIBinder();
    }
}
