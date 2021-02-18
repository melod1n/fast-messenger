package ru.melod1n.project.vkm.common

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import ru.melod1n.project.vkm.R
import ru.melod1n.project.vkm.fragment.FragmentConversations
import ru.melod1n.project.vkm.fragment.FragmentFriends
import ru.melod1n.project.vkm.fragment.FragmentImportant

object FragmentSwitcher {

    fun getCurrentFragment(fragmentManager: FragmentManager): Fragment? {
        val fragments = fragmentManager.fragments

        if (fragments.isEmpty()) throw RuntimeException("FragmentManager's fragments is empty")

        for (fragment in fragments) {
            if (fragment.isVisible) {
                return fragment
            }
        }

        return null
    }

    fun addFragments(
        fragmentManager: FragmentManager,
        containerId: Int,
        fragments: Collection<Fragment>
    ) {
        val transaction = fragmentManager.beginTransaction()

        for (fragment in fragments) {
            transaction.add(containerId, fragment, fragment.javaClass.simpleName)
        }

        transaction.commitNow()
    }

    fun showFragment(fragmentManager: FragmentManager, tag: String) {
        showFragment(fragmentManager, tag, false)
    }

    fun showFragment(
        fragmentManager: FragmentManager,
        tag: String,
        hideOthers: Boolean,
        containerId: Int = R.id.fragmentContainer
    ) {
        val fragments = fragmentManager.fragments

        if (fragments.isEmpty()) throw RuntimeException("FragmentManager's fragments is empty")

        var fragmentToShow: Fragment? = null

        for (fragment in fragments) {
            if (fragment.tag != null && fragment.tag == tag) {
                fragmentToShow = fragment
                break
            }
        }

        val transaction = fragmentManager.beginTransaction()

        if (fragmentToShow == null) {
            fragmentToShow = createFragmentByTag(tag)
            transaction.add(containerId, fragmentToShow, tag)
        } else {
            transaction.show(fragmentToShow)
        }

        if (hideOthers) {
            for (fragment in fragments) {
                if (fragment.tag != null && fragment.tag == tag) continue
                transaction.hide(fragment)
            }
        }

        transaction.commit()
    }

    fun clearFragments(fragmentManager: FragmentManager) {
        val fragments = fragmentManager.fragments

        if (fragments.isEmpty()) throw RuntimeException("FragmentManager's fragments is empty")

        val transaction = fragmentManager.beginTransaction()

        for (fragment in fragments) {
            transaction.remove(fragment)
        }

        transaction.commitNow()
    }

    fun hideFragments(fragmentManager: FragmentManager) {
        val fragments = fragmentManager.fragments

        if (fragments.isEmpty()) throw RuntimeException("FragmentManager's fragments is empty")

        val transaction = fragmentManager.beginTransaction()

        for (fragment in fragments) {
            transaction.hide(fragment)
        }

        transaction.commitNow()
    }

    private fun createFragmentByTag(tag: String): Fragment {
        return when (tag) {
            "FragmentFriends" -> FragmentFriends()
            "FragmentImportant" -> FragmentImportant()
            "FragmentConversations" -> FragmentConversations()
            else -> Fragment()
        }
    }
}
