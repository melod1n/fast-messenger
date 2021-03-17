package com.meloda.mvp

interface MvpView {

    fun showErrorSnackbar(t: Throwable)

    fun prepareNoItemsView()

    fun showNoItemsView()

    fun hideNoItemsView()

    fun prepareNoInternetView()

    fun showNoInternetView()

    fun hideNoInternetView()

    fun prepareErrorView()

    fun showErrorView()

    fun hideErrorView()

    fun showProgressBar()

    fun hideProgressBar()

    fun showRefreshLayout()

    fun hideRefreshLayout()

}