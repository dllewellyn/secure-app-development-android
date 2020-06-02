package com.dllewellyn.common.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SecureNoteEntity(
    @PrimaryKey(autoGenerate = true) val uid: Int?,
    @ColumnInfo(name = "note") val noteText: String?
)