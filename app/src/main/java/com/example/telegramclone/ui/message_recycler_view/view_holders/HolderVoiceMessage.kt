package com.example.telegramclone.ui.message_recycler_view.view_holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.telegramclone.database.CURRENT_UID
import com.example.telegramclone.databinding.MessageItemVoiceBinding
import com.example.telegramclone.ui.message_recycler_view.views.MessageView
import com.example.telegramclone.utilits.AppVoicePlayer
import com.example.telegramclone.utilits.asTime

class HolderVoiceMessage(
    val binding: MessageItemVoiceBinding
) : RecyclerView.ViewHolder(binding.root), MessageHolder {

    private val appVoicePlayer = AppVoicePlayer()

    override fun drawMessage(view: MessageView) {
        if (view.from == CURRENT_UID) {

            binding.apply {
                blockReceivedVoiceMessage.visibility = View.GONE
                blockUserVoiceMessage.visibility = View.VISIBLE
                chatUserVoiceMessageTime.text = view.timeStamp.asTime()
            }
        } else {
            binding.apply {
                blockReceivedVoiceMessage.visibility = View.VISIBLE
                blockUserVoiceMessage.visibility = View.GONE
                chatReceivedVoiceMessageTime.text = view.timeStamp.asTime()
            }
        }
    }

    override fun onAttach(view: MessageView) {
        appVoicePlayer.init()
        if (view.from == CURRENT_UID) {
            binding.chatUserBtnPlay.setOnClickListener {
                binding.chatUserBtnPlay.visibility = View.GONE
                binding.chatUserBtnStop.visibility = View.VISIBLE
                binding.chatUserBtnStop.setOnClickListener {
                    stop {
                        binding.chatUserBtnStop.setOnClickListener(null)
                        binding.chatUserBtnPlay.visibility = View.VISIBLE
                        binding.chatUserBtnStop.visibility = View.GONE
                    }
                }
                play(view) {
                    binding.chatUserBtnPlay.visibility = View.VISIBLE
                    binding.chatUserBtnStop.visibility = View.GONE
                }
            }
        } else {
            binding.chatReceivedBtnPlay.setOnClickListener {
                binding.chatReceivedBtnPlay.visibility = View.GONE
                binding.chatReceivedBtnStop.visibility = View.VISIBLE
                binding.chatReceivedBtnStop.setOnClickListener {
                    stop {
                        binding.chatReceivedBtnStop.setOnClickListener(null)
                        binding.chatReceivedBtnPlay.visibility = View.VISIBLE
                        binding.chatReceivedBtnStop.visibility = View.GONE
                    }
                }
                play(view) {
                    binding.chatReceivedBtnPlay.visibility = View.VISIBLE
                    binding.chatReceivedBtnStop.visibility = View.GONE
                }
            }
        }
    }

    private fun stop(function: () -> Unit) {
        appVoicePlayer.stop { function() }
    }

    private fun play(view: MessageView, function: () -> Unit) {
        appVoicePlayer.play(view.id, view.fileUrl) {
            function()
        }
    }

    override fun onDetach() {
        binding.chatUserBtnPlay.setOnClickListener(null)
        binding.chatReceivedBtnPlay.setOnClickListener(null)
        appVoicePlayer.release()
    }
}

