package com.dllewellyn.common.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity
class SingleFileEntity(
    @PrimaryKey
    val path: String,
    val type: String,
    val forModule: String,
    val encrypted: Boolean,
    val encryptionType : String?=null
) : Serializable