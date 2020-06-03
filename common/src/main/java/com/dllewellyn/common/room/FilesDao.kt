package com.dllewellyn.common.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FilesDao {

    @Query("SELECT * FROM singlefileentity")
    suspend fun getAll(): List<SingleFileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SingleFileEntity)

    @Query("SELECT * FROM singlefileentity WHERE forModule == :moduleName")
    suspend fun getAllForModule(moduleName: String): List<SingleFileEntity>
}