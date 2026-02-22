package com.aegislink.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "whitelist")
data class WhitelistEntity(
    @PrimaryKey val domain: String,
    val note: String = "User trusted"
)
