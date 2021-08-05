package com.meloda.fast.io

import java.io.ByteArrayOutputStream

class BytesOutputStream : ByteArrayOutputStream {
    constructor() : super(8192)
    constructor(size: Int) : super(size)

    val byteArray: ByteArray = buf
}