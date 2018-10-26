package com.gladysproject.gladys.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Switch
import com.gladysproject.gladys.R
import com.gladysproject.gladys.database.GladysDb
import com.gladysproject.gladys.database.entity.DeviceType
import com.gladysproject.gladys.database.entity.Rooms
import com.gladysproject.gladys.utils.AdapterCallback
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder
import kotlinx.android.synthetic.main.card_device_binary.view.*
import kotlinx.android.synthetic.main.card_device_multilevel.view.*
import kotlinx.android.synthetic.main.card_device_room.view.*
import kotlinx.android.synthetic.main.card_device_sensor.view.*
import kotlinx.coroutines.experimental.launch
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar

class DeviceTypeAdapter(
        private var deviceTypeByRoom: MutableList<Rooms>,
        private var context : Context,
        private var callbacks: AdapterCallback.AdapterCallbackDeviceState) :
        AbstractExpandableItemAdapter<DeviceTypeAdapter.RoomVH, RecyclerView.ViewHolder>() {

    init {
        setHasStableIds(true) // this is required for expandable feature.
    }

    override fun getGroupCount(): Int {
        return deviceTypeByRoom.size
    }

    override fun getChildCount(groupPosition: Int): Int {
        return deviceTypeByRoom[groupPosition].deviceTypes.size
    }

    override fun getGroupId(groupPosition: Int): Long {
        return deviceTypeByRoom[groupPosition].id
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return deviceTypeByRoom[groupPosition].deviceTypes[childPosition].id
    }

    override fun onCheckCanExpandOrCollapseGroup(holder: RoomVH, groupPosition: Int, x: Int, y: Int, expand: Boolean): Boolean {

        /** Update state in list for onBind  function */
        deviceTypeByRoom[groupPosition].isExpanded = expand

        /** Save state in database */
        launch {
            GladysDb.database?.roomsDao()?.updateRoomExpand(expand, deviceTypeByRoom[groupPosition].id)
        }

        return true
    }

    override fun getChildItemViewType(groupPosition: Int, childPosition: Int): Int {
        return if (deviceTypeByRoom[groupPosition].deviceTypes[childPosition].sensor?.toInt() == 1) 3
            else { when (deviceTypeByRoom[groupPosition].deviceTypes[childPosition].type) {
                "binary" -> 1
                else -> 2
            }
        }
    }

    override fun onCreateGroupViewHolder(parent: ViewGroup, viewType: Int): RoomVH? {
        return RoomVH(LayoutInflater.from(parent.context).inflate(R.layout.card_device_room, parent, false))
    }

    override fun onCreateChildViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        return when (viewType) {

            1 -> BinaryVH(LayoutInflater.from(parent.context).inflate(R.layout.card_device_binary, parent, false))

            2 -> MultilevelVH(LayoutInflater.from(parent.context).inflate(R.layout.card_device_multilevel, parent, false))

            else -> SensorVH(LayoutInflater.from(parent.context).inflate(R.layout.card_device_sensor, parent, false))
        }
    }

    override fun onBindGroupViewHolder(holder: RoomVH, groupPosition: Int, viewType: Int) {
        holder.itemView.isClickable
        holder.bind(deviceTypeByRoom[groupPosition])

        if(deviceTypeByRoom[groupPosition].isExpanded){
            holder.itemView.card_room.showCorner(true, true, false, false)
            holder.itemView.divider.visibility = View.VISIBLE
            holder.itemView.room_arrow.displayedChild = 0
        }else{
            holder.itemView.card_room.showCorner(true, true, true, true)
            holder.itemView.divider.visibility = View.INVISIBLE
            holder.itemView.room_arrow.displayedChild = 1
        }
    }

    override fun onBindChildViewHolder(holder: RecyclerView.ViewHolder, groupPosition: Int, childPosition: Int, viewType: Int) {

        /** Bind view compared to view type and set the corner of card if the child is a last ghild of the group*/

        when(viewType) {
            1 -> {
                (holder as BinaryVH).bind(deviceTypeByRoom[groupPosition].deviceTypes[childPosition], deviceTypeByRoom, groupPosition, context, callbacks)
                if (childPosition + 1 == deviceTypeByRoom[groupPosition].deviceTypes.size) holder.itemView.card_device_binary.showCorner(false, false, true, true)
                else holder.itemView.card_device_binary.showCorner(false, false, false, false)
            } 2 -> {
                (holder as MultilevelVH).bind(deviceTypeByRoom[groupPosition].deviceTypes[childPosition], context, callbacks)
                if (childPosition + 1 == deviceTypeByRoom[groupPosition].deviceTypes.size) holder.itemView.card_device_multilevel.showCorner(false, false, true, true)
                else holder.itemView.card_device_multilevel.showCorner(false, false, false, false)
            } 3 -> {
                (holder as SensorVH).bind(deviceTypeByRoom[groupPosition].deviceTypes[childPosition], context)
                if (childPosition + 1 == deviceTypeByRoom[groupPosition].deviceTypes.size) holder.itemView.card_device_sensor.showCorner(false, false, true, true)
                else holder.itemView.card_device_sensor.showCorner(false, false, false, false)
            }

        }

    }

    class RoomVH(itemView: View) : AbstractExpandableItemViewHolder(itemView){
        fun bind(room: Rooms) {
            itemView.room_name.text = room.name
        }
    }

    class BinaryVH(itemView: View) : RecyclerView.ViewHolder(itemView){
        @SuppressLint("SetTextI18n")
        fun bind(deviceType: DeviceType, deviceTypeByRoom: MutableList<Rooms>, groupPosition: Int, context: Context, callbacks: AdapterCallback.AdapterCallbackDeviceState){
            if(deviceType.deviceTypeName != null && deviceType.deviceTypeName != "")itemView.device_binary_name.text = deviceType.deviceTypeName
            else itemView.device_binary_name.text = "${context.getString(R.string.devicetype)} : ${deviceType.id}"

            if(deviceType.tag != null)itemView.device_binary_tag.text = deviceType.tag
            else itemView.device_binary_tag.text = context.getString(R.string.no_tag)

            itemView.device_binary_value.setOnCheckedChangeListener(null)
            if(deviceType.lastValue != null) itemView.device_binary_value.isChecked = deviceType.lastValue == 1.toFloat()

            itemView.findViewById<Switch>(R.id.device_binary_value).setOnCheckedChangeListener { _, isChecked ->
                if (deviceTypeByRoom[groupPosition].isExpanded) {
                    if (isChecked) callbacks.onClickCallbackDeviceState(deviceType.id, 1f)
                    else callbacks.onClickCallbackDeviceState(deviceType.id, 0f)
                }
            }
        }
    }

    class MultilevelVH(itemView: View) : RecyclerView.ViewHolder(itemView){
        @SuppressLint("SetTextI18n")
        fun bind(deviceType: DeviceType, context: Context, callbacks: AdapterCallback.AdapterCallbackDeviceState){
            if(deviceType.deviceTypeName != null && deviceType.deviceTypeName != "")itemView.device_multilevel_name.text = deviceType.deviceTypeName
            else itemView.device_multilevel_name.text = "${context.getString(R.string.devicetype)} : ${deviceType.id}"

            if(deviceType.tag != null)itemView.device_multilevel_tag.text = deviceType.tag
            else itemView.device_multilevel_tag.text = context.getString(R.string.no_tag)

            itemView.device_multilevel_value.max = deviceType.max!!
            if(deviceType.lastValue != null) itemView.device_multilevel_value.progress = deviceType.lastValue!!.toInt()

            itemView.findViewById<DiscreteSeekBar>(R.id.device_multilevel_value).setOnProgressChangeListener(object : DiscreteSeekBar.OnProgressChangeListener {
                override fun onProgressChanged(seekBar: DiscreteSeekBar, value: Int, fromUser: Boolean) {}
                override fun onStartTrackingTouch(seekBar: DiscreteSeekBar) {}
                override fun onStopTrackingTouch(seekBar: DiscreteSeekBar) {
                    callbacks.onClickCallbackDeviceState(deviceType.id, seekBar.progress.toFloat())
                }
            })

        }
    }

    class SensorVH(itemView: View) : RecyclerView.ViewHolder(itemView){
        @SuppressLint("SetTextI18n")
        fun bind(deviceType: DeviceType, context: Context) {
            if(deviceType.deviceTypeName != null && deviceType.deviceTypeName != "")itemView.device_sensor_name.text = deviceType.deviceTypeName
            else itemView.device_sensor_name.text = "${context.getString(R.string.devicetype)} : ${deviceType.id}"

            if(deviceType.tag != null)itemView.device_sensor_tag.text = deviceType.tag
            else itemView.device_sensor_tag.text = context.getString(R.string.no_tag)

            itemView.device_sensor_value.text =
                    if (deviceType.lastValue != null && deviceType.unit != null) "${deviceType.lastValue} ${deviceType.unit}"
                    else if (deviceType.lastValue != null) "${deviceType.lastValue}"
                    else "null"
        }
    }

    companion object {
        fun setExpandedGroups(deviceTypeByRoom: MutableList<Rooms>, expandableItemManager: RecyclerViewExpandableItemManager) {
            for ((index, room) in deviceTypeByRoom.withIndex()) {
                if (room.isExpanded) {
                    expandableItemManager.expandGroup(index)
                }
            }
        }
    }
}