package com.example.telegramclone.ui.screens

import android.view.*
import androidx.fragment.app.Fragment
import com.example.telegramclone.R
import com.example.telegramclone.utilits.APP_ACTIVITY
import com.example.telegramclone.utilits.hideKeyboard

open class BaseChangeFragment : Fragment() {

    override fun onStart() {
        super.onStart()
        setHasOptionsMenu(true)
        APP_ACTIVITY.appDrawer.disableDrawer()
        hideKeyboard()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        APP_ACTIVITY.menuInflater.inflate(R.menu.settings_menu_confirm, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings_confirm_change -> change()
        }
        return true

    }

    open fun change() {
    }
}