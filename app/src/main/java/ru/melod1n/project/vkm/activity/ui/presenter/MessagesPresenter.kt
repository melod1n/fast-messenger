package ru.melod1n.project.vkm.activity.ui.presenter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import ru.melod1n.project.vkm.R
import ru.melod1n.project.vkm.activity.ui.repository.MessagesRepository
import ru.melod1n.project.vkm.activity.ui.view.MessagesView
import ru.melod1n.project.vkm.adapter.MessagesAdapter
import ru.melod1n.project.vkm.api.UserConfig
import ru.melod1n.project.vkm.api.VKApiKeys
import ru.melod1n.project.vkm.api.model.VKConversation
import ru.melod1n.project.vkm.api.model.VKMessage
import ru.melod1n.project.vkm.api.model.VKModel
import ru.melod1n.project.vkm.base.mvp.MvpOnLoadListener
import ru.melod1n.project.vkm.base.mvp.MvpPresenter
import ru.melod1n.project.vkm.common.AppGlobal
import ru.melod1n.project.vkm.common.TaskManager
import ru.melod1n.project.vkm.database.MemoryCache
import ru.melod1n.project.vkm.event.EventInfo
import ru.melod1n.project.vkm.listener.ItemClickListener
import ru.melod1n.project.vkm.listener.ItemLongClickListener
import kotlin.random.Random

class MessagesPresenter(viewState: MessagesView) :
    MvpPresenter<VKMessage, MessagesRepository, MessagesView>(
        viewState,
        MessagesRepository::class.java.name
    ),
    ItemClickListener,
    ItemLongClickListener,
    TaskManager.OnEventListener {

    companion object {
        const val DEFAULT_MESSAGES_COUNT = 30
    }

    private lateinit var adapter: MessagesAdapter
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
        adapter = MessagesAdapter(context!!, arrayListOf(), conversation).also {
            it.itemClickListener = this
            it.itemLongClickListener = this
        }

        recyclerView.adapter = adapter
    }

    private fun getCachedConversation(peerId: Int) {
        repository.getCachedConversation(peerId, object : MvpOnLoadListener<VKConversation> {
            override fun onResponse(response: VKConversation) {
                conversation = response

                createAdapter()
                refreshConversation(response)

                getCachedMessages(peerId, 0, DEFAULT_MESSAGES_COUNT,
                    object : MvpOnLoadListener<Any?> {
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

        repository.loadConversation(peerId, object : MvpOnLoadListener<VKConversation> {

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

        repository.getChatInfo(conversation, object : MvpOnLoadListener<String> {
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
        listener: MvpOnLoadListener<Any?>? = null
    ) {
        repository.getCachedMessages(peerId, offset, count,
            object : MvpOnLoadListener<ArrayList<VKMessage>> {
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
            object : MvpOnLoadListener<ArrayList<VKMessage>> {
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

        repository.sendMessage(peerId, text, message.randomId, object : MvpOnLoadListener<Int> {
            override fun onResponse(response: Int) {
                message.messageId = response

                TaskManager.execute { MemoryCache.put(message) }
                TaskManager.loadMessage(VKApiKeys.UPDATE_MESSAGE, response)
            }

            override fun onError(t: Throwable) {
                viewState.showErrorSnackbar(t)

                viewState.setMessageText(lastMessageText)
            }
        })
    }

}