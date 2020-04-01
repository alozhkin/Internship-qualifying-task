package org.jetbrains.internship.utils

private val HEX_ARRAY = "0123456789ABCDEF".toCharArray()
fun ByteArray.toHexString(): String {
    val hexChars = CharArray(size * 2)
    for (j in indices) {
        val v: Int = this[j].toInt() and 0xFF
        hexChars[j * 2] = HEX_ARRAY[v ushr 4]
        hexChars[j * 2 + 1] = HEX_ARRAY[v and 0x0F]
    }
    return String(hexChars)
}

fun String.normalizeToAlgorithm(): String {
    with(this) {
        if (toLowerCase().contains("^sha[^-]".toRegex())) return substring(0..2) + "-" + substring(3)
    }
    return this
}