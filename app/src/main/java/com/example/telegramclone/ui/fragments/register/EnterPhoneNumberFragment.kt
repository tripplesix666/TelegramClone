package com.example.telegramclone.ui.fragments.register

import androidx.fragment.app.Fragment
import com.example.telegramclone.R
import com.example.telegramclone.database.AUTH
import com.example.telegramclone.database.signIn
import com.example.telegramclone.utilits.*
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.fragment_enter_phone_number.*
import java.util.concurrent.TimeUnit

class EnterPhoneNumberFragment : Fragment(R.layout.fragment_enter_phone_number) {

    private lateinit var phoneNumber: String
    private lateinit var callBacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onStart() {
        super.onStart()
        callBacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signIn(credential, phoneNumber)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                showToast(e.message.toString())
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                replaceFragment(EnterCodeFragment(phoneNumber, verificationId))
            }
        }
        register_btn_next.setOnClickListener { sendCode() }
    }

    private fun sendCode() {
        if (register_input_phone_number.text.toString().isEmpty()) {
            showToast(getString(R.string.register_toast_enter_phone))
        } else {
            authUser()
        }
    }

    private fun authUser() {
        phoneNumber = register_input_phone_number.text.toString()
        val option = PhoneAuthOptions.newBuilder(AUTH)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(APP_ACTIVITY)
            .setCallbacks(callBacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(option)
    }
}