package com.example.telegramclone.database

import android.net.Uri
import com.example.telegramclone.R
import com.example.telegramclone.models.CommonModel
import com.example.telegramclone.models.UserModel
import com.example.telegramclone.utilits.APP_ACTIVITY
import com.example.telegramclone.utilits.AppValueEventListener
import com.example.telegramclone.utilits.restartActivity
import com.example.telegramclone.utilits.showToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

fun initFirebase() {
    AUTH = FirebaseAuth.getInstance()
    REF_DATABASE_ROOT = FirebaseDatabase.getInstance().reference
    USER = UserModel()
    CURRENT_UID = AUTH.currentUser?.uid.toString()
    REF_STORAGE_ROOT = FirebaseStorage.getInstance().reference
}

inline fun putUrlToDatabase(url: String, crossinline function: () -> Unit) {
    REF_DATABASE_ROOT.child(NODE_USERS)
        .child(CURRENT_UID)
        .child(CHILD_PHOTO_URL)
        .setValue(url)
        .addOnSuccessListener { function() }
        .addOnFailureListener { showToast(it.message.toString()) }
}

inline fun getUrlFromStorage(path: StorageReference, crossinline function: (url: String) -> Unit) {
    path.downloadUrl
        .addOnSuccessListener { function(it.toString()) }
        .addOnFailureListener { showToast(it.message.toString()) }
}

inline fun putFileToStorage(it: Uri, path: StorageReference, crossinline function: () -> Unit) {
    path.putFile(it)
        .addOnSuccessListener { function() }
        .addOnFailureListener { showToast(it.message.toString()) }
}

