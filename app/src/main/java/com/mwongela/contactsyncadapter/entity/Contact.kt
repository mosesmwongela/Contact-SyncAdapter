package com.mwongela.contactsyncadapter.entity

data class Contact(
    val id: String,
    val name: String,
    val numbers: ArrayList<String>,
    val rawContactIdMap: HashMap<String, String>
)