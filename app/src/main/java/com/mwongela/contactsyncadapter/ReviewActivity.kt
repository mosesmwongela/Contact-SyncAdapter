package com.mwongela.contactsyncadapter

import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_review.*

class ReviewActivity : AppCompatActivity() {

    private val TAG: String = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        if (intent != null && intent.data != null) {
            Log.e(TAG, intent.data.toString())

            var contactName = ""
            val cursor = contentResolver.query(
                intent.data!!,
                arrayOf(
                    ContactsContract.Data.DATA1,
                    ContactsContract.Data.DATA2,
                    ContactsContract.Data.DATA3
                ),
                null, null, null
            )

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Log.e(
                        TAG, cursor.getString(
                            cursor
                                .getColumnIndexOrThrow(ContactsContract.Data.DATA1)
                        )
                    )
                    contactName = cursor.getString(
                        cursor
                            .getColumnIndexOrThrow(ContactsContract.Data.DATA2)
                    )
                    Log.e(TAG, contactName)
                    Log.e(
                        TAG, cursor.getString(
                            cursor
                                .getColumnIndexOrThrow(ContactsContract.Data.DATA3)
                        )
                    )
                } while (cursor.moveToNext())
                cursor.close()
            }

            tv_review.text = "Review" + " $contactName"
        }
    }
}
