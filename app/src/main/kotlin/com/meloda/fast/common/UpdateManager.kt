package com.meloda.fast.common

import androidx.lifecycle.MutableLiveData
import com.meloda.fast.api.network.Answer
import com.meloda.fast.api.network.ota.OtaRepo
import com.meloda.fast.extensions.setIfNotEquals
import com.meloda.fast.model.UpdateItem
import kotlinx.coroutines.*

class UpdateManager(private val repo: OtaRepo) {

    companion object {
        val newUpdate = MutableLiveData<UpdateItem?>(null)
    }

    fun checkUpdates(block: ((error: Throwable?, item: UpdateItem?) -> Unit)): Job =
        CoroutineScope(Dispatchers.Default).launch {
//            val job: suspend () -> Answer<UpdateItem> = { repo.getUpdates() }
//
//            val response = job()
//
//            withContext(Dispatchers.Main) {
//                when (response) {
//                    is Answer.Success -> {
//                        val item = response.data
//
//                        if (AppGlobal.versionName.split('_').getOrNull(1) != item.version) {
//                            newUpdate.setIfNotEquals(item)
//                            block.invoke(null, item)
//                        } else {
//                            block.invoke(null, null)
//                        }
//                    }
//                    is Answer.Error -> {
//                        newUpdate.setIfNotEquals(null)
//                        block.invoke(response.throwable, null)
//                    }
//                }
//            }
        }
}