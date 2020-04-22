package com.mwongela.contactsyncadapter.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.mwongela.contactsyncadapter.account.Authenticator

class AuthenticationService : Service() {

    private val TAG: String = javaClass.simpleName
    private lateinit var mAuthenticator: Authenticator

    override fun onCreate() {
        super.onCreate()

        Log.e(TAG, "SyncAdapter Authentication service started")
        mAuthenticator = Authenticator(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "SyncAdapter Authentication service stopped")
    }

    override fun onBind(intent: Intent): IBinder {
        Log.e(TAG, "Returning the AccountAuthenticator binder for intent $intent")
        return mAuthenticator.iBinder
    }
}
