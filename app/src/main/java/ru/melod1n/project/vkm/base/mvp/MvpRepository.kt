package ru.melod1n.project.vkm.base.mvp

@Suppress("UNCHECKED_CAST")
abstract class MvpRepository<T> {

    //    abstract fun loadValues(fields: MvpFields, listener: MvpOnLoadListener<T>?)
//
//    abstract fun loadCachedValues(fields: MvpFields, listener: MvpOnLoadListener<T>?)
//
//    protected fun sendError(listener: MvpOnLoadListener<T>?, errorId: String) {
//        listener?.onErrorLoad(MvpException(errorId))
//    }
//
    protected fun <Item> sendError(
        listener: MvpOnLoadListener<Item>,
        t: Throwable
    ) {
//        if (listener !is MvpOnLoadListener) return

        MvpBase.post { listener.onError(t) }
    }

    protected fun <Item> sendResponseArray(
        listener: MvpOnLoadListener<ArrayList<Item>>,
        response: ArrayList<Item>
    ) {
        listener.let { MvpBase.handler.post { listener.onResponse(response) } }
    }

    protected fun <Item> sendResponse(
        listener: MvpOnLoadListener<Item>,
        response: Item
    ) {
        listener.let {
            MvpBase.handler.post { listener.onResponse(response) }
        }
    }

//    protected open fun cacheLoadedValues(values: ArrayList<T>) {}
//
//    protected fun startNewThread(runnable: Runnable) {
//        Thread(runnable).start()
//    }
//
//    protected fun post(runnable: Runnable) {
//        MvpBase.handler.post(runnable)
//    }
}