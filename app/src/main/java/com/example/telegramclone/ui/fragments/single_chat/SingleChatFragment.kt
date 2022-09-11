package com.example.telegramclone.ui.fragments.single_chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.telegramclone.database.*
import com.example.telegramclone.databinding.FragmentSingleChatBinding
import com.example.telegramclone.models.CommonModel
import com.example.telegramclone.models.UserModel
import com.example.telegramclone.ui.fragments.BaseFragment
import com.example.telegramclone.utilits.*
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.toolbar_info.view.*

class SingleChatFragment(private val contact: CommonModel) : BaseFragment() {

    private lateinit var listenerInfoToolbar: AppValueEventListener
    private lateinit var receivingUser: UserModel
    private lateinit var toolbarInfo: View
    private lateinit var binding: FragmentSingleChatBinding
    private lateinit var refUser: DatabaseReference
    private lateinit var refMessage: DatabaseReference
    private lateinit var adapter: SingleChatAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageListener: AppValueEventListener
    private var listMessages = emptyList<CommonModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSingleChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        toolbarInfo = APP_ACTIVITY.toolbar.toolbar_info
        toolbarInfo.visibility = View.VISIBLE
        initToolbar()
        initRecyclerView()
    }

    private fun initRecyclerView() {
        recyclerView = binding.chatRecyclerView
        adapter = SingleChatAdapter()
        refMessage = REF_DATABASE_ROOT
            .child(NODE_MESSAGES)
            .child(CURRENT_UID)
            .child(contact.id)
        recyclerView.adapter = adapter
        messageListener = AppValueEventListener { dataSnapshot ->
            listMessages = dataSnapshot.children.map { it -> it.getCommonModel() }
            adapter.setList(listMessages)
            recyclerView.smoothScrollToPosition(adapter.itemCount)
        }
        refMessage.addValueEventListener(messageListener)
    }

    private fun initToolbar() {
        listenerInfoToolbar = AppValueEventListener {
            receivingUser = it.getUserModel()
            initInfoToolbar()
        }
        refUser = REF_DATABASE_ROOT.child(NODE_USER).child(contact.id)
        refUser.addValueEventListener(listenerInfoToolbar)
        binding.chatBtnSendMessage.setOnClickListener {
            val message = binding.chatInputMessage.text.toString()
            if (message.isEmpty()) {
                showToast("Нечего отправлять")
            } else {
                sendMessage(message, contact.id, TYPE_TEXT) {
                    binding.chatInputMessage.setText("")
                }
            }
        }
    }

    private fun initInfoToolbar() {
        if (receivingUser.full_name == receivingUser.phone) {
            toolbarInfo.toolbar_chat_full_name.text = contact.full_name
        } else {
            toolbarInfo.toolbar_chat_full_name.text = receivingUser.full_name
        }
        toolbarInfo.toolbar_chat_image.downloadAndSetImage(receivingUser.photoUrl)
        toolbarInfo.toolbar_chat_status.text = receivingUser.state
    }

    override fun onPause() {
        super.onPause()
        toolbarInfo.visibility = View.GONE
        refUser.removeEventListener(listenerInfoToolbar)
        refMessage.removeEventListener(messageListener)
    }


}