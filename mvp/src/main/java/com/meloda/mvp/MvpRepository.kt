package com.meloda.mvp

@Suppress("UNCHECKED_CAST")
abstract class MvpRepository<T> {

    protected fun <Item> sendError(
        listener: MvpOnResponseListener<Item>,
        t: Throwable
    ) {
        MvpBase.post { listener.onError(t) }
    }

    protected fun <Item> sendResponseArray(
        listener: MvpOnResponseListener<ArrayList<Item>>,
        response: ArrayList<Item>
    ) {
        listener.let { MvpBase.handler.post { listener.onResponse(response) } }
    }

    protected fun <Item> sendResponse(
        listener: MvpOnResponseListener<Item>,
        response: Item
    ) {
        listener.let {
            MvpBase.handler.post { listener.onResponse(response) }
        }
    }
}