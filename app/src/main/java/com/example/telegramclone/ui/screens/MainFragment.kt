package com.example.telegramclone.ui.screens

import androidx.fragment.app.Fragment
import com.example.telegramclone.R
import com.example.telegramclone.utilits.APP_ACTIVITY
import com.example.telegramclone.utilits.hideKeyboard

class MainFragment : Fragment(R.layout.fragment_chats) {

    override fun onResume() {
        super.onResume()
        APP_ACTIVITY.title = "Telegram"
        APP_ACTIVITY.appDrawer.enableDrawer()
        hideKeyboard()
    }
}