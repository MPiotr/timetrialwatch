package com.github.mpiotr.competitionwatch.dataset

import android.content.ContentValues
import android.content.Context
import androidx.room.OnConflictStrategy
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.github.mpiotr.competitionwatch.R

fun getDatabaseCallbacks(context : Context) : RoomDatabase.Callback = object : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        val cv = ContentValues()
        val defG = Groups(1L)
        cv.put("id", 1L)
        cv.put("name", context.getString(R.string.main_group_name))
        cv.put("num_splits_men", defG.num_splits_men)
        cv.put("num_splits_women", defG.num_splits_women)
        db.insert("groups", OnConflictStrategy.REPLACE, cv)
        cv.clear()
        val defSettings = Settings(1L)
        cv.put("start_interval_seconds", defSettings.start_interval_seconds)
        cv.put("start_initial_offset_seconds", defSettings.start_initial_offset_seconds)
        cv.put("competition_start_time", defSettings.competition_start_time)
        cv.put("use_name", defSettings.use_name)
        cv.put("use_colors", defSettings.use_colors)
        cv.put("use_email", defSettings.use_email)
        cv.put("play_start_sound", defSettings.play_start_sound)
        cv.put("automatic_start", defSettings.automatic_start)
        db.insert("info", OnConflictStrategy.REPLACE, cv)
    }

    /*fun onOpen(db: SupportSQLiteDatabase?) {
            // do something every time database is open
        }*/
}