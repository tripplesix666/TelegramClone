package com.example.telegramclone.ui.screens

import androidx.fragment.app.Fragment
import com.example.telegramclone.utilits.APP_ACTIVITY

open class BaseFragment() : Fragment() {

    override fun onStart() {
        super.onStart()
        APP_ACTIVITY.appDrawer.disableDrawer()
    }
}