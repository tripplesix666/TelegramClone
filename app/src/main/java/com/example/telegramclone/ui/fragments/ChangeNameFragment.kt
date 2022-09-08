package com.example.telegramclone.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.telegramclone.R
import com.example.telegramclone.databinding.FragmentChangeNameBinding
import com.example.telegramclone.utilits.*


class ChangeNameFragment : BaseChangeFragment() {

    private lateinit var binding: FragmentChangeNameBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChangeNameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        initFullNameList()
    }

    private fun initFullNameList() {
        val fullNameList = USER.full_name.split(" ")
        if (fullNameList.size > 1) {
            binding.settingsInputName.setText(fullNameList[0])
            binding.settingsInputSurname.setText(fullNameList[1])
        } else {
            binding.settingsInputName.setText(fullNameList[0])
        }
    }

    override fun change() {
        val name = binding.settingsInputName.text.toString()
        val surname = binding.settingsInputSurname.text.toString()

        if (name.isEmpty()) {
            showToast(getString(R.string.settings_toast_enter_name))
        } else {
            val fullName = "$name $surname"
            REF_DATABASE_ROOT.child(NODE_USER).child(CURRENT_UID).child(CHILD_FULL_NAME)
                .setValue(fullName).addOnCompleteListener {
                    if (it.isSuccessful) {
                        showToast(getString(R.string.toast_data_update))
                        USER.full_name = fullName
                        APP_ACTIVITY.appDrawer.updateHeader()
                        fragmentManager?.popBackStack()
                    }
                }
        }
    }


}