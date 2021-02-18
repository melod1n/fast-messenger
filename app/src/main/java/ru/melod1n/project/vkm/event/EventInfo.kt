package ru.melod1n.project.vkm.event

import ru.melod1n.project.vkm.api.VKApiKeys

class EventInfo<T> constructor(var key: VKApiKeys, var data: T? = null)