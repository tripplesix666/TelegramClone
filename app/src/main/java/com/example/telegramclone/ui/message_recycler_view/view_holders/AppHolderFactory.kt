package com.example.telegramclone.ui.message_recycler_view.view_holders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.telegramclone.databinding.MessageItemImageBinding
import com.example.telegramclone.databinding.MessageItemTextBinding
import com.example.telegramclone.databinding.MessageItemVoiceBinding
import com.example.telegramclone.ui.message_recycler_view.views.MessageView

class AppHolderFactory {

    companion object {
        fun getHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                MessageView.MESSAGE_IMAGE -> {
                    val inflater = LayoutInflater.from(parent.context)
                    val binding = MessageItemImageBinding.inflate(inflater, parent, false)
                    HolderImageMessage(binding)
                }
                MessageView.MESSAGE_VOICE -> {
                    val inflater = LayoutInflater.from(parent.context)
                    val binding = MessageItemVoiceBinding.inflate(inflater, parent, false)
                    HolderVoiceMessage(binding)
                }
                else -> {
                    val inflater = LayoutInflater.from(parent.context)
                    val binding = MessageItemTextBinding.inflate(inflater, parent, false)
                    HolderTextMessage(binding)
                }
            }
        }
    }
}