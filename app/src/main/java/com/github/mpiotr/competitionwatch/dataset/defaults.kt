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
        cv.put("id", 1L)
        cv.put("name", context.getString(R.string.main_group_name))
        cv.put("num_splits_men", 4)
        cv.put("num_splits_women", 4)
        db.insert("groups", OnConflictStrategy.REPLACE, cv)
        cv.clear()
        cv.put("start_interval_seconds", 15)
        cv.put("competition_start_time", 0L)
        db.insert("info", OnConflictStrategy.REPLACE, cv)
    }

    /*fun onOpen(db: SupportSQLiteDatabase?) {
            // do something every time database is open
        }*/
}