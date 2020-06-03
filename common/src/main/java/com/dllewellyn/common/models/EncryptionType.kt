package com.dllewellyn.common.models

import java.lang.IllegalStateException

enum class EncryptionType(val s: String) {
    PASSWORD("password"),
    KEYSTORE("keystore");

    companion object {
        fun stringToType(type: String): EncryptionType = when (type) {
            "password" -> PASSWORD
            "keystore"-> KEYSTORE
            else -> throw IllegalStateException()
        }
    }
}