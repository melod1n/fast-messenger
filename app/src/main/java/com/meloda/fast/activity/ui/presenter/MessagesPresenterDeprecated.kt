package com.meloda.fast.activity.ui.presenter

import androidx.recyclerview.widget.RecyclerView
import com.meloda.concurrent.EventInfo
import com.meloda.concurrent.TaskManager
import com.meloda.fast.R
import com.meloda.fast.UserConfig
import com.meloda.fast.activity.ui.repository.MessagesRepositoryDeprecated
import com.meloda.fast.activity.ui.view.MessagesViewDeprecated
import com.meloda.fast.adapter.MessagesAdapterDeprecated
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.listener.ItemClickListener
import com.meloda.fast.listener.ItemLongClickListener
import com.meloda.mvp.MvpOnResponseListener
import com.meloda.mvp.MvpPresenter
import com.meloda.vksdk.model.VKConversation
import com.meloda.vksdk.model.VKMessage
import com.meloda.vksdk.model.VKModel
import kotlin.random.Random

class MessagesPresenterDeprecated(viewState: MessagesViewDeprecated) :
    MvpPresenter<VKMessage, MessagesRepositoryDeprecated, MessagesViewDeprecated>(
        viewState,
        MessagesRepositoryDeprecated::class.java.name
    ),
    ItemClickListener,
    ItemLongClickListener,
    TaskManager.OnEventListener {

    companion object {
        const val DEFAULT_MESSAGES_COUNT = 30
    }

    private lateinit var adapter: MessagesAdapterDeprecated
    private lateinit var conversation: VKConversation

    private var peerId: Int = -1

    private var lastMessageText: String = ""

    private lateinit var recyclerView: RecyclerView

    override fun destroy() {
        adapter.destroy()
    }

    fun setup(peerId: Int, recyclerView: RecyclerView) {
        this.peerId = peerId
        this.recyclerView = recyclerView
        this.context = recyclerView.context

        viewState.showProgressBar()
        getCachedConversation(peerId)
    }

    fun updateData() {
        adapter.clear()
        loadMessages(peerId)
    }

    fun openProfile() {
        viewState.openProfile(conversation)
    }

    private fun createAdapter() {
        adapter = MessagesAdapterDeprecated(context!!, arrayListOf(), conversation).also {
            it.itemClickListener = this
            it.itemLongClickListener = this
        }

        recyclerView.adapter = adapter
    }

    private fun getCachedConversation(peerId: Int) {
        repository.getCachedConversation(peerId, object : MvpOnResponseListener<VKConversation> {
            override fun onResponse(response: VKConversation) {
                conversation = response

                createAdapter()
                refreshConversation(response)

                getCachedMessages(peerId, 0, DEFAULT_MESSAGES_COUNT,
                    object : MvpOnResponseListener<Any?> {
                        override fun onResponse(response: Any?) {
                            loadConversation(peerId)
                            loadMessages(peerId)
                        }

                        override fun onError(t: Throwable) {
                            loadConversation(peerId)
                            loadMessages(peerId)
                        }
                    })
            }

            override fun onError(t: Throwable) {
                loadConversation(peerId)
                loadMessages(peerId)
            }
        })
    }

    fun loadConversation(peerId: Int) {
        if (adapter.isNotEmpty()) {
            viewState.hideProgressBar()
        }

        repository.loadConversation(peerId, object : MvpOnResponseListener<VKConversation> {

            override fun onResponse(response: VKConversation) {
                conversation = response

                createAdapter()
                refreshConversation(response)
            }

            override fun onError(t: Throwable) {
                viewState.hideProgressBar()
                viewState.showErrorLoadConversationAlert()
            }

        })
    }

    private fun refreshConversation(conversation: VKConversation) {
        checkIsWritingAllowed(conversation)

        repository.getChatInfo(
            conversation,
            object : MvpOnResponseListener<String> {
                override fun onResponse(response: String) {
                    viewState.setChatInfo(response)
                }

                override fun onError(t: Throwable) {
                    viewState.setChatInfo(AppGlobal.resources.getString(R.string.error_obtain_chat_info))
                }
            })
    }

    private fun checkIsWritingAllowed(conversation: VKConversation) {
        if (conversation.isGroupChannel) {
            viewState.hideChatPanel()
            return
        }

        viewState.showChatPanel()
        viewState.setWritingAllowed(conversation.isAllowed)
    }

    private fun getCachedMessages(
        peerId: Int,
        offset: Int = 0,
        count: Int = DEFAULT_MESSAGES_COUNT,
        listener: MvpOnResponseListener<Any?>? = null
    ) {
        repository.getCachedMessages(peerId, offset, count,
            object : MvpOnResponseListener<ArrayList<VKMessage>> {
                override fun onResponse(response: ArrayList<VKMessage>) {
                    viewState.hideProgressBar()
                    fillAdapter(response, offset)

                    listener?.onResponse(null)
                }

                override fun onError(t: Throwable) {
                    if (adapter.isEmpty()) {
                        viewState.showProgressBar()
                    }

                    listener?.onError(t)
                }
            })
    }

    private fun loadMessages(peerId: Int, offset: Int = 0, count: Int = DEFAULT_MESSAGES_COUNT) {
        repository.loadMessages(peerId, offset, count,
            object : MvpOnResponseListener<ArrayList<VKMessage>> {
                override fun onResponse(response: ArrayList<VKMessage>) {
                    fillAdapter(response, offset)
                }

                override fun onError(t: Throwable) {

                }

            })
    }

    private fun fillAdapter(
        messages: ArrayList<VKMessage>,
        offset: Int
    ) {
        if (adapter.isEmpty()) adapter.isNotCachedValues = true
        if (offset == 0) {
            adapter.updateValues(messages)
        } else {
            adapter.addAll(messages)
        }

        adapter.notifyDataSetChanged()

        if (offset == 0) recyclerView.scrollToPosition(adapter.itemCount - 1)
    }

    override fun onItemClick(position: Int) {

    }

    override fun onItemLongClick(position: Int) {

    }

    override fun onNewEvent(info: EventInfo<*>) {

    }

    fun sendMessage(
        text: String = "",
        attachments: ArrayList<VKModel> = arrayListOf(),
        scrollToBottom: Boolean = true
    ) {
        lastMessageText = text

        val message = VKMessage().also {
            it.date = (System.currentTimeMillis() / 1000).toInt()
            it.text = text
            it.isOut = true
            it.peerId = peerId
            it.fromId = UserConfig.userId
            it.randomId = Random.nextInt()
        }

        viewState.setMessageText("")

        adapter.addMessage(message, true, scrollToBottom)

        repository.sendMessage(peerId, text, message.randomId, object : MvpOnResponseListener<Int> {
            override fun onResponse(response: Int) {
                message.id = response

//                TaskManager.execute { MemoryCache.put(message) }
//                TaskManager.loadMessage(VKApiKeys.UPDATE_MESSAGE, response)
            }

            override fun onError(t: Throwable) {
                viewState.showErrorSnackbar(t)

                viewState.setMessageText(lastMessageText)
            }
        })
    }

}