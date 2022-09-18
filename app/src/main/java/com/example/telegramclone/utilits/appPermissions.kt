package com.example.telegramclone.utilits

import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

const val READ_CONTACTS = android.Manifest.permission.READ_CONTACTS
const val RECORD_AUDIO = android.Manifest.permission.RECORD_AUDIO
const val WRITE_FILES = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
const val READ_FILES = android.Manifest.permission.READ_EXTERNAL_STORAGE
const val MANAGE_EXTERNAL_STORAGE = android.Manifest.permission.MANAGE_EXTERNAL_STORAGE

const val PERMISSION_REQUEST = 200

fun checkPermission(permission: String) : Boolean {
    return if (Build.VERSION.SDK_INT >= 23
        && ContextCompat.checkSelfPermission(APP_ACTIVITY,  permission) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(APP_ACTIVITY, arrayOf(permission), PERMISSION_REQUEST)
        false

    } else true
}