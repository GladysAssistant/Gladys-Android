package com.gladysassistant.gladys.database.dao

import androidx.room.*
import com.gladysassistant.gladys.database.entity.DeviceType

@Dao
interface DeviceTypeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDeviceType(devicetype: DeviceType)

    @Update
    fun updateDeviceType(devicetype: DeviceType)

    @Query("UPDATE devicetype SET lastValue = :value WHERE id = :id")
    fun updateDeviceTypeLastValue(value: Float, id: Long)

    @Query("DELETE FROM devicetype")
    fun deleteDeviceTypes()

    @Query("SELECT * FROM devicetype WHERE roomId = :id")
    fun getDeviceTypeByRoom(id : Long): MutableList<DeviceType>

}

