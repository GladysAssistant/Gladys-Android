package com.gladysproject.gladys.database.dao

import android.arch.persistence.room.*
import com.gladysproject.gladys.database.entity.Rooms

@Dao
interface RoomsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRoom(room: Rooms)

    @Query("UPDATE room SET isExpanded = :isExpanded WHERE id = :id")
    fun updateRoomExpand(isExpanded: Boolean, id: Long)

    @Query("UPDATE room SET id = :id, name = :name WHERE id = :id")
    fun updateRoomWithoutExpand(name: String?, id: Long)

    @Query("DELETE FROM room")
    fun deleteRooms()

    @Query("SELECT * FROM room WHERE id = :id")
    fun getRoomsById(id: Long): Rooms

    @Query("SELECT * FROM room")
    fun getAllRooms(): MutableList<Rooms>
}

