package com.meloda.fast.extensions

import android.graphics.*
import kotlin.math.min

fun Bitmap.borderedCircularBitmap(
    borderColor: Int = 0,
    borderWidth: Int = 0
): Bitmap? {
    val bitmap = Bitmap.createBitmap(
        width, // width in pixels
        height, // height in pixels
        Bitmap.Config.ARGB_8888
    )

    // canvas to draw circular bitmap
    val canvas = Canvas(bitmap)

    // get the maximum radius
    val radius = min(width / 2f, height / 2f)

    // create a path to draw circular bitmap border
    val borderPath = Path().apply {
        addCircle(
            width / 2f,
            height / 2f,
            radius,
            Path.Direction.CCW
        )
    }

    // draw border on circular bitmap
    canvas.clipPath(borderPath)
    canvas.drawColor(borderColor)


    // create a path for circular bitmap
    val bitmapPath = Path().apply {
        addCircle(
            width / 2f,
            height / 2f,
            radius - borderWidth,
            Path.Direction.CCW
        )
    }

    canvas.clipPath(bitmapPath)
    val paint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        isAntiAlias = true
    }

    // clear the circular bitmap drawing area
    // it will keep bitmap transparency
    canvas.drawBitmap(this, 0f, 0f, paint)

    // now draw the circular bitmap
    canvas.drawBitmap(this, 0f, 0f, null)


    val diameter = (radius * 2).toInt()
    val x = (width - diameter) / 2
    val y = (height - diameter) / 2

    // return cropped circular bitmap with border
    return Bitmap.createBitmap(
        bitmap, // source bitmap
        x, // x coordinate of the first pixel in source
        y, // y coordinate of the first pixel in source
        diameter, // width
        diameter // height
    )
}