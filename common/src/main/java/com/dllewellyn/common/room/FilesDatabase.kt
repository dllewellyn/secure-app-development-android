package com.dllewellyn.common.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SingleFileEntity::class], version = 1)
abstract class FilesDatabase : RoomDatabase() {
    abstract fun fileDao(): FilesDao
}