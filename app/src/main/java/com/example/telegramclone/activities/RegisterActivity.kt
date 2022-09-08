package com.example.telegramclone.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.telegramclone.R
import com.example.telegramclone.databinding.ActivityRegisterBinding
import com.example.telegramclone.ui.fragments.EnterPhoneNumberFragment
import com.example.telegramclone.utilits.initFirebase
import com.example.telegramclone.utilits.replaceFragment

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initFirebase()
    }

    override fun onStart() {
        super.onStart()
        toolbar = binding.registerToolbar
        setSupportActionBar(toolbar)
        title = getString(R.string.register_tittle_your_phone)
        replaceFragment(EnterPhoneNumberFragment())
    }
}