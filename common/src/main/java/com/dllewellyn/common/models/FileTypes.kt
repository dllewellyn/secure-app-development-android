package com.dllewellyn.common.models

import java.lang.IllegalArgumentException

enum class FileTypes(val s: String) {
    ROOM_DATABASE("room"),
    FILE("file"),
    SHARED_PREFERENCES("shared_prefs");

    companion object {
        fun stringToType(type: String): FileTypes = when (type) {
            "room" -> ROOM_DATABASE
            "file" -> FILE
            "shared_prefs" -> SHARED_PREFERENCES
            else -> throw IllegalArgumentException()
        }
    }
}