package com.example.telegramclone.ui.fragments.single_chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.telegramclone.R
import com.example.telegramclone.database.CURRENT_UID
import com.example.telegramclone.models.CommonModel
import com.example.telegramclone.utilits.asTime
import kotlinx.android.synthetic.main.message_item.view.*

class SingleChatAdapter : RecyclerView.Adapter<SingleChatAdapter.SingleChatHolder>() {

    private var listMessagesCache = mutableListOf<CommonModel>()
    private lateinit var diffResult: DiffUtil.DiffResult

    class SingleChatHolder(view: View) : RecyclerView.ViewHolder(view) {
        val blockUserMessage: ConstraintLayout = view.block_user_message
        val chatUserMessage: TextView = view.chat_user_message
        val chatUserMessageTime: TextView = view.chat_user_message_time

        val blockReceiveMessage: ConstraintLayout = view.block_receive_message
        val chatReceiveMessage: TextView = view.chat_receive_message
        val chatReceiveMessageTime: TextView = view._chat_receive_message_time


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleChatHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.message_item, parent, false)
        return SingleChatHolder(view)
    }

    override fun onBindViewHolder(holder: SingleChatHolder, position: Int) {
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

    fun addItem(
        item: CommonModel,
        toBottom: Boolean,
        onSuccess: () -> Unit
    ) {
        if (toBottom) {
            if (!listMessagesCache.contains(item)) {
                listMessagesCache.add(item)
                notifyItemInserted(listMessagesCache.size)
            }
        } else {
            if (!listMessagesCache.contains(item)) {
                listMessagesCache.add(item)
                listMessagesCache.sortBy { it.timeStamp.toString() }
                notifyItemInserted(0)
            }
        }
        onSuccess()
    }
}


