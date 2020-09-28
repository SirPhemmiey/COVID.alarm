package de.sladkin.corona.repository

import com.google.firebase.database.FirebaseDatabase
import de.sladkin.corona.model.LatLng

class DatabaseRepository(
    private val database: FirebaseDatabase,
    private val sharedPrefsUtil: SharedPrefsUtil
) {

    fun writeLocation(location: LatLng) {
        database.getReference("users").apply {
            sharedPrefsUtil.getUserId()?.let {
                child(it).setValue(location)
            } ?: push().key?.let {
                child(it).setValue(location)
                sharedPrefsUtil.writeUserId(it)
            }
        }
    }

}