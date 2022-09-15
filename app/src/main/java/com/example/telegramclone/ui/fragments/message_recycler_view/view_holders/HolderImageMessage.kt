package com.example.telegramclone.ui.fragments.message_recycler_view.view_holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.telegramclone.database.CURRENT_UID
import com.example.telegramclone.databinding.MessageItemImageBinding
import com.example.telegramclone.ui.fragments.message_recycler_view.views.MessageView
import com.example.telegramclone.utilits.asTime
import com.example.telegramclone.utilits.downloadAndSetImage

class HolderImageMessage(
    val binding: MessageItemImageBinding
) : RecyclerView.ViewHolder(binding.root)

fun drawMessageImage(holder: HolderImageMessage, view: MessageView) {
    if (view.from == CURRENT_UID) {

        holder.binding.apply {
            blockReceivedImageMessage.visibility = View.GONE
            blockUserImageMessage.visibility = View.VISIBLE
            chatUserImage.downloadAndSetImage(view.fileUrl)
            chatUserImageMessageTime.text = view.timeStamp.asTime()
        }
    } else {
        holder.binding.apply {
            blockReceivedImageMessage.visibility = View.VISIBLE
            blockUserImageMessage.visibility = View.GONE
            chatReceivedImage.downloadAndSetImage(view.fileUrl)
            chatReceivedImageMessageTime.text = view.timeStamp.asTime()
        }
    }
}