package com.gladysproject.gladys.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Switch
import com.gladysproject.gladys.R
import com.gladysproject.gladys.models.DeviceType
import com.gladysproject.gladys.models.DeviceTypeByRoom
import com.gladysproject.gladys.utils.AdapterCallback
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder
import kotlinx.android.synthetic.main.card_device_binary.view.*
import kotlinx.android.synthetic.main.card_device_multilevel.view.*
import kotlinx.android.synthetic.main.card_device_room.view.*
import kotlinx.android.synthetic.main.card_device_sensor.view.*

class DeviceTypeAdapter(
        private var deviceTypeByRoom: List<DeviceTypeByRoom>,
        private var context : Context,
        private var callbacks: AdapterCallback.AdapterCallbackDeviceState) :
        AbstractExpandableItemAdapter<DeviceTypeAdapter.RoomVH, RecyclerView.ViewHolder>() {

    private var rotationAngle = 0

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
        return deviceTypeByRoom[groupPosition].roomId
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return deviceTypeByRoom[groupPosition].deviceTypes[childPosition].deviceTypeId
    }

    override fun onCheckCanExpandOrCollapseGroup(holder: RoomVH, groupPosition: Int, x: Int, y: Int, expand: Boolean): Boolean {
        if(expand){
            holder.itemView.card_room.showCorner(true, true, false, false)
            holder.itemView.divider.visibility = View.VISIBLE
            rotationAngle = if (rotationAngle == 0) 180 else 0
            holder.itemView.arrow.animate().rotation(rotationAngle.toFloat()).setDuration(500).start()
        }
        else{
            holder.itemView.card_room.showCorner(true, true, true, true)
            holder.itemView.divider.visibility = View.INVISIBLE
            rotationAngle = if (rotationAngle == 180) 0 else 180
            holder.itemView.arrow.animate().rotation(rotationAngle.toFloat()).setDuration(500).start()
        }
        return true
    }

    override fun getChildItemViewType(groupPosition: Int, childPosition: Int): Int {
        val viewType: Int

        when (deviceTypeByRoom[groupPosition].deviceTypes[childPosition].sensor?.toInt()) {
            1 -> viewType = 3

            else -> viewType = when (deviceTypeByRoom[groupPosition].deviceTypes[childPosition].type) {

                "binary" -> 1

                "multilevel" -> 2

                "byte" -> 2

                "brightness" -> 2

                "saturation" -> 2

                else -> 3
            }
        }

        return viewType
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
    }

    override fun onBindChildViewHolder(holder: RecyclerView.ViewHolder, groupPosition: Int, childPosition: Int, viewType: Int) {

        when(viewType) {
            1 -> (holder as BinaryVH).bind(deviceTypeByRoom[groupPosition].deviceTypes[childPosition], context, callbacks)
            2 -> (holder as MultilevelVH).bind(deviceTypeByRoom[groupPosition].deviceTypes[childPosition], context, callbacks)
            3 -> (holder as SensorVH).bind(deviceTypeByRoom[groupPosition].deviceTypes[childPosition], context)
        }

    }

    class RoomVH(itemView: View) : AbstractExpandableItemViewHolder(itemView){
        fun bind(room: DeviceTypeByRoom) {
            itemView.roomName.text = room.roomName
        }
    }

    class BinaryVH(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun bind(deviceType: DeviceType, context: Context, callbacks: AdapterCallback.AdapterCallbackDeviceState){
            itemView.device_binary_name.text = deviceType.deviceTypeName

            if(deviceType.tag != null)itemView.device_binary_tag.text = deviceType.tag
            else itemView.device_binary_tag.text = context.getString(R.string.no_tag)

            if(deviceType.lastValue != null) itemView.device_binary_value.isChecked = deviceType.lastValue == 1.toFloat()

            itemView.findViewById<Switch>(R.id.device_binary_value).setOnCheckedChangeListener { _, isChecked ->
                if(isChecked) callbacks.onClickCallbackDeviceState(deviceType.deviceTypeId, 1f)
                else callbacks.onClickCallbackDeviceState(deviceType.deviceTypeId, 0f)
            }
        }
    }

    class MultilevelVH(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun bind(deviceType: DeviceType, context: Context, callbacks: AdapterCallback.AdapterCallbackDeviceState){
            itemView.device_multilevel_name.text = deviceType.deviceTypeName

            if(deviceType.tag != null)itemView.device_multilevel_tag.text = deviceType.tag
            else itemView.device_multilevel_tag.text = context.getString(R.string.no_tag)

            itemView.device_multilevel_value.max = deviceType.max!!
            if(deviceType.lastValue != null) itemView.device_multilevel_value.progress = deviceType.lastValue!!.toInt()

            itemView.findViewById<SeekBar>(R.id.device_multilevel_value).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                    callbacks.onClickCallbackDeviceState(deviceType.deviceTypeId, i.toFloat())
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
        }
    }

    class SensorVH(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun bind(deviceType: DeviceType, context: Context) {
            itemView.device_sensor_name.text = deviceType.deviceTypeName

            if(deviceType.tag != null)itemView.device_sensor_tag.text = deviceType.tag
            else itemView.device_sensor_tag.text = context.getString(R.string.no_tag)

            itemView.device_sensor_value.text =
                    if (deviceType.lastValue != null && deviceType.unit != null) "${deviceType.lastValue} ${deviceType.unit}"
                    else if (deviceType.lastValue != null) "${deviceType.lastValue}"
                    else "null"

        }
    }
}