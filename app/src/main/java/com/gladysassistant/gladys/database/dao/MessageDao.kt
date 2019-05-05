package com.gladysassistant.gladys.database.dao

import androidx.room.*
import com.gladysassistant.gladys.database.entity.Message

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

