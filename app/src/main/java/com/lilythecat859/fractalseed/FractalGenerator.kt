package com.lilythecat859.fractalseed

import android.graphics.Bitmap
import androidx.renderscript.*
import java.security.MessageDigest

object FractalGenerator {

    private const val WIDTH = 1440
    private const val HEIGHT = 1440
    private const val MAX_ITER = 120

    suspend fun generate(seedBytes: ByteArray): Bitmap =
        androidx.renderscript.RenderScript.createContext().use { rs ->
            val digest = MessageDigest.getInstance("SHA-256").digest(seedBytes)
            val seed = digest.fold(0L) { acc, b -> (acc shl 8) + (b.toInt() and 0xff) }

            val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)

            val script = ScriptC_buddhabrot(rs)
            script._width = WIDTH
            script._height = HEIGHT
            script._maxIter = MAX_ITER
            script._seed = seed

            val allocOut = Allocation.createFromBitmap(rs, bitmap)
            script.forEach_root(allocOut, allocOut)
            allocOut.copyTo(bitmap)
            bitmap
        }
}
