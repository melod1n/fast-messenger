package ru.melod1n.project.vkm.base.mvp

import android.content.Context
import android.os.Bundle
import androidx.annotation.NonNull
import androidx.annotation.Nullable

@Suppress("UNCHECKED_CAST")
abstract class MvpPresenter<MainItem, Repository : MvpRepository<MainItem>, V : MvpView>(
    protected var viewState: V,
    private val repositoryStringClassName: String
) {

    protected var context: Context? = null

    protected fun requireContext(): Context {
        if (context == null) throw IllegalStateException("context is null")

        return context!!
    }

    enum class ListState {
        EMPTY, EMPTY_LOADING, EMPTY_NO_INTERNET, EMPTY_ERROR, FILLED, FILLED_LOADING
    }

    protected var tag: String = ""

    lateinit var repository: Repository

    init {
        initRepository()
    }

    private fun initRepository() {
        val clazz = Class.forName(repositoryStringClassName)

        this.repository = clazz.newInstance() as Repository
    }

    open fun onCreate(context: Context, bundle: Bundle? = null) {
        this.context = context
    }

    open fun onCreateView(bundle: Bundle? = null) {}
    open fun onViewCreated(bundle: Bundle? = null) {}

    protected fun post(runnable: Runnable) {
        MvpBase.post(runnable)
    }

    open fun destroy() {}

    fun prepareViews() {
        viewState.prepareNoItemsView()
        viewState.prepareNoInternetView()
        viewState.prepareErrorView()
    }

    fun setState(state: ListState) {
        when (state) {
            ListState.EMPTY -> {
                viewState.hideRefreshLayout()
                viewState.hideProgressBar()
                viewState.showNoItemsView()
                viewState.hideNoInternetView()
                viewState.hideErrorView()
            }
            ListState.EMPTY_LOADING -> {
                viewState.hideRefreshLayout()
                viewState.showProgressBar()
                viewState.hideNoItemsView()
                viewState.hideNoInternetView()
                viewState.hideErrorView()
            }
            ListState.EMPTY_NO_INTERNET -> {
                viewState.hideRefreshLayout()
                viewState.hideProgressBar()
                viewState.hideNoItemsView()
                viewState.showNoInternetView()
                viewState.hideErrorView()
            }
            ListState.EMPTY_ERROR -> {
                viewState.hideRefreshLayout()
                viewState.hideProgressBar()
                viewState.hideNoItemsView()
                viewState.hideNoInternetView()
                viewState.showErrorView()
            }
            ListState.FILLED -> {
                viewState.hideRefreshLayout()
                viewState.hideProgressBar()
                viewState.hideNoItemsView()
                viewState.hideNoInternetView()
                viewState.hideErrorView()
            }
            ListState.FILLED_LOADING -> {
                viewState.showRefreshLayout()
                viewState.hideProgressBar()
                viewState.hideNoItemsView()
                viewState.hideNoInternetView()
                viewState.hideErrorView()
            }
        }
    }

}