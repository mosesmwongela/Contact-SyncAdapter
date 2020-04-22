package com.mwongela.contactsyncadapter.util

import android.content.ContentProviderOperation
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import com.mwongela.contactsyncadapter.entity.Contact

/*
    * I made use of the ContentProviderOperation to handle the table insertion operations on the
    * table to add a raw contact and the data corresponding to my app and finally to apply the
    * changes to the database as a batch operation.
 */

class ContactsManager {

    companion object {

        private const val MESSAGE_MIME_TYPE =
            "vnd.android.cursor.item/com.mwongela.contactsyncadapter.message"
        private const val REVIEW_MIME_TYPE =
            "vnd.android.cursor.item/com.mwongela.contactsyncadapter.review"

        /**
         * Method to register a contact with the app
         */
        fun registerContact(context: Context, contact: Contact) {

            for (number in contact.numbers) {
                val operations = ArrayList<ContentProviderOperation>()

                // insert account name and type
                operations.add(
                    ContentProviderOperation
                        .newInsert(
                            addCallerIsSyncAdapterParameter(
                                ContactsContract.RawContacts.CONTENT_URI,
                                true
                            )
                        )
                        .withValue(
                            ContactsContract.RawContacts.ACCOUNT_NAME,
                            Constants.ACCOUNT_NAME
                        )
                        .withValue(
                            ContactsContract.RawContacts.ACCOUNT_TYPE,
                            Constants.ACCOUNT_TYPE
                        )
                        .withValue(
                            ContactsContract.RawContacts.AGGREGATION_MODE,
                            ContactsContract.RawContacts.AGGREGATION_MODE_DEFAULT
                        )
                        .build()
                )

                // insert by phone number (because its unique)
                operations.add(
                    ContentProviderOperation
                        .newInsert(
                            addCallerIsSyncAdapterParameter(
                                ContactsContract.Data.CONTENT_URI,
                                true
                            )
                        )
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                        )
                        .withValue(
                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                            number
                        ) // Supply the number to be synced
                        .build()
                )

                /* (This will be the data retrieved when you click you app from contacts) */

                // insert your app data for messaging user
                operations.add(
                    ContentProviderOperation
                        .newInsert(
                            addCallerIsSyncAdapterParameter(
                                ContactsContract.Data.CONTENT_URI,
                                true
                            )
                        )
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, MESSAGE_MIME_TYPE)
                        .withValue(ContactsContract.Data.DATA1, number)
                        .withValue(ContactsContract.Data.DATA2, contact.name)
                        .withValue(
                            ContactsContract.Data.DATA3,
                            "Message " + getFormattedNumber(number)
                        )
                        .build()
                )


                // insert your app data for reviewing user
                operations.add(
                    ContentProviderOperation
                        .newInsert(
                            addCallerIsSyncAdapterParameter(
                                ContactsContract.Data.CONTENT_URI,
                                true
                            )
                        )
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, REVIEW_MIME_TYPE)
                        .withValue(ContactsContract.Data.DATA1, number)
                        .withValue(ContactsContract.Data.DATA2, contact.name)
                        .withValue(
                            ContactsContract.Data.DATA3,
                            "Review " + getFormattedNumber(number)
                        )
                        .build()
                )

