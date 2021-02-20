package com.meloda.fast.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.meloda.fast.R
import com.meloda.fast.adapter.SimpleItemAdapter
import com.meloda.fast.api.model.VKConversation
import com.meloda.fast.api.model.VKUser
import com.meloda.fast.database.MemoryCache
import com.meloda.fast.item.SimpleMenuItem

open class ProfileDialog(
    private val conversation: VKConversation,
    private val chatTitle: String
) : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "profile_bottom_sheet_dialog"
    }

    private lateinit var title: TextView
    private lateinit var subtitle: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var root: LinearLayout

    private var adapter: SimpleItemAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NO_TITLE, R.style.AppTheme_ProfileDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_profile_bottom, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = view.findViewById(R.id.profileTitle)
        subtitle = view.findViewById(R.id.profileSubtitle)
        recyclerView = view.findViewById(R.id.profileItemMenu)
        root = view.findViewById(R.id.profileRoot)

        val layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        recyclerView.layoutManager = layoutManager

        title.text = chatTitle

        subtitle.text = getSubtitle()

        val items = ArrayList<SimpleMenuItem>()

        items.add(
            SimpleMenuItem(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_search
                )!!, "Search"
            )
        )

        createAdapter(items)
    }

    private fun createAdapter(items: ArrayList<SimpleMenuItem>) {
        adapter = SimpleItemAdapter(requireContext(), items)
        recyclerView.adapter = adapter
    }

    private fun getSubtitle(): String {
        return when (conversation.type) {
            VKConversation.TYPE_CHAT -> getString(
                R.string.chat_members,
                conversation.membersCount
            )
            VKConversation.TYPE_GROUP -> {
                val group = MemoryCache.getGroupById(conversation.conversationId) ?: return ""

                "@${group.screenName}"
            }
            VKConversation.TYPE_USER -> {
//                    val user = MemoryCache.getUserById(conversation.id) ?: return ""

                //TODO: придумать чо делать
                val user: VKUser = null ?: return ""

                var str =
                    if (user.screenName.contains("id${user.userId}")) "" else "@${user.screenName}"

                val online =
                    getString(if (user.isOnlineMobile) R.string.user_online_mobile else if (user.isOnline) R.string.user_online else R.string.user_offline)

                str += if (str.isEmpty()) online else " · $online"

                str
            }
            else -> ""
        }
    }
}