package com.meloda.fast.activity

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.amulyakhare.textdrawable.TextDrawable
import com.meloda.fast.R
import com.meloda.fast.activity.ui.presenter.MessagesPresenterDeprecated
import com.meloda.fast.activity.ui.view.MessagesViewDeprecated
import com.meloda.fast.api.model.VKConversation
import com.meloda.fast.api.model.VKGroup
import com.meloda.fast.api.model.VKModel
import com.meloda.fast.api.model.VKUser
import com.meloda.fast.base.BaseActivity
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.dialog.ProfileDialog
import com.meloda.fast.extensions.ContextExtensions.color
import com.meloda.fast.extensions.DrawableExtensions.tint
import com.meloda.fast.extensions.ImageViewExtensions.loadImage
import com.meloda.fast.fragment.SettingsFragment
import com.meloda.fast.util.KeyboardUtils
import com.meloda.fast.util.TextUtils
import com.meloda.fast.util.ViewUtils
import com.meloda.fast.widget.CircleImageView


class MessagesActivityDeprecated : BaseActivity(), MessagesViewDeprecated {

    companion object {
        const val TAG = "MessagesActivity"

        const val MESSAGES_COUNT = 30

        const val TAG_EXTRA_TITLE = "title"
        const val TAG_EXTRA_AVATAR = "avatar"
        const val TAG_EXTRA_ID = "id"
        const val TAG_EXTRA_USER = "user"
        const val TAG_EXTRA_GROUP = "group"
    }

    private var isEdit = false

    private var fabState = FabState.VOICE

    private enum class FabState {
        VOICE, SEND, EDIT, DELETE, BLOCKED
    }

    private var title = ""
    private var avatar = ""

    private var lastMessageText = ""
    private var attachments = arrayListOf<VKModel>()

    private var peerId = 0

    private var dialogUser: VKUser? = null
    private var dialogGroup: VKGroup? = null

    private lateinit var presenterDeprecated: MessagesPresenterDeprecated

    lateinit var recyclerView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var toolbar: Toolbar
    private lateinit var chatAvatar: CircleImageView
    private lateinit var chatTitle: TextView
    private lateinit var chatInfo: TextView
    private lateinit var chatPanel: LinearLayout
    private lateinit var chatMessage: EditText
    private lateinit var chatSend: ImageButton
    private lateinit var progressBar: ProgressBar

    private lateinit var noItemsView: LinearLayout
    private lateinit var noInternetView: LinearLayout
    private lateinit var errorView: LinearLayout

    override fun onDestroy() {
        super.onDestroy()
        presenterDeprecated.destroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)
        initViews()

        initExtraData()

        prepareToolbar()
        prepareRefreshLayout()
        prepareRecyclerView()
        prepareEditText()

