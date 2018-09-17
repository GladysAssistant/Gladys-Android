package com.gladysproject.gladys.database.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import com.gladysproject.gladys.database.entity.Event
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEvent(event: Event)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEvents(event: MutableList<Event>)

    @Query("DELETE FROM event")
    fun deleteEvents()

    @Query("SELECT * FROM event ORDER BY datetime DESC")
    fun getAllEvents(): MutableList<Event>

}

