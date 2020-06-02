package com.dllewellyn.common.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface NotesDao {

    @Query("SELECT * FROM securenoteentity")
    suspend fun getAll(): List<SecureNoteEntity>

    @Insert
    suspend fun insert(entity: SecureNoteEntity)
}