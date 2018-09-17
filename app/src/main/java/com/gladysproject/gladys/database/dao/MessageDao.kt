package com.gladysproject.gladys.database.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.gladysproject.gladys.database.entity.Message

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessage(message: Message)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessages(message: MutableList<Message>)

    @Query("DELETE FROM message")
    fun deleteMessages()

    @Query("SELECT * FROM message")
    fun getAllMessages(): MutableList<Message>

}

