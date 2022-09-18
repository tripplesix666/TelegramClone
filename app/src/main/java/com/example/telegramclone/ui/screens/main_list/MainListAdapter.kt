package com.example.telegramclone.ui.screens.main_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.telegramclone.databinding.MainListItemBinding
import com.example.telegramclone.models.CommonModel
import com.example.telegramclone.utilits.downloadAndSetImage

class MainListAdapter : RecyclerView.Adapter<MainListAdapter.MainListHolder>() {

    private var listItems = mutableListOf<CommonModel>()

    class MainListHolder(
        val binding: MainListItemBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainListHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = MainListItemBinding.inflate(inflater, parent, false)
        return MainListHolder(binding)
    }

    override fun onBindViewHolder(holder: MainListHolder, position: Int) {
        holder.binding.mainListItemName.text = listItems[position].full_name
        holder.binding.mainListLastMessage.text = listItems[position].lastMessage
        holder.binding.mainListItemPhoto.downloadAndSetImage(listItems[position].photoUrl)
    }

    override fun getItemCount(): Int = listItems.size

    fun updateListItems(item: CommonModel) {
        listItems.add(item)
        notifyItemInserted(listItems.size)
    }
}