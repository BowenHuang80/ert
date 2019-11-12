package com.completeinnovations.ert.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ReportSyncService extends Service {

    private static final Object syncAdapterLock = new Object();
    private static ReportSyncAdapter reportSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("ReportSyncService", "onCreate - ReportSyncService");
        synchronized (syncAdapterLock) {
            if (reportSyncAdapter == null) {
                reportSyncAdapter = new ReportSyncAdapter
                        (getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return reportSyncAdapter.getSyncAdapterBinder();
    }
}
