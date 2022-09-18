package com.example.telegramclone.ui.screens.single_chat

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.example.telegramclone.R
import com.example.telegramclone.database.*
import com.example.telegramclone.databinding.FragmentSingleChatBinding
import com.example.telegramclone.models.CommonModel
import com.example.telegramclone.models.UserModel
import com.example.telegramclone.ui.message_recycler_view.views.AppViewFactory
import com.example.telegramclone.ui.screens.BaseFragment
import com.example.telegramclone.utilits.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.choice_upload.*
import kotlinx.android.synthetic.main.toolbar_info.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SingleChatFragment(private val contact: CommonModel) : BaseFragment() {

    private lateinit var listenerInfoToolbar: AppValueEventListener
    private lateinit var receivingUser: UserModel
    private lateinit var toolbarInfo: View
    private lateinit var binding: FragmentSingleChatBinding
    private lateinit var refUser: DatabaseReference
    private lateinit var refMessage: DatabaseReference
    private lateinit var adapter: SingleChatAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageListener: AppChildEventListener
    private var countMessages = 15
    private var isScrolling = false
    private var smoothScrollToPosition = true
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var appVoiceRecorder: AppVoiceRecorder
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // use the returned uri
            val messageKey = getMessageKey(contact.id)
            val uriContent = result.uriContent

            uriContent?.let { uri ->
                uploadFileToStorage(uri, messageKey, contact.id, TYPE_MESSAGE_IMAGE)
                smoothScrollToPosition = true
            }
        } else {
            // an error occurred
            val exception = result.error
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSingleChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        initFields()
        initToolbar()
        initRecyclerView()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initFields() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet_choice)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        appVoiceRecorder = AppVoiceRecorder()
        swipeRefreshLayout = binding.chatSwipeRefresh
        layoutManager = LinearLayoutManager(this.context)
        toolbarInfo = APP_ACTIVITY.toolbar.toolbar_info
        toolbarInfo.visibility = View.VISIBLE
        binding.chatInputMessage.addTextChangedListener(AppTextWatcher {
            val string = binding.chatInputMessage.text.toString()
            if (string.isEmpty() || string == "Запись") {
                binding.chatBtnSendMessage.visibility = View.GONE
                binding.chatBtnAttach.visibility = View.VISIBLE
                binding.chatBtnVoice.visibility = View.VISIBLE
            } else {
                binding.chatBtnSendMessage.visibility = View.VISIBLE
                binding.chatBtnAttach.visibility = View.GONE
                binding.chatBtnVoice.visibility = View.GONE
            }
        })
        binding.chatBtnAttach.setOnClickListener { attach() }

        CoroutineScope(Dispatchers.IO).launch {
            binding.chatBtnVoice.setOnTouchListener { view, motionEvent ->
                if (checkPermission(RECORD_AUDIO)) {
                    if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                        val messageKey = getMessageKey(contact.id)
                        appVoiceRecorder.startRecord(messageKey)
                        binding.chatInputMessage.setText("Запись")
                        binding.chatBtnVoice.setColorFilter(
                            ContextCompat.getColor(
                                APP_ACTIVITY,
                                R.color.blue
                            )
                        )
                    } else if (motionEvent.action == MotionEvent.ACTION_UP) {
                        binding.chatInputMessage.setText("")
                        binding.chatBtnVoice.colorFilter = null
                        appVoiceRecorder.stopRecord { file, messageKey ->
                            uploadFileToStorage(
                                Uri.fromFile(file),
                                messageKey,
                                contact.id,
                                TYPE_MESSAGE_VOICE
                            )
                            smoothScrollToPosition = true
                        }
                    }
                }
                true
            }
        }
    }

    private fun attach() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        btn_attach_file.setOnClickListener { attachFile() }
        btn_attach_image.setOnClickListener { attachImage() }
    }

    private fun attachFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE)
        println()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            when (requestCode) {
                PICK_FILE_REQUEST_CODE -> {
                    val uri = data.data
                    val messageKey = getMessageKey(contact.id)
                    val fileName = getFileNameFromUri(uri!!)
                    uploadFileToStorage(uri, messageKey, contact.id, TYPE_MESSAGE_FILE, fileName)
                    smoothScrollToPosition = true
                }
            }
        }
    }

    private fun attachImage() {
        cropImage.launch(
            options() {
                setGuidelines(CropImageView.Guidelines.ON)
                setAspectRatio(1, 1)
                setRequestedSize(250, 250)
            }

        )
    }

    private fun initRecyclerView() {
        recyclerView = binding.chatRecyclerView
        adapter = SingleChatAdapter()
        refMessage = REF_DATABASE_ROOT
            .child(NODE_MESSAGES)
            .child(CURRENT_UID)
            .child(contact.id)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)
        recyclerView.isNestedScrollingEnabled = false

        messageListener = AppChildEventListener {
            val message = it.getCommonModel()
            if (smoothScrollToPosition) {
                adapter.addItemToBottom(AppViewFactory.getView(message)) {
                    recyclerView.smoothScrollToPosition(adapter.itemCount)
                }
            } else {
                adapter.addItemToTop(AppViewFactory.getView(message)) {
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }

        refMessage.limitToLast(countMessages).addChildEventListener(messageListener)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (isScrolling && dy < 0 && layoutManager.findFirstVisibleItemPosition() <= 3) {
                    updateData()
                }

            }
        })
        swipeRefreshLayout.setOnRefreshListener { updateData() }
    }

    private fun updateData() {
        smoothScrollToPosition = false
        isScrolling = false
        countMessages += 10
        refMessage.removeEventListener(messageListener)
        refMessage.limitToLast(countMessages).addChildEventListener(messageListener)
    }

    private fun initToolbar() {
        listenerInfoToolbar = AppValueEventListener {
            receivingUser = it.getUserModel()
            initInfoToolbar()
        }
        refUser = REF_DATABASE_ROOT
            .child(NODE_USERS)
            .child(contact.id)

        refUser.addValueEventListener(listenerInfoToolbar)

        binding.chatBtnSendMessage.setOnClickListener {
            smoothScrollToPosition = true
            val message = binding.chatInputMessage.text.toString()
            if (message.isEmpty()) {
                showToast("Нечего отправлять")
            } else {
                sendMessage(message, contact.id, TYPE_TEXT) {
                    saveToMainList(contact.id, TYPE_CHAT)
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

    override fun onDestroyView() {
        super.onDestroyView()
        appVoiceRecorder.releaseRecorder()
        adapter.onDestroy()
    }
}