inline fun initUser(crossinline function: () -> Unit) {
    REF_DATABASE_ROOT
        .child(NODE_USERS)
        .child(CURRENT_UID)
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
        REF_DATABASE_ROOT
            .child(NODE_PHONES)
            .addListenerForSingleValueEvent(AppValueEventListener {
                it.children.forEach { dataSnapshot ->
                    arrayContacts.forEach { contact ->
                        if (dataSnapshot.key == contact.phone) {
                            REF_DATABASE_ROOT.child(NODE_PHONES_CONTACTS)
                                .child(CURRENT_UID)
                                .child(dataSnapshot.value.toString())
                                .child(CHILD_ID)
                                .setValue(dataSnapshot.value.toString())
                                .addOnFailureListener { showToast(it.message.toString()) }

                            REF_DATABASE_ROOT
                                .child(NODE_PHONES_CONTACTS)
                                .child(CURRENT_UID)
                                .child(dataSnapshot.value.toString())
                                .child(CHILD_FULL_NAME)
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

fun signIn(credential: PhoneAuthCredential, phoneNumber: String) {
    AUTH.signInWithCredential(credential)
        .addOnSuccessListener {
            val uid = AUTH.currentUser?.uid.toString()
            val dateMap = mutableMapOf<String, Any>()
            dateMap[CHILD_ID] = uid
            dateMap[CHILD_PHONE] = phoneNumber
            dateMap[CHILD_FULL_NAME] = phoneNumber

            REF_DATABASE_ROOT.child(NODE_USERS).child(uid)
                .addListenerForSingleValueEvent(AppValueEventListener {

                    if (!it.hasChild(CHILD_USERNAME)) {
                        dateMap[CHILD_USERNAME] = uid
                    }

                    REF_DATABASE_ROOT
                        .child(NODE_PHONES)
                        .child(phoneNumber)
                        .setValue(uid)
                        .addOnFailureListener { showToast(it.message.toString()) }
                        .addOnSuccessListener {
                            REF_DATABASE_ROOT
                                .child(NODE_USERS)
                                .child(uid)
                                .updateChildren(dateMap)
                                .addOnSuccessListener {
                                    showToast("Добро пожаловать")
                                    restartActivity()
                                }
                                .addOnFailureListener { showToast(it.message.toString()) }
                        }
                })
        }
        .addOnFailureListener { showToast(it.message.toString()) }
}

fun sendMessage(message: String, receivingUserId: String, typeText: String, function: () -> Unit) {
    val refDialogUser = "$NODE_MESSAGES/$CURRENT_UID/$receivingUserId"
    val refDialogReceivingUser = "$NODE_MESSAGES/$receivingUserId/$CURRENT_UID"
    val messageKey = REF_DATABASE_ROOT.child(refDialogUser).push().key

    val mapMessage = hashMapOf<String, Any>()
    mapMessage[CHILD_FROM] = CURRENT_UID
    mapMessage[CHILD_ID] = messageKey.toString()
    mapMessage[CHILD_TYPE] = typeText
    mapMessage[CHILD_TEXT] = message
    mapMessage[CHILD_TIMESTAMP] = ServerValue.TIMESTAMP

    val mapDialog = hashMapOf<String, Any>()
    mapDialog["$refDialogUser/$messageKey"] = mapMessage
    mapDialog["$refDialogReceivingUser/$messageKey"] = mapMessage

    REF_DATABASE_ROOT
        .updateChildren(mapDialog)
        .addOnSuccessListener { function() }
        .addOnFailureListener { showToast(it.message.toString()) }
}

fun sendMessageAsFile(
    receivingUserId: String,
    fileUrl: String,
    messageKey: String,
    typeMessage: String,
    fileName: String = ""
) {
    val refDialogUser = "$NODE_MESSAGES/$CURRENT_UID/$receivingUserId"
    val refDialogReceivingUser = "$NODE_MESSAGES/$receivingUserId/$CURRENT_UID"

    val mapMessage = hashMapOf<String, Any>()
    mapMessage[CHILD_FROM] = CURRENT_UID
    mapMessage[CHILD_ID] = messageKey
    mapMessage[CHILD_TYPE] = typeMessage
    mapMessage[CHILD_TIMESTAMP] = ServerValue.TIMESTAMP
    mapMessage[CHILD_FILE_URL] = fileUrl
    mapMessage[CHILD_TEXT] = fileName

    val mapDialog = hashMapOf<String, Any>()
    mapDialog["$refDialogUser/$messageKey"] = mapMessage
    mapDialog["$refDialogReceivingUser/$messageKey"] = mapMessage

    REF_DATABASE_ROOT
        .updateChildren(mapDialog)
        .addOnFailureListener { showToast(it.message.toString()) }
}

fun updateCurrentUsername(newUsername: String) {
    REF_DATABASE_ROOT
        .child(NODE_USERS)
        .child(CURRENT_UID)
        .child(CHILD_USERNAME)
        .setValue(newUsername)
        .addOnSuccessListener {
            showToast(APP_ACTIVITY.getString(R.string.toast_data_update))
            deleteOldUsername(newUsername)
        }
        .addOnFailureListener { showToast(it.message.toString()) }
}

private fun deleteOldUsername(newUsername: String) {
    REF_DATABASE_ROOT.child(NODE_USERNAMES).child(USER.username).removeValue()
        .addOnSuccessListener {
            showToast(APP_ACTIVITY.getString(R.string.toast_data_update))
            APP_ACTIVITY.supportFragmentManager.popBackStack()
            USER.username = newUsername
        }
        .addOnFailureListener { showToast(it.message.toString()) }
}

fun setBioToDataBase(newBio: String) {
    REF_DATABASE_ROOT
        .child(NODE_USERS)
        .child(CURRENT_UID)
        .child(CHILD_BIO)
        .setValue(newBio)
        .addOnSuccessListener {
            showToast(APP_ACTIVITY.getString(R.string.toast_data_update))
            USER.bio = newBio
            APP_ACTIVITY.supportFragmentManager.popBackStack()
        }
        .addOnFailureListener { showToast(it.message.toString()) }

}

fun setNameToDatabase(fullName: String) {
    REF_DATABASE_ROOT
        .child(NODE_USERS)
        .child(CURRENT_UID)
        .child(CHILD_FULL_NAME)
        .setValue(fullName)
        .addOnSuccessListener {
            showToast(APP_ACTIVITY.getString(R.string.toast_data_update))
            USER.full_name = fullName
            APP_ACTIVITY.appDrawer.updateHeader()
            APP_ACTIVITY.supportFragmentManager.popBackStack()
        }
        .addOnFailureListener { showToast(it.message.toString()) }
}

fun getMessageKey(id: String) = REF_DATABASE_ROOT
    .child(NODE_MESSAGES)
    .child(CURRENT_UID)
    .child(id)
    .push().key.toString()

fun uploadFileToStorage(uri: Uri, messageKey: String, receivedId: String, typeMessage: String, fileName: String = "") {
    val path = REF_STORAGE_ROOT
        .child(FOLDER_FILES)
        .child(messageKey)
    putFileToStorage(uri, path) {
        getUrlFromStorage(path) { url ->
            sendMessageAsFile(receivedId, url, messageKey, typeMessage, fileName)
        }
    }
}

fun getFileFromStorage(file: File, fileUrl: String, function: () -> Unit) {
    val path = REF_STORAGE_ROOT.storage.getReferenceFromUrl(fileUrl)
    path.getFile(file)
        .addOnSuccessListener { function() }
        .addOnFailureListener { showToast(it.message.toString()) }
}