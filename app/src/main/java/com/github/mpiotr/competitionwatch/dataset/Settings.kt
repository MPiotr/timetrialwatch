package com.github.mpiotr.competitionwatch.dataset

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "info")
data class Settings(
    @PrimaryKey val id : Long,
    val start_interval_seconds : Int = 1,
    val start_initial_offset_seconds : Int = 30,
    val competition_start_time : Long = 0L,
    val play_start_sound : Boolean = true,
    val automatic_start  : Boolean = false,
    val use_name : Boolean = true,
    val use_email : Boolean = true,
    val use_colors : Boolean = true
    //val use_team : Boolean = false
    //val use_age : Boolean = false
   )