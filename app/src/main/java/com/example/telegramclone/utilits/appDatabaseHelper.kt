package com.example.telegramclone.utilits

import android.annotation.SuppressLint
import android.net.Uri
import android.provider.ContactsContract
import com.example.telegramclone.MainActivity
import com.example.telegramclone.activities.RegisterActivity
import com.example.telegramclone.models.CommonModel
import com.example.telegramclone.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.ArrayList

lateinit var AUTH: FirebaseAuth
lateinit var CURRENT_UID: String
lateinit var REF_DATABASE_ROOT: DatabaseReference
lateinit var REF_STORAGE_ROOT: StorageReference
lateinit var USER: User

const val NODE_USER = "users"
const val NODE_USERNAMES = "usernames"
const val NODE_PHONES = "phones"
const val NODE_PHONES_CONTACTS = "phones_contacts"

const val FOLDER_PROFILE_IMAGE = "profile_image"

const val CHILD_ID = "id"
const val CHILD_PHONE = "phone"
const val CHILD_USERNAME = "username"
const val CHILD_FULL_NAME = "full_name"
const val CHILD_BIO = "bio"
const val CHILD_PHOTO_URL = "photoUrl"
const val CHILD_STATE = "state"

fun initFirebase() {
    AUTH = FirebaseAuth.getInstance()
    REF_DATABASE_ROOT = FirebaseDatabase.getInstance().reference
    USER = User()
    CURRENT_UID = AUTH.currentUser?.uid.toString()
    REF_STORAGE_ROOT = FirebaseStorage.getInstance().reference
}

inline fun putUrlToDatabase(url: String, crossinline function: () -> Unit) {
    REF_DATABASE_ROOT.child(NODE_USER).child(CURRENT_UID).child(CHILD_PHOTO_URL)
        .setValue(url)
        .addOnSuccessListener { function() }
        .addOnFailureListener { showToast(it.message.toString()) }
}

inline fun getUrlFromStorage(path: StorageReference, crossinline function: (url: String) -> Unit) {
    path.downloadUrl
        .addOnSuccessListener { function(it.toString()) }
        .addOnFailureListener { showToast(it.message.toString()) }
}

inline fun putImageToStorage(it: Uri, path: StorageReference, crossinline function: () -> Unit) {
    path.putFile(it)
        .addOnSuccessListener { function() }
        .addOnFailureListener { showToast(it.message.toString()) }
}

inline fun initUser(crossinline function: () -> Unit) {
    REF_DATABASE_ROOT.child(NODE_USER).child(CURRENT_UID)
        .addListenerForSingleValueEvent(AppValueEventListener {
            USER = it.getValue(User::class.java) ?: User()
            if (USER.username.isEmpty()) {
                USER.username = CURRENT_UID
            }
            function()
        })
}


@SuppressLint("Range")
fun initContacts() {
    if (checkPermission(READ_CONTACTS)) {
        val arrayContacts = arrayListOf<CommonModel>()
        val cursor = APP_ACTIVITY.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        cursor?.let {
            while (it.moveToNext()) {
                val fullName = it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val phone = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val newModel = CommonModel()
                newModel.full_name = fullName
                newModel.phone = phone.replace(Regex("[\\s,-]"), "")
                arrayContacts.add(newModel)
            }
        }
        cursor?.close()
        updatePhonesToDatabase(arrayContacts)
    }
}

fun updatePhonesToDatabase(arrayContacts: ArrayList<CommonModel>) {
    REF_DATABASE_ROOT.child(NODE_PHONES).addListenerForSingleValueEvent(AppValueEventListener{
        it.children.forEach { dataSnapshot ->
            arrayContacts.forEach { contact ->
                if (dataSnapshot.key == contact.phone) {
                    REF_DATABASE_ROOT.child(NODE_PHONES_CONTACTS).child(CURRENT_UID)
                        .child(dataSnapshot.value.toString()).child(CHILD_ID)
                        .setValue(dataSnapshot.value.toString())
                        .addOnFailureListener { showToast(it.message.toString()) }
                }
            }
        }
    })
}

fun DataSnapshot.getCommonModel(): CommonModel =
    this.getValue(CommonModel::class.java) ?: CommonModel()

fun signInForNewUsers(credential: PhoneAuthCredential, phoneNumber: String) {
    AUTH.signInWithCredential(credential).addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val uid = AUTH.currentUser?.uid.toString()
            val dateMap = mutableMapOf<String, Any>()
            dateMap[CHILD_ID] = uid
            dateMap[CHILD_PHONE] = phoneNumber
            dateMap[CHILD_USERNAME] = uid
            dateMap[CHILD_FULL_NAME] = phoneNumber

            REF_DATABASE_ROOT.child(NODE_PHONES).child(phoneNumber).setValue(uid)
                .addOnFailureListener { showToast(it.message.toString()) }
                .addOnSuccessListener {
                    REF_DATABASE_ROOT.child(NODE_USER).child(uid).updateChildren(dateMap)
                        .addOnSuccessListener {
                            showToast("Добро пожаловать")
                            REG_ACTIVITY.replaceActivity(MainActivity())
                        }
                        .addOnFailureListener { showToast(it.message.toString()) }
                }
        } else {
            showToast(task.exception?.message.toString())
        }
    }
}

fun signInForOldUsers(credential: PhoneAuthCredential) {
    AUTH.signInWithCredential(credential).addOnCompleteListener { task ->
        if (task.isSuccessful) {
            showToast("Добро пожаловать снова")
            REG_ACTIVITY.replaceActivity(MainActivity())
        } else {
            showToast(task.exception?.message.toString())
        }
    }
}

fun signIn(credential: PhoneAuthCredential, phoneNumber: String) {
    val listOfPhones = mutableListOf<String>()
    REF_DATABASE_ROOT.child(NODE_PHONES)
        .addListenerForSingleValueEvent(AppValueEventListener {
            it.children.forEach { snapshot ->
                listOfPhones.add(snapshot.key.toString())
            }
            if (listOfPhones.contains(phoneNumber)) {
                signInForOldUsers(credential)
            } else {
                signInForNewUsers(credential, phoneNumber)
            }
        })
}

