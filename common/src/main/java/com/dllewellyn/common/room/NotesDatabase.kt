package com.dllewellyn.common.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SecureNoteEntity::class], version = 1)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun notesDao(): NotesDao
}