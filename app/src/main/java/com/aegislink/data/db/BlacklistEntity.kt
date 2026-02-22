package com.aegislink.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blacklist")
data class BlacklistEntity(
    @PrimaryKey val domain: String,
    val reason: String = "Matched local blacklist"
)
