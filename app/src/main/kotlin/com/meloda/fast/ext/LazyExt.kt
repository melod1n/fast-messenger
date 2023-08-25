package com.meloda.fast.ext

/**
 * Вариант [lazy], но без потокобезопасности
 */
fun <T> unsafeLazy(initializer: () -> T): Lazy<T> = lazy(LazyThreadSafetyMode.NONE, initializer)
