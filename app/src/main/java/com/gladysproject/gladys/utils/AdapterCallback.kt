package com.gladysproject.gladys.utils

import com.gladysproject.gladys.database.entity.Task

interface AdapterCallback {

    interface AdapterCallbackDeviceState {
        fun onClickCallbackDeviceState(id: Long?, value: Float?)
    }

    interface AdapterCallbackTask {
        fun onClickCallbackTask(task: Task)
    }
}
