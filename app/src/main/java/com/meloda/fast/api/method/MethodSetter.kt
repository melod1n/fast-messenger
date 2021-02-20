package com.meloda.fast.api.method

import android.util.ArrayMap
import android.util.Log
import com.meloda.fast.BuildConfig
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VKApi
import com.meloda.fast.listener.OnResponseListener
import com.meloda.fast.util.ArrayUtils
import java.net.URLEncoder

@Suppress("UNCHECKED_CAST")
open class MethodSetter(private val name: String) {

    private val params: ArrayMap<String, String> = ArrayMap()

    fun put(key: String, value: Any): MethodSetter {
        params[key] = value.toString()
        return this
    }

    fun put(key: String, value: String): MethodSetter {
        params[key] = value
        return this
    }

    fun put(key: String, value: Int): MethodSetter {
        params[key] = value.toString()
        return this
    }

    fun put(key: String, value: Long): MethodSetter {
        params[key] = value.toString()
        return this
    }

    fun put(key: String, value: Boolean): MethodSetter {
        params[key] = if (value) "1" else "0"
        return this
    }

    private fun getSignedUrl(): String {
        if (!params.containsKey("access_token")) {
            params["access_token"] = UserConfig.token
        }

        if (!params.containsKey("v")) {
            params["v"] = VKApi.API_VERSION
        }

        if (!params.containsKey("lang")) {
            params["lang"] = VKApi.language
        }

        return "${VKApi.BASE_URL}$name?${retrieveParams()}"
    }

    private fun retrieveParams(): String {
        val builder = StringBuilder()

        for (i in 0 until params.size) {
            val key = params.keyAt(i)
            val value = params.valueAt(i)

            if (builder.isNotEmpty()) {
                builder.append("&")
            }

            builder.append(key)
            builder.append("=")
            builder.append(URLEncoder.encode(value, "UTF-8"))
        }

        val params = builder.toString()

        if (BuildConfig.DEBUG) {
            Log.i("MethodSetter", "retrieved params: $params")
        }

        return params
    }

    fun <E> execute(cls: Class<E>): ArrayList<E>? {
        return VKApi.execute(getSignedUrl(), cls)
    }

    fun <E> executeArray(cls: Class<E>, listener: OnResponseListener<ArrayList<E>>?) {
        VKApi.executeArray(getSignedUrl(), cls, listener)
    }

    fun <E> execute(cls: Class<E>, listener: OnResponseListener<E>?) {
        VKApi.execute(getSignedUrl(), cls, listener)
    }

    fun userId(value: Int): MethodSetter {
        return put("user_id", value)
    }

    fun userIds(vararg ids: Int): MethodSetter {
        return put("user_ids", ArrayUtils.asString(ids))
    }

    fun userIds(ids: ArrayList<Int>): MethodSetter {
        return put("user_ids", ArrayUtils.asString(ids))
    }

    fun ownerId(value: Int): MethodSetter {
        return put("owner_id", value)
    }

    fun groupId(value: Int): MethodSetter {
        return put("group_id", value)
    }

    fun groupIds(vararg ids: Int): MethodSetter {
        return put("group_ids", ArrayUtils.asString(ids))
    }

    fun groupIds(ids: ArrayList<Int>): MethodSetter {
        return put("group_ids", ArrayUtils.asString(ids))
    }

    fun fields(values: String): MethodSetter {
        return put("fields", values)
    }

    fun count(value: Int): MethodSetter {
        return put("count", value)
    }

    fun sort(value: Int): MethodSetter {
        put("sort", value)
        return this
    }

    /**
     *
     * hints — сортировать по рейтингу, аналогично тому, как друзья сортируются в разделе Мои друзья
     * random — возвращает друзей в случайном порядке.
     * mobile — возвращает выше тех друзей, у которых установлены мобильные приложения.
     * name — сортировать по имени (долго)
     *
     */

    fun order(value: String): MethodSetter {
        put("order", value)
        return this
    }

    fun offset(value: Int = 0): MethodSetter {
        return put("offset", value)
    }

    fun nameCase(value: String): MethodSetter {
        return put("name_case", value)
    }

    fun captchaSid(value: String): MethodSetter {
        return put("captcha_sid", value)
    }

    fun captchaKey(value: String): MethodSetter {
        return put("captcha_key", value)
    }

}