package com.mwongela.contactsyncadapter

import android.Manifest
import android.accounts.Account
import android.accounts.AccountManager
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mwongela.contactsyncadapter.util.Constants
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {

    private val TAG: String = "MainActivity"

    private val PERMISSIONS_REQUEST_CODE: Int = 1

    private lateinit var mAccount: Account
    private val SECONDS_IN_A_MINUTE = 60L
    private val NUMBER_OF_MINUTES = 15L
    private val SYNC_INTERVAL =
        NUMBER_OF_MINUTES * SECONDS_IN_A_MINUTE //minimum sync duration 15mins


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //request contact permission before adding app account
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS),
                PERMISSIONS_REQUEST_CODE
            )
        } else {
            addAppAccount()
        }
    }

    /**
     * Method to check if app account is already added
     */
    private fun checkIfAppAccountExists(): Boolean {
        var accountExists = false
        for (account in AccountManager.get(this).accounts) {
            if (account.type == Constants.ACCOUNT_TYPE) {
                accountExists = true
                break
            }
        }
        return accountExists
    }

    /**
     * Method to add app account to device

     * this can be nested if your app has accounts -
     * add each individual account, so that its synced differently
     */
    private fun addAppAccount() {
        mAccount = Account(Constants.ACCOUNT_NAME, Constants.ACCOUNT_TYPE)

        if (!checkIfAppAccountExists()) {
            if (AccountManager.get(this).addAccountExplicitly(mAccount, null, null)) {
                ContentResolver.setSyncAutomatically(mAccount, ContactsContract.AUTHORITY, true)
                ContentResolver.addPeriodicSync(
                    mAccount,
                    ContactsContract.AUTHORITY,
                    Bundle(),
                    SYNC_INTERVAL
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        addAppAccount()
                    } else {
                        showPermissionsAlert()
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    //region Receive Sync completed broadcast from SyncAdapter
    companion object {
        const val ACTION_SYNC_COMPLETED: String =
            "com.mwongela.contactsyncadapter.ACTION_FINISHED_SYNC"
    }

    private var mSyncBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.e(TAG, "Contact sync Completed")
            Toast.makeText(this@MainActivity, "Contact sync Completed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(ACTION_SYNC_COMPLETED)
        registerReceiver(mSyncBroadcastReceiver, intentFilter)
    }

    override fun onPause() {
        unregisterReceiver(mSyncBroadcastReceiver)
        super.onPause()
    }
    //endregion

    /**
     * Method to show permissions alert
     */
    private fun showPermissionsAlert() {
        val builder = AlertDialog.Builder(this)

        builder.setMessage(getString(R.string.permissions_alert_text))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.go_to_settings)) { dialog, which ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
                dialog.cancel()
                finish()
            }

        val alert = builder.create()
        alert.setTitle(getString(R.string.permissions_alert))
        alert.show()
    }

    /**
     * Method to handle button clicks
     */
    fun handleClick(view: View) {
        when (view.id) {

            R.id.btn_sync_contacts -> {
                // refresh contacts by calling SyncAdapter
                SyncContacts(this).execute()
            }
        }
    }

    private class SyncContacts internal constructor(context: MainActivity) :
        AsyncTask<String?, String?, String?>() {

        private val mainActivityWeakReference: WeakReference<MainActivity>

        init {
            mainActivityWeakReference = WeakReference<MainActivity>(context)
        }

        override fun onPreExecute() {
            val mainActivity: MainActivity = mainActivityWeakReference.get() ?: return
            mainActivity.showpDialogue("Syncing")
        }

        override fun onProgressUpdate(vararg values: String?) {

        }

        override fun doInBackground(vararg params: String?): String? {
            val mainActivity: MainActivity = mainActivityWeakReference.get() ?: return null

            val settingsBundle = Bundle().apply {
                putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
                putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
            }
            ContentResolver.requestSync(
                mainActivity.mAccount,
                ContactsContract.AUTHORITY,
                settingsBundle
            )

            return null
        }


        override fun onPostExecute(result: String?) {
            val mainActivity: MainActivity = mainActivityWeakReference.get() ?: return
            mainActivity.hidepDialog()
        }
    }

    private fun showpDialogue(message: String) {
        try {

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hidepDialog() {
        try {

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}