package com.gladysassistant.gladys.database.dao

import androidx.room.*
import com.gladysassistant.gladys.database.entity.Task

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTask(task: Task)

    @Update
    fun updateTask(task: Task)

    @Query("DELETE FROM task WHERE id = :id")
    fun deleteTask(id: Long)

    @Query("SELECT * FROM task")
    fun getAllTasks(): MutableList<Task>

}