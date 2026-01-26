package com.github.mpiotr.competitionwatch

import androidx.compose.ui.res.stringResource
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "groups")
data class Groups(@PrimaryKey val id : Long, val name : String = "New Group", val num_splits_men : Int = 4, val num_splits_women : Int = 4)
