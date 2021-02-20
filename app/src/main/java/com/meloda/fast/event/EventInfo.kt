package com.meloda.fast.event

import com.meloda.fast.api.VKApiKeys

class EventInfo<T> constructor(var key: VKApiKeys, var data: T? = null)