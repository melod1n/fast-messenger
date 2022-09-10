package com.meloda.fast.screens.chatinfo

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ChatInfoPagerAdapter(
    private val fragment: ChatInfoFragment
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return fragment.getTabsCount()
    }

    override fun createFragment(position: Int): Fragment {
        return fragment.createTabFragment(position)
    }

}