        presenterDeprecated = MessagesPresenterDeprecated(this)
        presenterDeprecated.setup(peerId, recyclerView)
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recyclerView)
        refreshLayout = findViewById(R.id.refreshLayout)
        chatAvatar = findViewById(R.id.chatAvatar)
        chatTitle = findViewById(R.id.chatTitle)
        chatInfo = findViewById(R.id.chatInfo)
        chatPanel = findViewById(R.id.chatPanel)
        chatMessage = findViewById(R.id.chatMessage)
        chatSend = findViewById(R.id.chatSend)
        progressBar = findViewById(R.id.progressBar)

        noItemsView = findViewById(R.id.noItemsView)
        noInternetView = findViewById(R.id.noInternetView)
        errorView = findViewById(R.id.errorView)
    }

    private fun initExtraData() {
        peerId = intent.getIntExtra(TAG_EXTRA_ID, -1)
        title = intent.getStringExtra(TAG_EXTRA_TITLE) ?: ""
        avatar = intent.getStringExtra(TAG_EXTRA_AVATAR) ?: ""

        dialogUser = intent.getSerializableExtra(TAG_EXTRA_USER) as VKUser?
        dialogGroup = intent.getSerializableExtra(TAG_EXTRA_GROUP) as VKGroup?
    }

    private fun prepareToolbar() {
        setSupportActionBar(toolbar)

        val placeholder = TextDrawable
            .builder()
            .buildRound(TextUtils.getFirstLetterFromString(title), color(R.color.accent))

        chatAvatar.setImageDrawable(placeholder)

        chatAvatar.loadImage(avatar, placeholder)

        toolbar.setOnClickListener { presenterDeprecated.openProfile() }

        chatAvatar.setOnClickListener { presenterDeprecated.openProfile() }

        chatTitle.text = title

        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.navigationIcon.tint(color(R.color.accent))
    }

    private fun prepareRefreshLayout() {
        refreshLayout.isEnabled = false
    }

    private fun prepareRecyclerView() {
        recyclerView.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false).also {
                it.stackFromEnd = true
            }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy < 0 && AppGlobal.inputMethodManager.isAcceptingText && AppGlobal.preferences.getBoolean(
                        SettingsFragment.KEY_HIDE_KEYBOARD_ON_SCROLL_UP,
                        true
                    )
                ) {
                    KeyboardUtils.hideKeyboardFrom(chatMessage)
                }
            }
        })
    }

    private fun prepareEditText() {
        chatMessage.addTextChangedListener {
            fabState = if (it.toString().trim().isEmpty()) {
                if (isEdit) {
                    FabState.DELETE
                } else {
                    FabState.VOICE
                }
            } else {
                if (isEdit) {
                    FabState.EDIT
                } else {
                    FabState.SEND
                }
            }

            refreshFabStyle()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_messages, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()

            R.id.messagesRefresh -> {
                presenterDeprecated.updateData()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun refreshFabStyle() {
        chatSend.isClickable = true

        when (fabState) {
            FabState.VOICE -> {
                chatSend.apply {
                    setImageResource(R.drawable.ic_mic)

                    setOnClickListener {
                        showVoiceRecordingTip()
                    }
                    setOnLongClickListener {
                        true
                    }
                }
            }
            FabState.SEND -> {
                chatSend.apply {
                    setImageResource(R.drawable.ic_send)

                    setOnClickListener {
                        presenterDeprecated.sendMessage(chatMessage.text.toString(), attachments)
                    }

                    setOnLongClickListener {
                        presenterDeprecated.sendMessage(chatMessage.text.toString(), attachments, false)
                        true
                    }
                }
            }
            FabState.EDIT -> {
                chatSend.apply {
                    setImageResource(R.drawable.ic_done)

                    setOnClickListener {
                        //editMessage()
                    }

                    setOnLongClickListener {
                        performClick()
                        true
                    }
                }

            }
            FabState.DELETE -> {
                chatSend.apply {
                    setImageResource(R.drawable.ic_trash_outline)

                    chatSend.setOnClickListener {
                        //deleteMessage
                    }

                    chatSend.setOnLongClickListener {
                        performClick()
                        true
                    }
                }
            }
            FabState.BLOCKED -> {
                chatSend.apply {
                    isClickable = false
                    setImageResource(R.drawable.ic_lock)
                }
            }
        }
    }

    override fun showChatPanel() {
        chatPanel.isVisible = true
    }

    override fun hideChatPanel() {
        chatPanel.isVisible = false
    }

    override fun setWritingAllowed(allowed: Boolean) {
        if (allowed) {
            fabState = FabState.VOICE

            chatSend.imageTintList = ColorStateList.valueOf(color(R.color.accent))

            chatMessage.isEnabled = true

            chatPanel.setBackgroundResource(R.drawable.chat_panel_background)
        } else {
            fabState = FabState.BLOCKED

            chatSend.imageTintList = ColorStateList.valueOf(Color.WHITE)

            chatMessage.isEnabled = false
            chatMessage.setHintTextColor(Color.WHITE)
            chatMessage.setHint(R.string.no_access)

            chatPanel.setBackgroundResource(R.drawable.chat_panel_background_blocked)
        }
    }

    override fun setChatInfo(info: String) {
        chatInfo.text = info
        chatInfo.isVisible = info.isNotEmpty()
    }

    override fun openProfile(conversation: VKConversation) {
        conversation.let {
            val profileDialog = ProfileDialog(it, title)
            profileDialog.show(supportFragmentManager, ProfileDialog.TAG)
        }
    }

    override fun showErrorLoadConversationAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.error_occurred)
        builder.setMessage(R.string.error_loading_message)
        builder.setPositiveButton(R.string.retry) { _, _ ->
            presenterDeprecated.loadConversation(peerId)
        }
        builder.setNegativeButton(R.string.no) { _, _ -> onBackPressed() }
        builder.setCancelable(false)
        builder.show()
    }

    override fun showVoiceRecordingTip() {
        Toast.makeText(this, R.string.voice_record_tip, Toast.LENGTH_LONG).show()
    }

    override fun setMessageText(text: String) {
        chatMessage.setText(text)
    }

    override fun showErrorSnackbar(t: Throwable) {
        ViewUtils.showErrorSnackbar(getRootView(), t)
    }

    override fun prepareNoItemsView() {
    }

    override fun showNoItemsView() {
        noItemsView.isVisible = true
    }

    override fun hideNoItemsView() {
        noItemsView.isVisible = false
    }

    override fun prepareNoInternetView() {
    }

    override fun showNoInternetView() {
        noInternetView.isVisible = true
    }

    override fun hideNoInternetView() {
        noInternetView.isVisible = false
    }

    override fun prepareErrorView() {
    }

    override fun showErrorView() {
        errorView.isVisible = true
    }

    override fun hideErrorView() {
        errorView.isVisible = false
    }

    override fun showProgressBar() {
        progressBar.isVisible = true
    }

    override fun hideProgressBar() {
        progressBar.isVisible = false
    }

    override fun showRefreshLayout() {
        refreshLayout.isRefreshing = true
    }

    override fun hideRefreshLayout() {
        refreshLayout.isRefreshing = false
    }


}