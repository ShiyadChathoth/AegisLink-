package com.aegislink.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WhitelistDao {
    @Query("SELECT * FROM whitelist WHERE domain = :domain LIMIT 1")
    suspend fun find(domain: String): WhitelistEntity?

    @Query("SELECT * FROM whitelist ORDER BY domain ASC LIMIT :limit")
    suspend fun list(limit: Int): List<WhitelistEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WhitelistEntity)

    @Query("DELETE FROM whitelist WHERE domain = :domain")
    suspend fun deleteByDomain(domain: String): Int
}
