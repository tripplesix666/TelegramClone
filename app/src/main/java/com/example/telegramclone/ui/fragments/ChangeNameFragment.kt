package com.example.telegramclone.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.telegramclone.R
import com.example.telegramclone.database.USER
import com.example.telegramclone.database.setNameToDatabase
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
            setNameToDatabase(fullName)
        }
    }
}