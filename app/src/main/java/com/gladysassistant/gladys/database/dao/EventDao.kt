package com.gladysassistant.gladys.database.dao

import androidx.room.*
import com.gladysassistant.gladys.database.entity.Event

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

