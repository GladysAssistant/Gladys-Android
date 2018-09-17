package com.gladysproject.gladys.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import com.gladysproject.gladys.database.converter.DateConverter
import com.gladysproject.gladys.database.dao.EventDao
import com.gladysproject.gladys.database.entity.Event

@Database(entities = arrayOf(Event::class), version = 1)
@TypeConverters(DateConverter::class)
abstract class GladysAppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
}