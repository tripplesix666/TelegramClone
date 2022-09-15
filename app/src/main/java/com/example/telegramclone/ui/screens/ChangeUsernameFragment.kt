package com.example.telegramclone.ui.screens

import android.os.Bundle
import android.view.*
import com.example.telegramclone.database.*
import com.example.telegramclone.databinding.FragmentChangeUsernameBinding
import com.example.telegramclone.utilits.*


class ChangeUsernameFragment : BaseChangeFragment() {

    private lateinit var newUsername: String
    private lateinit var binding: FragmentChangeUsernameBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChangeUsernameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.settingsInputUsername.setText(USER.username)
    }

    override fun change() {
        newUsername = binding.settingsInputUsername.text.toString().lowercase()
        if (newUsername.isEmpty()) {
            showToast("Поле пустое")
        } else {
            REF_DATABASE_ROOT.child(NODE_USERNAMES)
                .addListenerForSingleValueEvent(AppValueEventListener {
                    if (it.hasChild(newUsername)) {
                        showToast("Такой пользователь уже существует")
                    } else {
                        changeUsername()
                    }
                })
        }
    }

    private fun changeUsername() {
        REF_DATABASE_ROOT.child(NODE_USERNAMES).child(newUsername).setValue(CURRENT_UID)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    updateCurrentUsername(newUsername)
                }
            }
    }

}