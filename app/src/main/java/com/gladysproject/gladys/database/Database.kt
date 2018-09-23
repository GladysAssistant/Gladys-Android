package com.gladysproject.gladys.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import com.gladysproject.gladys.database.converter.DateConverter
import com.gladysproject.gladys.database.dao.*
import com.gladysproject.gladys.database.entity.*

@Database(entities = [Event::class, Message::class, Rooms::class, DeviceType::class, Task::class], version = 1)
@TypeConverters(DateConverter::class)
abstract class GladysAppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun messageDao(): MessageDao
    abstract fun roomsDao(): RoomsDao
    abstract fun deviceTypeDao(): DeviceTypeDao
    abstract fun taskDao(): TaskDao
}

abstract class GladysDb {

    companion object {

        var database: GladysAppDatabase? = null

        fun initializeDatabase(context: Context){
            GladysDb.database = Room.databaseBuilder(context, GladysAppDatabase::class.java, "gladys-app-db")
                    .fallbackToDestructiveMigration()
                    .build()
        }

    }

}