                val contentProviderResult =
                    context.contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)

                /*
                Manually aggregate the new rawcontactid with a rawcontactid of number to be registered
                (If automatic aggregation does not work)
                 */
                val newRawContactId = ContentUris.parseId(contentProviderResult[0].uri).toString()
                Log.d(
                    ContactsManager::class.java.simpleName,
                    "New RawContactId -> $newRawContactId"
                )
                val contactRawContactId = contact.rawContactIdMap[number]
                Log.d(
                    ContactsManager::class.java.simpleName,
                    "RawContactId for number to be registered -> $contactRawContactId"
                )

                if (newRawContactId != null && contactRawContactId != null) {
                    manuallyAggregate(context, newRawContactId, contactRawContactId)
                }
            }
        }

        /**
         * Method to register a number with the app
         */
        fun registerNumber(
            context: Context,
            number: String,
            contactName: String,
            rawContactIdMap: HashMap<String, String>
        ) {
            val operations = ArrayList<ContentProviderOperation>()

            // insert account name and type
            operations.add(
                ContentProviderOperation
                    .newInsert(
                        addCallerIsSyncAdapterParameter(
                            ContactsContract.RawContacts.CONTENT_URI,
                            true
                        )
                    )
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, Constants.ACCOUNT_NAME)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, Constants.ACCOUNT_TYPE)
                    .withValue(
                        ContactsContract.RawContacts.AGGREGATION_MODE,
                        ContactsContract.RawContacts.AGGREGATION_MODE_DEFAULT
                    )
                    .build()
            )

            // insert by phone number (because its unique)
            operations.add(
                ContentProviderOperation
                    .newInsert(
                        addCallerIsSyncAdapterParameter(
                            ContactsContract.Data.CONTENT_URI,
                            true
                        )
                    )
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                    )
                    .withValue(
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        number
                    ) // Supply the number to be synced
                    .build()
            )

            /* (This will be the data retrieved when you click you app from contacts) */
            // insert your app data for message
            operations.add(
                ContentProviderOperation
                    .newInsert(
                        addCallerIsSyncAdapterParameter(
                            ContactsContract.Data.CONTENT_URI,
                            true
                        )
                    )
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, MESSAGE_MIME_TYPE)
                    .withValue(ContactsContract.Data.DATA1, number)
                    .withValue(ContactsContract.Data.DATA2, contactName)
                    .withValue(ContactsContract.Data.DATA3, "Message " + getFormattedNumber(number))
                    .build()
            )

            // insert your app data for review intent
            operations.add(
                ContentProviderOperation
                    .newInsert(
                        addCallerIsSyncAdapterParameter(
                            ContactsContract.Data.CONTENT_URI,
                            true
                        )
                    )
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, REVIEW_MIME_TYPE)
                    .withValue(ContactsContract.Data.DATA1, number)
                    .withValue(ContactsContract.Data.DATA2, contactName)
                    .withValue(ContactsContract.Data.DATA3, "Review " + getFormattedNumber(number))
                    .build()
            )

            val contentProviderResult =
                context.contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)

            /*
            Manually aggregate the new rawcontactid with a rawcontactid of number to be registered
            (If automatic aggregation does not work)
             */
            val newRawContactId = ContentUris.parseId(contentProviderResult[0].uri).toString()
            Log.d(ContactsManager::class.java.simpleName, "New RawContactId -> $newRawContactId")
            val contactRawContactId = rawContactIdMap[number]
            Log.d(
                ContactsManager::class.java.simpleName,
                "RawContactId for number to be registered -> $contactRawContactId"
            )

            if (newRawContactId != null && contactRawContactId != null) {
                manuallyAggregate(context, newRawContactId, contactRawContactId)
            }
        }

        /**
         * Method to get database number string formatted to match server string
         */
        private fun getFormattedNumber(numberString: String): String {

            var phoneNum = numberString?.replace(" ".toRegex(), "")

            if (phoneNum.startsWith("+")) {
                phoneNum = phoneNum.substring(4)
            }
            if (phoneNum.startsWith("0")) {
                phoneNum = phoneNum.substring(1)
            }
            if (phoneNum.startsWith("3")) {
                phoneNum = phoneNum.substring(2)
            }

            return "254$phoneNum"
        }

        /**
         * Method to delete RawContact of number specified
         */
        fun deleteNumber(context: Context, number: String) {
            //region Get RawContactId's for the number
            val rawContactIdCursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID),
                "${ContactsContract.CommonDataKinds.Phone.NUMBER} = ?",
                arrayOf(number), null
            )

            val rawContactIdList = ArrayList<String>()
            if (rawContactIdCursor != null && rawContactIdCursor.moveToFirst()) {
                do {
                    rawContactIdList.add(
                        rawContactIdCursor.getString(
                            rawContactIdCursor.getColumnIndexOrThrow(
                                ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID
                            )
                        )
                    )
                } while (rawContactIdCursor.moveToNext())
                rawContactIdCursor.close()
            }
            //endregion

            //region Get the RawContactId with the app's account type
            var appRawContactId = ""
            for (rawContactId in rawContactIdList) {
                val accTypeCursor = context.contentResolver.query(
                    ContactsContract.RawContacts.CONTENT_URI,
                    arrayOf(ContactsContract.RawContacts.ACCOUNT_TYPE),
                    "${ContactsContract.RawContacts._ID} = ?",
                    arrayOf(rawContactId), null
                )

                if (accTypeCursor != null && accTypeCursor.moveToFirst()) {
                    do {
                        val accountType = accTypeCursor.getString(
                            accTypeCursor.getColumnIndexOrThrow(
                                ContactsContract.RawContacts.ACCOUNT_TYPE
                            )
                        )
                        if (accountType == Constants.ACCOUNT_TYPE) {
                            appRawContactId = rawContactId
                        }
                    } while (accTypeCursor.moveToNext())
                    accTypeCursor.close()
                }

                if (appRawContactId.isNotEmpty())
                    break
            }
            //endregion

            //region Perform delete
            val operations = ArrayList<ContentProviderOperation>()

            operations.add(
                ContentProviderOperation
                    .newDelete(ContactsContract.RawContacts.CONTENT_URI)
                    .withSelection(
                        "${ContactsContract.RawContacts._ID} = ?",
                        arrayOf(appRawContactId)
                    )
                    .build()
            )

            context.contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
            //endregion
        }

        /**
         * Method to manually aggregate two raw contacts
         */
        private fun manuallyAggregate(context: Context, id1: String, id2: String) {
            val ops = ArrayList<ContentProviderOperation>()

            ops.add(
                ContentProviderOperation
                    .newUpdate(ContactsContract.AggregationExceptions.CONTENT_URI)
                    .withValue(
                        ContactsContract.AggregationExceptions.TYPE,
                        ContactsContract.AggregationExceptions.TYPE_KEEP_TOGETHER
                    )
                    .withValue(ContactsContract.AggregationExceptions.RAW_CONTACT_ID1, id1)
                    .withValue(ContactsContract.AggregationExceptions.RAW_CONTACT_ID2, id2)
                    .build()
            )

            context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
        }

        /**
         * Check for sync enabled or disabled
         */
        private fun addCallerIsSyncAdapterParameter(uri: Uri, isSyncOperation: Boolean): Uri {
            return if (isSyncOperation) {
                uri.buildUpon()
                    .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
                    .build()
            } else {
                uri
            }
        }
    }
}