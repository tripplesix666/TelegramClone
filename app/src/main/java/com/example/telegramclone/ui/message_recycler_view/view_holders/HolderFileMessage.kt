package com.example.telegramclone.ui.message_recycler_view.view_holders

import android.Manifest
import android.os.Environment
import android.view.View
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.example.telegramclone.database.CURRENT_UID
import com.example.telegramclone.database.getFileFromStorage
import com.example.telegramclone.databinding.MessageItemFileBinding
import com.example.telegramclone.ui.message_recycler_view.views.MessageView
import com.example.telegramclone.utilits.*
import java.io.File

class HolderFileMessage(
    val binding: MessageItemFileBinding
) : RecyclerView.ViewHolder(binding.root), MessageHolder {

    override fun drawMessage(view: MessageView) {
        if (view.from == CURRENT_UID) {

            binding.apply {
                blockReceivedFileMessage.visibility = View.GONE
                blockUserFileMessage.visibility = View.VISIBLE
                chatUserFileMessageTime.text = view.timeStamp.asTime()
                chatUserFilename.text = view.text
            }
        } else {
            binding.apply {
                blockReceivedFileMessage.visibility = View.VISIBLE
                blockUserFileMessage.visibility = View.GONE
                chatReceivedFileMessageTime.text = view.timeStamp.asTime()
                chatReceivedFilename.text = view.text
            }
        }
    }

    override fun onAttach(view: MessageView) {
        if (view.from == CURRENT_UID) {
            binding.chatUserBtnDownload.setOnClickListener { clickToBtnFile(view) }
        } else binding.chatReceivedBtnDownload.setOnClickListener { }
    }

    private fun clickToBtnFile(view: MessageView) {
        if (view.from == CURRENT_UID) {
            binding.chatUserBtnDownload.visibility = View.INVISIBLE
            binding.chatUserProgressBar.visibility = View.VISIBLE
        } else {
            binding.chatReceivedBtnDownload.visibility = View.INVISIBLE
            binding.chatReceivedProgressBar.visibility = View.VISIBLE
        }
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            view.text
        )

        try {

            if (checkPermission(WRITE_FILES)) {
                file.createNewFile()
                getFileFromStorage(file, view.fileUrl) {
                    if (view.from == CURRENT_UID) {
                        binding.chatUserBtnDownload.visibility = View.VISIBLE
                        binding.chatUserProgressBar.visibility = View.INVISIBLE
                    } else {
                        binding.chatReceivedBtnDownload.visibility = View.VISIBLE
                        binding.chatReceivedProgressBar.visibility = View.INVISIBLE
                    }
                }
                showToast("Скачено")
            } else {
                showToast("${checkPermission(WRITE_FILES)}")
            }
        } catch (e: java.lang.Exception) {
            showToast(e.message.toString())
        }
    }

    override fun onDetach() {
        binding.chatUserBtnDownload.setOnClickListener(null)
        binding.chatReceivedBtnDownload.setOnClickListener(null)
    }


}

