package com.github.mpiotr.competitionwatch

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "info")
data class Settings(
    @PrimaryKey val id : Long,
    val start_interval_seconds : Int = 1,
    val competition_start_time : Long = 0L,
    )

