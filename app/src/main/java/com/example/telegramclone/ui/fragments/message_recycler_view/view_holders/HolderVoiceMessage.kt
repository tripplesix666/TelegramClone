package com.example.telegramclone.ui.fragments.message_recycler_view.view_holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.telegramclone.database.CURRENT_UID
import com.example.telegramclone.databinding.MessageItemImageBinding
import com.example.telegramclone.databinding.MessageItemVoiceBinding
import com.example.telegramclone.ui.fragments.message_recycler_view.views.MessageView
import com.example.telegramclone.utilits.asTime

class HolderVoiceMessage(
    val binding: MessageItemVoiceBinding
) : RecyclerView.ViewHolder(binding.root)

fun drawMessageVoice(holder: HolderVoiceMessage, view: MessageView) {
    if (view.from == CURRENT_UID) {

        holder.binding.apply {
            blockReceivedVoiceMessage.visibility = View.GONE
            blockUserVoiceMessage.visibility = View.VISIBLE
            chatUserVoiceMessageTime.text = view.timeStamp.asTime()
        }
    } else {
        holder.binding.apply {
            blockReceivedVoiceMessage.visibility = View.VISIBLE
            blockUserVoiceMessage.visibility = View.GONE
            chatReceivedVoiceMessageTime.text = view.timeStamp.asTime()
        }
    }
}