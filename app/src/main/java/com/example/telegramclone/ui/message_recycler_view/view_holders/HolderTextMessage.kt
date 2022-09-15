package com.example.telegramclone.ui.message_recycler_view.view_holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.telegramclone.database.CURRENT_UID
import com.example.telegramclone.databinding.MessageItemTextBinding
import com.example.telegramclone.ui.message_recycler_view.views.MessageView
import com.example.telegramclone.utilits.asTime

class HolderTextMessage(
    val binding: MessageItemTextBinding
) : RecyclerView.ViewHolder(binding.root), MessageHolder {
    override fun drawMessage(view: MessageView) {
        if (view.from == CURRENT_UID) {

            binding.apply {
                blockUserMessage.visibility = View.VISIBLE
                blockReceiveMessage.visibility = View.GONE
                chatUserMessage.text = view.text
                chatUserMessageTime.text = view.timeStamp.asTime()
            }
        } else {
            binding.apply {
                blockReceiveMessage.visibility = View.VISIBLE
                blockUserMessage.visibility = View.GONE
                chatReceiveMessage.text = view.text
                chatReceiveMessageTime.text = view.timeStamp.asTime()
            }
        }
    }

    override fun onAttach(view: MessageView) {
    }

    override fun onDetach() {

    }
}
