package com.aegislink.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BlacklistDao {
    @Query("SELECT * FROM blacklist WHERE domain = :domain LIMIT 1")
    suspend fun find(domain: String): BlacklistEntity?

    @Query("SELECT * FROM blacklist ORDER BY domain ASC LIMIT :limit")
    suspend fun list(limit: Int): List<BlacklistEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<BlacklistEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: BlacklistEntity)

    @Query("DELETE FROM blacklist WHERE domain = :domain")
    suspend fun deleteByDomain(domain: String): Int
}
