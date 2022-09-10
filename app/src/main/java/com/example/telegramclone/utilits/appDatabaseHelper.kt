package com.example.telegramclone.utilits

import android.net.Uri
import com.example.telegramclone.MainActivity
import com.example.telegramclone.models.CommonModel
import com.example.telegramclone.models.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

lateinit var AUTH: FirebaseAuth
lateinit var CURRENT_UID: String
lateinit var REF_DATABASE_ROOT: DatabaseReference
lateinit var REF_STORAGE_ROOT: StorageReference
lateinit var USER: UserModel

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
    USER = UserModel()
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
            USER = it.getValue(UserModel::class.java) ?: UserModel()
            if (USER.username.isEmpty()) {
                USER.username = CURRENT_UID
            }
            function()
        })
}

fun updatePhonesToDatabase(arrayContacts: ArrayList<CommonModel>) {
    if (AUTH.currentUser != null) {
        REF_DATABASE_ROOT.child(NODE_PHONES).addListenerForSingleValueEvent(AppValueEventListener {
            it.children.forEach { dataSnapshot ->
                arrayContacts.forEach { contact ->
                    if (dataSnapshot.key == contact.phone) {
                        REF_DATABASE_ROOT.child(NODE_PHONES_CONTACTS).child(CURRENT_UID)
                            .child(dataSnapshot.value.toString()).child(CHILD_ID)
                            .setValue(dataSnapshot.value.toString())
                            .addOnFailureListener { showToast(it.message.toString()) }

                        REF_DATABASE_ROOT.child(NODE_PHONES_CONTACTS).child(CURRENT_UID)
                            .child(dataSnapshot.value.toString()).child(CHILD_FULL_NAME)
                            .setValue(contact.full_name)
                            .addOnFailureListener { showToast(it.message.toString()) }
                    }
                }
            }
        })
    }
}

fun DataSnapshot.getCommonModel(): CommonModel =
    this.getValue(CommonModel::class.java) ?: CommonModel()

fun DataSnapshot.getUserModel(): UserModel =
    this.getValue(UserModel::class.java) ?: UserModel()

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

