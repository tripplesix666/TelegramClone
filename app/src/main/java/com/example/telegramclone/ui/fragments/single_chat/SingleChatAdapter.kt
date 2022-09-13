package com.example.telegramclone.ui.fragments.single_chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.telegramclone.R
import com.example.telegramclone.database.CURRENT_UID
import com.example.telegramclone.models.CommonModel
import com.example.telegramclone.utilits.TYPE_MESSAGE_IMAGE
import com.example.telegramclone.utilits.TYPE_MESSAGE_TEXT
import com.example.telegramclone.utilits.asTime
import com.example.telegramclone.utilits.downloadAndSetImage
import kotlinx.android.synthetic.main.message_item.view.*

class SingleChatAdapter : RecyclerView.Adapter<SingleChatAdapter.SingleChatHolder>() {

    private var listMessagesCache = mutableListOf<CommonModel>()

    class SingleChatHolder(view: View) : RecyclerView.ViewHolder(view) {
        val blockUserMessage: ConstraintLayout = view.block_user_message
        val chatUserMessage: TextView = view.chat_user_message
        val chatUserMessageTime: TextView = view.chat_user_message_time

        val blockReceiveMessage: ConstraintLayout = view.block_receive_message
        val chatReceiveMessage: TextView = view.chat_receive_message
        val chatReceiveMessageTime: TextView = view._chat_receive_message_time

        val blockReceivedImageMessage: ConstraintLayout = view.block_receive_image_message
        val blockUserImageMessage: ConstraintLayout = view.block_user_image_message
        val chatUserImage: ImageView = view.chat_user_image
        val chatReceivedImage: ImageView = view.chat_receive_image
        val chatUserImageMessageTime: TextView = view.chat_user_message_time
        val chatReceivedImageMessageTime: TextView = view._chat_receive_image_message_time


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleChatHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.message_item, parent, false)
        return SingleChatHolder(view)
    }

    override fun onBindViewHolder(holder: SingleChatHolder, position: Int) {
        when (listMessagesCache[position].type) {
             TYPE_MESSAGE_TEXT -> drawMessageText(holder, position)
            TYPE_MESSAGE_IMAGE -> drawMessageImage(holder, position)
        }

    }

    private fun drawMessageImage(holder: SingleChatHolder, position: Int) {
        holder.blockUserMessage.visibility = View.GONE
        holder.blockReceiveMessage.visibility = View.GONE

        if (listMessagesCache[position].from == CURRENT_UID) {
            holder.blockReceivedImageMessage.visibility = View.GONE
            holder.blockUserImageMessage.visibility = View.VISIBLE
            holder.chatUserImage.downloadAndSetImage(listMessagesCache[position].fileUrl)
            holder.chatUserImageMessageTime.text = listMessagesCache[position].timeStamp.toString().asTime()
        } else {
            holder.blockReceivedImageMessage.visibility = View.VISIBLE
            holder.blockUserImageMessage.visibility = View.GONE
            holder.chatReceivedImage.downloadAndSetImage(listMessagesCache[position].fileUrl)
            holder.chatReceivedImageMessageTime.text = listMessagesCache[position].timeStamp.toString().asTime()
        }
    }

    private fun drawMessageText(holder: SingleChatHolder, position: Int) {
        holder.blockReceivedImageMessage.visibility = View.GONE
        holder.blockUserImageMessage.visibility = View.GONE

        if (listMessagesCache[position].from == CURRENT_UID) {
            holder.blockUserMessage.visibility = View.VISIBLE
            holder.blockReceiveMessage.visibility = View.GONE
            holder.chatUserMessage.text = listMessagesCache[position].text
            holder.chatUserMessageTime.text =
                listMessagesCache[position].timeStamp.toString().asTime()
        } else {
            holder.blockReceiveMessage.visibility = View.VISIBLE
            holder.blockUserMessage.visibility = View.GONE
            holder.chatReceiveMessage.text = listMessagesCache[position].text
            holder.chatReceiveMessageTime.text =
                listMessagesCache[position].timeStamp.toString().asTime()
        }
    }

    override fun getItemCount(): Int = listMessagesCache.size


    fun addItemToBottom(
        item: CommonModel,
        onSuccess: () -> Unit
    ) {
        if (!listMessagesCache.contains(item)) {
            listMessagesCache.add(item)
            notifyItemInserted(listMessagesCache.size)
        }
        onSuccess()
    }

    fun addItemToTop(
        item: CommonModel,
        onSuccess: () -> Unit
    ) {
        if (!listMessagesCache.contains(item)) {
            listMessagesCache.add(item)
            listMessagesCache.sortBy { it.timeStamp.toString() }
            notifyItemInserted(0)
        }
        onSuccess()
    }

}


