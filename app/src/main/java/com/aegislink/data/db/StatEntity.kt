package com.aegislink.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stats")
data class StatEntity(
    @PrimaryKey val key: String,
    val count: Long
)
