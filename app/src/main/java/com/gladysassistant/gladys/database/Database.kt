package com.gladysassistant.gladys.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gladysassistant.gladys.database.converter.DateConverter
import com.gladysassistant.gladys.database.dao.*
import com.gladysassistant.gladys.database.entity.*

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