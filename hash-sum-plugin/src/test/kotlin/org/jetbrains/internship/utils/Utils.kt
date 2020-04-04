package org.jetbrains.internship.utils

import java.lang.IllegalArgumentException

enum class Algorithm {
    SHA1,
    SHA224,
    SHA256,
    SHA384,
    SHA512,
    MD2,
    MD5;

    companion object {
        // cannot distinguish MD2 from MD5, uses MD5 by default
        fun getAlgorithmWithCode(code: String): Algorithm {
            return if (code.matches("^[a-fA-F0-9]+$".toRegex())) {
                when (code.length) {
                    32 -> MD5
                    40 -> SHA1
                    56 -> SHA224
                    64 -> SHA256
                    96 -> SHA384
                    128 -> SHA512
                    else -> throw IllegalArgumentException("Unsupported algorithm")
                }
            } else {
                throw IllegalArgumentException("Unsupported algorithm")
            }
        }
    }
}