package com.example.telegramclone.ui.fragments.single_chat

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.telegramclone.database.CURRENT_UID
import com.example.telegramclone.ui.fragments.message_recycler_view.view_holders.AppHolderFactory
import com.example.telegramclone.ui.fragments.message_recycler_view.view_holders.HolderImageMessage
import com.example.telegramclone.ui.fragments.message_recycler_view.view_holders.HolderTextMessage
import com.example.telegramclone.ui.fragments.message_recycler_view.views.MessageView
import com.example.telegramclone.utilits.asTime
import com.example.telegramclone.utilits.downloadAndSetImage

class SingleChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var listMessagesCache = mutableListOf<MessageView>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return AppHolderFactory.getHolder(parent, viewType)
    }

    override fun getItemViewType(position: Int): Int {
        return listMessagesCache[position].getTypeView()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HolderImageMessage -> drawMessageImage(holder, position)
            is HolderTextMessage -> drawMessageText(holder, position)
            else -> {}
        }

    }

    private fun drawMessageImage(holder: HolderImageMessage, position: Int) {
        if (listMessagesCache[position].from == CURRENT_UID) {

            holder.binding.apply {
                blockReceivedImageMessage.visibility = View.GONE
                blockUserImageMessage.visibility = View.VISIBLE
                chatUserImage.downloadAndSetImage(listMessagesCache[position].fileUrl)
                chatUserImageMessageTime.text = listMessagesCache[position].timeStamp.asTime()
            }
        } else {
            holder.binding.apply {
                blockReceivedImageMessage.visibility = View.VISIBLE
                blockUserImageMessage.visibility = View.GONE
                chatReceivedImage.downloadAndSetImage(listMessagesCache[position].fileUrl)
                chatReceivedImageMessageTime.text = listMessagesCache[position].timeStamp.asTime()
            }
        }
    }

    private fun drawMessageText(holder: HolderTextMessage, position: Int) {
        if (listMessagesCache[position].from == CURRENT_UID) {

            holder.binding.apply {
                blockUserMessage.visibility = View.VISIBLE
                blockReceiveMessage.visibility = View.GONE
                chatUserMessage.text = listMessagesCache[position].text
                chatUserMessageTime.text = listMessagesCache[position].timeStamp.asTime()
            }
        } else {
            holder.binding.apply {
                blockReceiveMessage.visibility = View.VISIBLE
                blockUserMessage.visibility = View.GONE
                chatReceiveMessage.text = listMessagesCache[position].text
                chatReceiveMessageTime.text = listMessagesCache[position].timeStamp.asTime()
            }
        }
    }

    override fun getItemCount(): Int = listMessagesCache.size

    fun addItemToBottom(
        item: MessageView,
        onSuccess: () -> Unit
    ) {
        if (!listMessagesCache.contains(item)) {
            listMessagesCache.add(item)
            notifyItemInserted(listMessagesCache.size)
        }
        onSuccess()
    }

    fun addItemToTop(
        item: MessageView,
        onSuccess: () -> Unit
    ) {
        if (!listMessagesCache.contains(item)) {
            listMessagesCache.add(item)
            listMessagesCache.sortBy { it.timeStamp }
            notifyItemInserted(0)
        }
        onSuccess()
    }

}


