package com.mwongela.contactsyncadapter.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.mwongela.contactsyncadapter.adapter.SyncAdapter

/*
    The sync service is what binds the sync adapter to the android sync framework.
 */

class SyncService : Service() {

    private val TAG: String = javaClass.simpleName
    private var mSyncAdapter: SyncAdapter? = null

    override fun onCreate() {
        super.onCreate()

        Log.e(TAG, "Sync service created")
        synchronized(this) {
            if (mSyncAdapter == null) {
                mSyncAdapter = SyncAdapter(applicationContext, true)
            }
        }
    }

    override fun onBind(intent: Intent): IBinder {
        Log.e(TAG, "Sync service bound")
        return mSyncAdapter!!.syncAdapterBinder
    }
}
